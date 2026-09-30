"""Jobs+ 메트릭 v2 집계 스크립트.

서버의 logs/jobsplus-metrics/v2/<시즌> 폴더를 읽어 직업별 작업시간, EXP/h, BTC/h,
초·중·후반 지표, Action별 표, 레벨·스킬 구매 타임라인, 코인 흐름, 데이터 품질 경고를 만든다.
표준 라이브러리만 사용한다.

사용 예:
    python analyze_metrics.py <메트릭 폴더> --exclude SERVER_ADMIN__ --since 2026-10-01
"""

import argparse
import bisect
import csv
import math
import sys
from collections import defaultdict
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Set, Tuple

KST = timezone(timedelta(hours=9))
ADVENTURER = "jobsplus:adventurer"
UNASSIGNED = "(unassigned)"
DEFAULT_EXCLUDED_SOURCES = "spawner,trial_spawner"
SESSION_BREAK_SECONDS = 120
STAGES = ("early", "mid", "late")


@dataclass
class Options:
    exclude_players: Set[str] = field(default_factory=set)
    include_non_survival: bool = False
    excluded_sources: Set[str] = field(default_factory=lambda: set(DEFAULT_EXCLUDED_SOURCES.split(",")))
    afk_seconds: int = 300
    gap_seconds: int = 600
    # 직업이 하나뿐인 세션은 gap 밖의 활동도 그 직업의 재료 준비·이동으로 본다.
    session_fill: bool = True
    stage_levels: Tuple[int, int] = (32, 64)
    since_ms: Optional[int] = None
    until_ms: Optional[int] = None
    balance_versions: Optional[Set[str]] = None


@dataclass
class Minute:
    """activity.csv 한 행이 덮는 구간. start는 포함하지 않고 end는 포함한다."""
    start_ms: int
    end_ms: int
    afk: bool
    session: int = 0
    weights: Dict[str, float] = field(default_factory=dict)
    levels: Dict[str, int] = field(default_factory=dict)
    adventurer_weight: float = 0.0
    adventurer_level: int = 0
    allocation: Dict[str, float] = field(default_factory=dict)
    allocation_levels: Dict[str, int] = field(default_factory=dict)

    @property
    def seconds(self) -> float:
        return max(0.0, (self.end_ms - self.start_ms) / 1000.0)


@dataclass
class Metrics:
    actions: List[dict]
    activity: List[dict]
    events: List[dict]
    snapshots: List[dict]
    bitcoin_rewards: Dict[Tuple[str, str], List[Tuple[float, int]]]
    powerup_totals: Dict[str, Dict[str, int]]
    powerup_prices: Dict[Tuple[str, str], int]


# ---------------------------------------------------------------- 읽기

def read_csv(path: Path) -> List[dict]:
    if not path.exists():
        return []
    with path.open(encoding="utf-8", newline="") as handle:
        return list(csv.DictReader(handle))


def to_int(value, default=0) -> int:
    try:
        return int(float(value))
    except (TypeError, ValueError):
        return default


def to_float(value, default=0.0) -> float:
    try:
        return float(value)
    except (TypeError, ValueError):
        return default


def to_bool(value) -> bool:
    return str(value).strip().lower() == "true"


def load(directory: Path) -> Metrics:
    actions = read_csv(directory / "actions.csv")
    for row in actions:
        row["bucket_start_ms"] = to_int(row["bucket_start_ms"])
        row["bucket_ms"] = to_int(row["bucket_ms"], 5000)
        row["job_level"] = to_int(row["job_level"])
        row["exp_coupon_multiplier"] = max(1, to_int(row["exp_coupon_multiplier"], 1))
        row["btc_coupon_multiplier"] = max(1, to_int(row["btc_coupon_multiplier"], 1))
        row["count"] = to_int(row["count"])
        for key in ("exp_base", "exp_coupon_bonus", "exp_skill_bonus", "exp_total"):
            row[key] = to_float(row[key])
        row["btc"] = to_int(row["btc"])

    activity = read_csv(directory / "activity.csv")
    for row in activity:
        row["timestamp_ms"] = to_int(row["timestamp_ms"])
        row["sample_seconds"] = to_int(row["sample_seconds"])
        row["idle_seconds"] = to_int(row["idle_seconds"])
        row["job_actions"] = to_int(row["job_actions"])

    events = read_csv(directory / "events.csv")
    for row in events:
        row["timestamp_ms"] = to_int(row["timestamp_ms"])
        row["details"] = parse_detail(row.get("detail", ""))

    snapshots = read_csv(directory / "snapshots.csv")

    bitcoin_rewards: Dict[Tuple[str, str], List[Tuple[float, int]]] = defaultdict(list)
    for row in read_csv(directory / "balance_rewards.csv"):
        if row["holder_type"] == "job" and row["reward_type"] == "jobsplus:bitcoin_reward":
            bitcoin_rewards[(row["balance_version"], row["action_id"])].append(
                (to_float(row["chance"]), max(1, to_int(row["btc_amount"], 1))))

    powerup_totals: Dict[str, Dict[str, int]] = defaultdict(lambda: defaultdict(int))
    powerup_prices: Dict[Tuple[str, str], int] = {}
    for row in read_csv(directory / "balance_powerups.csv"):
        powerup_totals[row["balance_version"]][row["job_id"]] += 1
        powerup_prices[(row["balance_version"], row["powerup_id"])] = to_int(row["price"])

    return Metrics(actions, activity, events, snapshots, dict(bitcoin_rewards),
                   {version: dict(jobs) for version, jobs in powerup_totals.items()}, powerup_prices)


def parse_detail(detail: str) -> Dict[str, str]:
    values = {}
    for part in (detail or "").split(";"):
        if "=" in part:
            key, value = part.split("=", 1)
            values[key] = value
    return values


# ---------------------------------------------------------------- 필터

def player_excluded(uuid: str, name: str, options: Options) -> bool:
    lowered = {value.lower() for value in options.exclude_players}
    return uuid.lower() in lowered or (name or "").lower() in lowered


def in_window(timestamp_ms: int, options: Options) -> bool:
    if options.since_ms is not None and timestamp_ms < options.since_ms:
        return False
    if options.until_ms is not None and timestamp_ms >= options.until_ms:
        return False
    return True


def filter_actions(rows: Iterable[dict], options: Options, warnings: List[str]) -> Tuple[List[dict], List[dict]]:
    kept, excluded_sources = [], []
    non_survival = 0
    unknown = 0
    other_balance = 0
    for row in rows:
        if player_excluded(row["player_uuid"], row["player_name"], options):
            continue
        if not in_window(row["bucket_start_ms"], options):
            continue
        if options.balance_versions is not None and row["balance_version"] not in options.balance_versions:
            other_balance += 1
            continue
        if not options.include_non_survival and row["game_mode"] != "survival":
            non_survival += 1
            continue
        if row["source"] in options.excluded_sources:
            excluded_sources.append(row)
            continue
        if not row["action_id"]:
            unknown += 1
        kept.append(row)
    if non_survival:
        warnings.append(f"생존 모드가 아닌 액션 행 {non_survival}개를 제외했습니다.")
    if other_balance:
        warnings.append(f"지정하지 않은 밸런스 버전의 액션 행 {other_balance}개를 제외했습니다.")
    if unknown:
        warnings.append(f"직업 액션 밖에서 기록된 보상 행이 {unknown}개 있습니다(action_id 비어 있음). 누락 경로를 확인하세요.")
    if excluded_sources:
        count = sum(row["count"] for row in excluded_sources)
        warnings.append(f"스폰 원인 {sorted(options.excluded_sources)} 대상 액션 {count}회를 기본 집계에서 제외했습니다.")
    return kept, excluded_sources


# ---------------------------------------------------------------- 시간 배분

def build_minutes(activity: List[dict], actions: List[dict], options: Options,
                  warnings: List[str]) -> Dict[str, List[Minute]]:
    """플레이어별 1분 구간을 만들고 잠수 여부와 직업별 가중치를 채운다."""
    minutes_by_player: Dict[str, List[Minute]] = defaultdict(list)
    for row in activity:
        if player_excluded(row["player_uuid"], row["player_name"], options):
            continue
        if not in_window(row["timestamp_ms"], options):
            continue
        if not options.include_non_survival and row["game_mode"] != "survival":
            continue
        end = row["timestamp_ms"]
        start = end - row["sample_seconds"] * 1000
        minutes_by_player[row["player_uuid"]].append(Minute(start, end, False))

    for uuid, minutes in minutes_by_player.items():
        minutes.sort(key=lambda minute: minute.end_ms)
        mark_sessions(minutes)
    ends_by_player = {uuid: [minute.end_ms for minute in minutes] for uuid, minutes in minutes_by_player.items()}

    idle_by_player: Dict[str, List[Tuple[int, int]]] = defaultdict(list)
    for row in activity:
        if row["idle_seconds"] >= options.afk_seconds:
            idle_by_player[row["player_uuid"]].append((row["timestamp_ms"] - row["idle_seconds"] * 1000, row["timestamp_ms"]))
    for uuid, minutes in minutes_by_player.items():
        mark_afk(minutes, idle_by_player.get(uuid, []))

    players_without_activity = set()
    for row in actions:
        minutes = minutes_by_player.get(row["player_uuid"])
        if not minutes:
            players_without_activity.add(row["player_name"])
            continue
        minute = find_minute(minutes, ends_by_player[row["player_uuid"]], row["bucket_start_ms"])
        if minute is None:
            continue
        weight = row["exp_base"] + row["count"] * 1e-6
        if row["job_id"] == ADVENTURER:
            minute.adventurer_weight += weight
            minute.adventurer_level = max(minute.adventurer_level, row["job_level"])
        else:
            minute.weights[row["job_id"]] = minute.weights.get(row["job_id"], 0.0) + weight
            minute.levels[row["job_id"]] = max(minute.levels.get(row["job_id"], 0), row["job_level"])

    if players_without_activity:
        warnings.append("activity.csv 없이 액션만 있는 플레이어가 있어 작업시간에서 빠졌습니다: "
                        + ", ".join(sorted(players_without_activity)))

    for minutes in minutes_by_player.values():
        allocate(minutes, options)
    return minutes_by_player


def mark_sessions(minutes: List[Minute]) -> None:
    session = 0
    previous_end = None
    for minute in minutes:
        if previous_end is not None and minute.start_ms - previous_end > SESSION_BREAK_SECONDS * 1000:
            session += 1
        minute.session = session
        previous_end = minute.end_ms


def mark_afk(minutes: List[Minute], idle_ranges: List[Tuple[int, int]]) -> None:
    """idle_seconds가 기준 이상인 샘플은 그 시간만큼 거슬러 올라가 앞 구간까지 잠수로 본다."""
    for idle_start, idle_end in idle_ranges:
        for minute in minutes:
            if minute.end_ms > idle_start and minute.end_ms <= idle_end:
                minute.afk = True


def find_minute(minutes: List[Minute], ends: List[int], timestamp_ms: int) -> Optional[Minute]:
    """ends는 minutes의 end_ms를 같은 순서로 담은 목록이다."""
    index = bisect.bisect_left(ends, timestamp_ms)
    if index < len(minutes) and minutes[index].start_ms <= timestamp_ms <= minutes[index].end_ms:
        return minutes[index]
    return None


def allocate(minutes: List[Minute], options: Options) -> None:
    """직업 액션이 있는 구간은 기본 EXP 비율로 나누고, 없는 활동 구간은 같은 세션의 가까운 직업 구간에 붙인다.

    가까운 직업 구간이 gap 밖이어도 그 세션의 직업 액션이 한 직업뿐이면 재료 준비·이동으로 보고 그 직업에 붙인다.
    여러 직업을 오간 세션에서 gap 밖에 있는 구간은 어느 직업의 준비인지 알 수 없어 미배분으로 둔다.
    """
    sources = [index for index, minute in enumerate(minutes) if minute.weights and not minute.afk]
    source_ends = [minutes[index].end_ms for index in sources]
    session_sources: Dict[int, List[Minute]] = defaultdict(list)
    for index in sources:
        session_sources[minutes[index].session].append(minutes[index])

    for minute in minutes:
        if minute.afk or not minute.weights:
            continue
        total = sum(minute.weights.values())
        for job, weight in minute.weights.items():
            minute.allocation[job] = minute.seconds * weight / total
            minute.allocation_levels[job] = minute.levels.get(job, 0)

    for minute in minutes:
        if minute.afk or minute.weights:
            continue
        source = nearest_source(minutes, sources, source_ends, minute, options.gap_seconds)
        if source is None and options.session_fill:
            source = single_job_source(session_sources.get(minute.session, []), minute)
        if source is not None:
            job = max(source.weights.items(), key=lambda item: item[1])[0]
            minute.allocation[job] = minute.seconds
            minute.allocation_levels[job] = source.levels.get(job, 0)
        elif minute.adventurer_weight > 0:
            minute.allocation[ADVENTURER] = minute.seconds
            minute.allocation_levels[ADVENTURER] = minute.adventurer_level
        else:
            minute.allocation[UNASSIGNED] = minute.seconds


def nearest_source(minutes: List[Minute], sources: List[int], source_ends: List[int], minute: Minute,
                   gap_seconds: int) -> Optional[Minute]:
    index = bisect.bisect_left(source_ends, minute.end_ms)
    best, best_distance = None, None
    for candidate_index in (index - 1, index):
        if 0 <= candidate_index < len(sources):
            candidate = minutes[sources[candidate_index]]
            if candidate.session != minute.session:
                continue
            distance = abs(candidate.end_ms - minute.end_ms)
            if distance <= gap_seconds * 1000 and (best_distance is None or distance < best_distance):
                best, best_distance = candidate, distance
    return best


def single_job_source(session_sources: List[Minute], minute: Minute) -> Optional[Minute]:
    """세션의 직업 액션이 한 직업뿐이면 그 직업의 가장 가까운 구간을 돌려준다."""
    jobs = {job for source in session_sources for job in source.weights}
    if len(jobs) != 1:
        return None
    return min(session_sources, key=lambda source: abs(source.end_ms - minute.end_ms))


# ---------------------------------------------------------------- 집계

def stage_of(level: int, options: Options) -> str:
    if level < options.stage_levels[0]:
        return "early"
    if level < options.stage_levels[1]:
        return "mid"
    return "late"


def expected_bitcoin(row: dict, metrics: Metrics, with_coupon: bool) -> float:
    rewards = metrics.bitcoin_rewards.get((row["balance_version"], row["action_id"]), [])
    multiplier = row["btc_coupon_multiplier"] if with_coupon else 1
    return sum(row["count"] * min(100.0, chance * multiplier) / 100.0 * amount for chance, amount in rewards)


def new_stat() -> Dict[str, float]:
    return defaultdict(float)


def add_action(stat: Dict[str, float], row: dict, metrics: Metrics) -> None:
    stat["actions"] += row["count"]
    stat["exp_base"] += row["exp_base"]
    stat["exp_coupon_bonus"] += row["exp_coupon_bonus"]
    stat["exp_skill_bonus"] += row["exp_skill_bonus"]
    # 스킬 보너스는 쿠폰이 곱해진 EXP에 붙으므로 쿠폰 배율로 나눠 쿠폰이 없었을 때의 스킬 보너스를 구한다.
    stat["exp_skill_no_coupon"] += row["exp_skill_bonus"] / row["exp_coupon_multiplier"]
    stat["exp_total"] += row["exp_total"]
    stat["btc"] += row["btc"]
    stat["btc_expected"] += expected_bitcoin(row, metrics, with_coupon=False)
    stat["btc_expected_with_coupon"] += expected_bitcoin(row, metrics, with_coupon=True)
    if row["btc_coupon_multiplier"] == 1:
        stat["btc_no_coupon"] += row["btc"]
        stat["btc_expected_no_coupon_rows"] += expected_bitcoin(row, metrics, with_coupon=False)


def rates(stat: Dict[str, float], hours: float) -> Dict[str, object]:
    def per_hour(value):
        return round(value / hours, 3) if hours > 0 else ""

    expected = stat["btc_expected_with_coupon"]
    return {
        "hours": round(hours, 3),
        "actions": int(stat["actions"]),
        "actions_per_h": per_hour(stat["actions"]),
        "exp_base_per_h": per_hour(stat["exp_base"]),
        "exp_actual_no_coupon_per_h": per_hour(stat["exp_base"] + stat["exp_skill_no_coupon"]),
        "exp_total_per_h": per_hour(stat["exp_total"]),
        "btc_expected_per_h": per_hour(stat["btc_expected"]),
        "btc_actual_per_h": per_hour(stat["btc"]),
        "btc_actual": int(stat["btc"]),
        "btc_actual_vs_expected": round(stat["btc"] / expected, 3) if expected > 0 else "",
        "btc_expected_95pct_error": f"±{round(196 / math.sqrt(stat['btc_expected']))}%" if stat["btc_expected"] > 0 else "",
        "coupon_exp_share": round(stat["exp_coupon_bonus"] / stat["exp_total"], 3) if stat["exp_total"] > 0 else "",
    }


def analyze(metrics: Metrics, options: Options) -> Dict[str, List[dict]]:
    warnings: List[str] = []
    actions, excluded_source_rows = filter_actions(metrics.actions, options, warnings)
    minutes_by_player = build_minutes(metrics.activity, actions, options, warnings)

    names: Dict[str, str] = {}
    for row in metrics.actions + metrics.activity + metrics.events:
        if row.get("player_uuid") and row.get("player_name"):
            names[row["player_uuid"]] = row["player_name"]

    # 직업·구간별 작업시간
    job_seconds: Dict[str, float] = defaultdict(float)
    stage_seconds: Dict[Tuple[str, str], float] = defaultdict(float)
    player_rows = []
    job_players: Dict[str, Set[str]] = defaultdict(set)
    for uuid, minutes in minutes_by_player.items():
        online = sum(minute.seconds for minute in minutes)
        afk = sum(minute.seconds for minute in minutes if minute.afk)
        per_job: Dict[str, float] = defaultdict(float)
        for minute in minutes:
            for job, seconds in minute.allocation.items():
                per_job[job] += seconds
                job_seconds[job] += seconds
                if job != UNASSIGNED:
                    stage_seconds[(job, stage_of(minute.allocation_levels.get(job, 0), options))] += seconds
                    job_players[job].add(uuid)
        row = {"player_uuid": uuid, "player_name": names.get(uuid, ""), "online_h": round(online / 3600, 3),
               "afk_h": round(afk / 3600, 3), "active_h": round((online - afk) / 3600, 3)}
        for job, seconds in sorted(per_job.items()):
            row[f"h:{job}"] = round(seconds / 3600, 3)
        player_rows.append(row)

    # 직업·구간·액션별 EXP/BTC
    ends_by_player = {uuid: [minute.end_ms for minute in minutes] for uuid, minutes in minutes_by_player.items()}
    job_stats: Dict[str, Dict[str, float]] = defaultdict(new_stat)
    stage_stats: Dict[Tuple[str, str], Dict[str, float]] = defaultdict(new_stat)
    action_stats: Dict[Tuple[str, str], Dict[str, float]] = defaultdict(new_stat)
    adventurer_own = new_stat()
    for row in actions:
        add_action(job_stats[row["job_id"]], row, metrics)
        add_action(stage_stats[(row["job_id"], stage_of(row["job_level"], options))], row, metrics)
        add_action(action_stats[(row["job_id"], row["action_id"])], row, metrics)
        if row["job_id"] == ADVENTURER:
            minute = find_minute(minutes_by_player.get(row["player_uuid"], []),
                                 ends_by_player.get(row["player_uuid"], []), row["bucket_start_ms"])
            if minute is not None and ADVENTURER in minute.allocation:
                add_action(adventurer_own, row, metrics)

    job_rows = []
    for job in sorted(set(job_stats) | {job for job in job_seconds if job != UNASSIGNED}):
        hours = job_seconds.get(job, 0.0) / 3600
        row = {"job_id": job, "players": len(job_players.get(job, set()))}
        row.update(rates(job_stats[job], hours))
        for stage in STAGES:
            stage_hours = stage_seconds.get((job, stage), 0.0) / 3600
            stage_rate = rates(stage_stats[(job, stage)], stage_hours)
            row[f"{stage}_h"] = stage_rate["hours"]
            row[f"{stage}_exp_base_per_h"] = stage_rate["exp_base_per_h"]
            row[f"{stage}_btc_expected_per_h"] = stage_rate["btc_expected_per_h"]
        if job == ADVENTURER:
            own = rates(adventurer_own, hours)
            row["note"] = (f"모험가 전용 구간 EXP/h {own['exp_base_per_h']}, "
                           f"타 직업 중 이동 포함 전체 기본 EXP {round(job_stats[job]['exp_base'], 1)}")
        job_rows.append(row)
    if UNASSIGNED in job_seconds:
        warnings.append(f"직업 액션과 연결되지 않은 활동시간 {round(job_seconds[UNASSIGNED] / 3600, 2)}h는 어느 직업에도 넣지 않았습니다.")

    stage_rows = []
    for (job, stage), stat in sorted(stage_stats.items()):
        row = {"job_id": job, "stage": stage}
        row.update(rates(stat, stage_seconds.get((job, stage), 0.0) / 3600))
        stage_rows.append(row)

    action_rows = []
    for (job, action), stat in sorted(action_stats.items()):
        job_exp = job_stats[job]["exp_base"]
        row = {"job_id": job, "action_id": action or "(unknown)"}
        row.update(rates(stat, job_seconds.get(job, 0.0) / 3600))
        row["exp_base_share"] = round(stat["exp_base"] / job_exp, 3) if job_exp > 0 else ""
        action_rows.append(row)

    excluded_rows = []
    excluded_stats: Dict[Tuple[str, str, str], Dict[str, float]] = defaultdict(new_stat)
    for row in excluded_source_rows:
        add_action(excluded_stats[(row["player_name"], row["action_id"], row["source"])], row, metrics)
    for (player, action, source), stat in sorted(excluded_stats.items()):
        excluded_rows.append({"player_name": player, "action_id": action, "source": source,
                              "actions": int(stat["actions"]), "exp_total": round(stat["exp_total"], 3),
                              "btc": int(stat["btc"])})

    level_rows, powerup_rows, failure_rows, coin_rows = timelines(metrics, minutes_by_player, options, names)
    warnings.extend(event_warnings(metrics, options))

    return {
        "jobs": job_rows,
        "stages": stage_rows,
        "actions": action_rows,
        "players": player_rows,
        "level_timeline": level_rows,
        "powerup_timeline": powerup_rows,
        "powerup_failures": failure_rows,
        "coin_flow": coin_rows,
        "excluded_sources": excluded_rows,
        "warnings": [{"warning": warning} for warning in warnings],
    }


def cumulative_hours(minutes: List[Minute], job: str, timestamp_ms: int) -> float:
    return sum(minute.allocation.get(job, 0.0) for minute in minutes if minute.end_ms <= timestamp_ms) / 3600


def timelines(metrics: Metrics, minutes_by_player: Dict[str, List[Minute]], options: Options,
              names: Dict[str, str]):
    level_rows, powerup_rows = [], []
    failures: Dict[Tuple[str, str], int] = defaultdict(int)
    earned: Dict[Tuple[str, str], int] = defaultdict(int)
    spent: Dict[Tuple[str, str], int] = defaultdict(int)
    latest_totals: Dict[str, int] = {}
    for version_totals in metrics.powerup_totals.values():
        for job, total in version_totals.items():
            latest_totals[job] = max(latest_totals.get(job, 0), total)

    for event in sorted(metrics.events, key=lambda row: row["timestamp_ms"]):
        uuid = event["player_uuid"]
        if not uuid or player_excluded(uuid, event["player_name"], options):
            continue
        if not in_window(event["timestamp_ms"], options):
            continue
        minutes = minutes_by_player.get(uuid, [])
        job = event["job_id"]
        when = datetime.fromtimestamp(event["timestamp_ms"] / 1000, KST).strftime("%Y-%m-%d %H:%M:%S")
        if event["event"] == "LEVEL_UP":
            earned[(uuid, job)] += to_int(event["coins_after"]) - to_int(event["coins_before"])
            level_rows.append({"player_name": names.get(uuid, event["player_name"]), "job_id": job,
                               "level": to_int(event["value_after"]), "time_kst": when,
                               "job_hours": round(cumulative_hours(minutes, job, event["timestamp_ms"]), 3)})
        elif event["event"] == "POWERUP_BUY":
            spent[(uuid, job)] += to_int(event["value"])
            owned = to_int(event["details"].get("owned_after"))
            total = latest_totals.get(job, 0)
            powerup_rows.append({"player_name": names.get(uuid, event["player_name"]), "job_id": job,
                                 "powerup_id": event["target_id"], "price": to_int(event["value"]),
                                 "job_level": to_int(event["job_level"]), "coins_after": to_int(event["coins_after"]),
                                 "time_kst": when,
                                 "job_hours": round(cumulative_hours(minutes, job, event["timestamp_ms"]), 3),
                                 "owned_after": owned, "total_powerups": total,
                                 "completed": bool(total) and owned >= total})
        elif event["event"] == "POWERUP_BUY_FAILED":
            failures[(job, event["details"].get("reason", ""))] += 1

    failure_rows = [{"job_id": job, "reason": reason, "count": count}
                    for (job, reason), count in sorted(failures.items())]
    coin_rows = []
    for key in sorted(set(earned) | set(spent)):
        uuid, job = key
        coin_rows.append({"player_name": names.get(uuid, ""), "job_id": job, "coins_earned": earned.get(key, 0),
                          "coins_spent": spent.get(key, 0),
                          "funded_by_other_jobs": max(0, spent.get(key, 0) - earned.get(key, 0))})
    return level_rows, powerup_rows, failure_rows, coin_rows


def event_warnings(metrics: Metrics, options: Options) -> List[str]:
    warnings = []
    counts: Dict[str, int] = defaultdict(int)
    admin_players: Set[str] = set()
    versions: Set[str] = set()
    for event in metrics.events:
        if not in_window(event["timestamp_ms"], options):
            continue
        counts[event["event"]] += 1
        if event["event"].startswith("ADMIN_") and not player_excluded(event["player_uuid"], event["player_name"], options):
            admin_players.add(event["player_name"])
        if event["event"] == "BALANCE_VERSION":
            versions.add(event["target_id"])
    crashes = max(0, counts["SERVER_START"] - counts["SERVER_STOP"] - 1)
    if crashes:
        warnings.append(f"SERVER_STOP 없이 다시 시작된 기록이 {crashes}번 있습니다. 비정상 종료 직전 최대 1분의 기록이 빠졌을 수 있습니다.")
    if admin_players:
        warnings.append("관리자 명령으로 레벨·EXP·코인·스킬이 바뀐 플레이어: " + ", ".join(sorted(admin_players))
                        + " (--exclude로 제외하거나 해당 구간을 따로 보세요)")
    if counts["COUPON_USE"]:
        warnings.append(f"쿠폰 사용 {counts['COUPON_USE']}회. 기본 EXP/h와 기대 BTC/h는 쿠폰을 뺀 값이고, 실제 BTC/h에는 쿠폰이 포함됩니다.")
    if len(versions) > 1:
        warnings.append("밸런스 버전이 여러 개 섞여 있습니다: " + ", ".join(sorted(versions))
                        + " (--balance로 한 버전만 볼 수 있습니다)")
    return warnings


# ---------------------------------------------------------------- 출력

def write_csv(path: Path, rows: List[dict]) -> None:
    if not rows:
        return
    fieldnames: List[str] = []
    for row in rows:
        for key in row:
            if key not in fieldnames:
                fieldnames.append(key)
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)


def summary_markdown(result: Dict[str, List[dict]]) -> str:
    lines = ["| 직업 | 인원 | 작업h | 기본 EXP/h | 실제 EXP/h(쿠폰 제외) | 기대 BTC/h | 실제 BTC/h | 실제/기대 | 초반 EXP/h | 중반 EXP/h | 후반 EXP/h |",
             "| -- | --: | --: | --: | --: | --: | --: | --: | --: | --: | --: |"]
    for row in result["jobs"]:
        lines.append("| {job_id} | {players} | {hours} | {exp_base_per_h} | {exp_actual_no_coupon_per_h} | "
                     "{btc_expected_per_h} | {btc_actual_per_h} | {btc_actual_vs_expected} | {early_exp_base_per_h} | "
                     "{mid_exp_base_per_h} | {late_exp_base_per_h} |".format(**row))
    if result["warnings"]:
        lines.append("")
        lines.append("경고")
        lines.extend(f"- {row['warning']}" for row in result["warnings"])
    return "\n".join(lines)


def parse_date(value: Optional[str]) -> Optional[int]:
    if not value:
        return None
    parsed = datetime.strptime(value, "%Y-%m-%d").replace(tzinfo=KST)
    return int(parsed.timestamp() * 1000)


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Jobs+ 메트릭 v2 집계")
    parser.add_argument("directory", type=Path, help="logs/jobsplus-metrics/v2/<시즌> 폴더")
    parser.add_argument("--out", type=Path, help="결과 CSV 폴더 (기본: <폴더>/analysis)")
    parser.add_argument("--exclude", action="append", default=[], help="제외할 플레이어 이름 또는 UUID (여러 번 지정 가능)")
    parser.add_argument("--since", help="이 날짜(KST, YYYY-MM-DD)부터")
    parser.add_argument("--until", help="이 날짜(KST, YYYY-MM-DD) 전까지")
    parser.add_argument("--balance", action="append", help="이 밸런스 버전만 집계 (여러 번 지정 가능)")
    parser.add_argument("--include-non-survival", action="store_true", help="크리에이티브·관전 모드 기록도 포함")
    parser.add_argument("--exclude-sources", default=DEFAULT_EXCLUDED_SOURCES,
                        help="기본 집계에서 뺄 대상 몹 스폰 원인 (쉼표 구분, 빈 값이면 모두 포함)")
    parser.add_argument("--afk-seconds", type=int, default=300, help="입력 없이 이 시간 이상이면 잠수로 본다")
    parser.add_argument("--gap-minutes", type=int, default=10, help="직업 액션 없는 활동 구간을 붙일 최대 거리")
    parser.add_argument("--no-session-fill", action="store_true",
                        help="직업이 하나뿐인 세션이라도 gap 밖의 활동을 그 직업에 붙이지 않는다")
    parser.add_argument("--stages", default="32,64", help="초반/중반, 중반/후반을 나누는 직업 레벨")
    args = parser.parse_args(argv)

    stage_levels = tuple(int(value) for value in args.stages.split(","))
    if len(stage_levels) != 2 or stage_levels[0] >= stage_levels[1]:
        parser.error("--stages는 '초반끝,중반끝' 형식의 증가하는 두 레벨이어야 합니다.")
    options = Options(
        exclude_players=set(args.exclude),
        include_non_survival=args.include_non_survival,
        excluded_sources={value.strip() for value in args.exclude_sources.split(",") if value.strip()},
        afk_seconds=args.afk_seconds,
        gap_seconds=args.gap_minutes * 60,
        session_fill=not args.no_session_fill,
        stage_levels=stage_levels,
        since_ms=parse_date(args.since),
        until_ms=parse_date(args.until),
        balance_versions=set(args.balance) if args.balance else None,
    )

    if not (args.directory / "actions.csv").exists():
        print(f"actions.csv가 없습니다: {args.directory}", file=sys.stderr)
        return 1

    result = analyze(load(args.directory), options)
    out = args.out or args.directory / "analysis"
    out.mkdir(parents=True, exist_ok=True)
    for name, rows in result.items():
        write_csv(out / f"{name}.csv", rows)
    print(summary_markdown(result))
    print(f"\n결과 CSV: {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
