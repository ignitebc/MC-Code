"""analyze_metrics.py 회귀 검사. 헤더는 Java 메트릭 코드의 HEADER 상수와 같아야 한다."""

import csv
import tempfile
import unittest
from pathlib import Path

import analyze_metrics as am

ACTIONS_HEADER = ("bucket_start_ms,bucket_ms,player_uuid,player_name,job_id,action_id,trigger,source,job_level,game_mode,"
                  "exp_coupon_multiplier,btc_coupon_multiplier,balance_version,count,exp_base,exp_coupon_bonus,"
                  "exp_skill_bonus,exp_total,btc")
ACTIVITY_HEADER = ("timestamp_ms,sample_seconds,player_uuid,player_name,dimension,x,y,z,game_mode,operator,in_vehicle,"
                   "moved_blocks,rotation_degrees,rotation_ticks,menu_changes,job_actions,teleports,idle_seconds,afk")
EVENTS_HEADER = ("timestamp_ms,event,player_uuid,player_name,job_id,target_id,value,value_before,value_after,"
                 "coins_before,coins_after,job_level,detail")
REWARDS_HEADER = ("balance_version,holder_type,holder_id,job_id,action_id,trigger,reward_index,reward_type,chance,"
                  "priority,exp_min,exp_max,btc_amount,exp_multiplier,coin_amount")
POWERUPS_HEADER = "balance_version,powerup_id,job_id,price,required_level,parent_id,powerup_type"

T0 = 1_790_000_000_000
A = "aaaaaaaa-0000-0000-0000-000000000001"
B = "bbbbbbbb-0000-0000-0000-000000000002"
MINER = "jobsplus:miner"
ORE = "jobsplus:miner/break_common_ore"
VERSION = "abc123def456"


def write(path: Path, header: str, rows):
    with path.open("w", encoding="utf-8", newline="") as handle:
        handle.write(header + "\n")
        writer = csv.writer(handle)
        for row in rows:
            writer.writerow(row)


def action(offset_s, uuid, name, job, action_id, count, exp_base, level=5, mode="survival", exp_coupon=1,
           btc_coupon=1, coupon_bonus=0.0, skill_bonus=0.0, btc=0, source=""):
    total = exp_base + coupon_bonus + skill_bonus
    return [T0 + offset_s * 1000, 5000, uuid, name, job, action_id, "arc:on_break_block", source, level, mode,
            exp_coupon, btc_coupon, VERSION, count, exp_base, coupon_bonus, skill_bonus, total, btc]


def activity(minute_index, uuid, name, idle, mode="survival"):
    end = T0 + minute_index * 60_000
    return [end, 60, uuid, name, "minecraft:overworld", 0, 64, 0, mode, "false", "false", 10.0, 30.0, 20, 0, 0, 0,
            idle, str(idle >= 300).lower()]


class AnalyzeMetricsTest(unittest.TestCase):

    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.directory = Path(self.temp.name)
        # 플레이어 A: 1~4분 채굴, 5분은 액션 없이 이동, 6~10분은 잠수(10분 시점 idle 300초)
        activity_rows = [activity(1, A, "Alpha", 5), activity(2, A, "Alpha", 3), activity(3, A, "Alpha", 4),
                         activity(4, A, "Alpha", 2), activity(5, A, "Alpha", 10), activity(6, A, "Alpha", 70),
                         activity(7, A, "Alpha", 130), activity(8, A, "Alpha", 190), activity(9, A, "Alpha", 250),
                         activity(10, A, "Alpha", 300),
                         activity(1, B, "Admin", 5)]
        action_rows = [
            action(10, A, "Alpha", MINER, ORE, 100, 65.8),
            action(70, A, "Alpha", MINER, ORE, 100, 65.8, btc=1),
            action(130, A, "Alpha", MINER, ORE, 100, 65.8),
            # 쿠폰 2배 구간: 기본 10, 쿠폰 +10, 스킬 20% 보너스는 쿠폰 적용 EXP 20에 붙어 +4
            action(190, A, "Alpha", MINER, ORE, 100, 10.0, exp_coupon=2, coupon_bonus=10.0, skill_bonus=4.0),
            action(15, A, "Alpha", am.ADVENTURER, "jobsplus:adventurer/move_5_blocks", 4, 3.3),
            action(20, A, "Alpha", MINER, ORE, 5, 3.0, mode="creative"),
            action(25, A, "Alpha", "jobsplus:hunter", "jobsplus:hunter/kill_mob", 3, 68.6, source="spawner"),
            action(30, B, "Admin", MINER, ORE, 50, 30.0),
        ]
        event_rows = [
            [T0 - 1000, "SERVER_START", "", "", "", VERSION, "", "", "", "", "", "", "schema=2"],
            [T0 + 120_000, "LEVEL_UP", A, "Alpha", MINER, "", "", 4, 5, 60, 80, 5, ""],
            [T0 + 250_000, "POWERUP_BUY", A, "Alpha", MINER, "jobsplus:miner/job_exp_i", 10, "", "", 80, 70, 5,
             "required_level=5;parent=;owned_after=1"],
            [T0 + 260_000, "POWERUP_BUY_FAILED", A, "Alpha", MINER, "jobsplus:miner/job_exp_ii", 20, "", "", 70,
             70, 5, "reason=level_too_low;required_level=15"],
            [T0 + 270_000, "ADMIN_SET_COINS", B, "Admin", "", "", "", "", "", 0, 999, "", "source=Server"],
            # 채굴 중(3분 구간) 총을 든 스켈레톤에게 사망
            [T0 + 150_000, "DEATH", A, "Alpha", "", "tacz:bullet", "minecraft:skeleton", "", "", "", "", "",
             "direct=tacz:bullet;dimension=minecraft:overworld;x=0;y=64;z=0;"
             "attacker_weapon=tacz:modern_kinetic_gun;attacker_gun=tacz:ak47;attacker_armor=minecraft:iron_chestplate"],
        ]
        write(self.directory / "actions.csv", ACTIONS_HEADER, action_rows)
        write(self.directory / "activity.csv", ACTIVITY_HEADER, activity_rows)
        write(self.directory / "events.csv", EVENTS_HEADER, event_rows)
        write(self.directory / "balance_rewards.csv", REWARDS_HEADER, [
            [VERSION, "job", MINER, MINER, ORE, "arc:on_break_block", 0, "jobsplus:job_exp", "100.0", 1, 0.658, 0.658,
             "", "", ""],
            [VERSION, "job", MINER, MINER, ORE, "arc:on_break_block", 1, "jobsplus:bitcoin_reward", "0.5", 1, "", "", 1,
             "", ""],
        ])
        write(self.directory / "balance_powerups.csv", POWERUPS_HEADER, [
            [VERSION, "jobsplus:miner/job_exp_i", MINER, 10, 5, "", "BASIC"],
        ])
        self.options = am.Options(exclude_players={"Admin"})
        self.result = am.analyze(am.load(self.directory), self.options)

    def tearDown(self):
        self.temp.cleanup()

    def job(self, job_id):
        return next(row for row in self.result["jobs"] if row["job_id"] == job_id)

    def test_active_time_excludes_afk_and_carries_travel_minute(self):
        miner = self.job(MINER)
        # 1~4분 채굴 + 5분 이동 = 300초, 6~10분은 잠수
        self.assertAlmostEqual(miner["hours"], 300 / 3600, places=3)
        player = next(row for row in self.result["players"] if row["player_name"] == "Alpha")
        self.assertAlmostEqual(player["afk_h"], 300 / 3600, places=3)
        self.assertAlmostEqual(player["online_h"], 600 / 3600, places=3)

    def test_experience_split_removes_coupon(self):
        miner = self.job(MINER)
        hours = 300 / 3600
        base = 65.8 * 3 + 10.0
        self.assertAlmostEqual(miner["exp_base_per_h"], round(base / hours, 3), places=2)
        # 쿠폰이 없었다면 스킬 보너스는 4 / 2 = 2
        self.assertAlmostEqual(miner["exp_actual_no_coupon_per_h"], round((base + 2.0) / hours, 3), places=2)
        self.assertAlmostEqual(miner["coupon_exp_share"], round(10.0 / (base + 10.0 + 4.0), 3), places=3)

    def test_expected_bitcoin_uses_counts_and_chance(self):
        miner = self.job(MINER)
        # 400회 × 0.5% × 1개 = 2개
        self.assertAlmostEqual(miner["btc_expected_per_h"], round(2.0 / (300 / 3600), 3), places=2)
        self.assertEqual(miner["btc_actual"], 1)
        self.assertAlmostEqual(miner["btc_actual_vs_expected"], 0.5, places=3)

    def test_filters(self):
        warnings = " ".join(row["warning"] for row in self.result["warnings"])
        self.assertIn("생존 모드가 아닌 액션 행 1개", warnings)
        self.assertEqual(len(self.result["excluded_sources"]), 1)
        self.assertEqual(self.result["excluded_sources"][0]["source"], "spawner")
        self.assertEqual(self.job(MINER)["players"], 1)
        # 제외한 관리자 계정은 경고에서도 빠지고, 제외하지 않으면 관리자 명령 대상으로 알린다.
        self.assertNotIn("Admin", warnings)
        unfiltered = am.analyze(am.load(self.directory), am.Options())
        self.assertIn("Admin", " ".join(row["warning"] for row in unfiltered["warnings"]))
        self.assertEqual(next(row for row in unfiltered["jobs"] if row["job_id"] == MINER)["players"], 2)

    def test_adventurer_passive_is_not_allocated_time(self):
        adventurer = self.job(am.ADVENTURER)
        self.assertEqual(adventurer["hours"], 0)

    def test_timelines_and_coin_flow(self):
        level = self.result["level_timeline"][0]
        self.assertEqual(level["level"], 5)
        self.assertAlmostEqual(level["job_hours"], 120 / 3600, places=3)
        powerup = self.result["powerup_timeline"][0]
        self.assertTrue(powerup["completed"])
        self.assertEqual(powerup["total_powerups"], 1)
        coins = self.result["coin_flow"][0]
        self.assertEqual(coins["coins_earned"], 20)
        self.assertEqual(coins["coins_spent"], 10)
        self.assertEqual(coins["funded_by_other_jobs"], 0)
        failure = self.result["powerup_failures"][0]
        self.assertEqual(failure["reason"], "level_too_low")

    def test_stage_boundaries(self):
        options = am.Options(stage_levels=(32, 64))
        self.assertEqual(am.stage_of(31, options), "early")
        self.assertEqual(am.stage_of(32, options), "mid")
        self.assertEqual(am.stage_of(64, options), "late")

    def test_minute_outside_gap_stays_unassigned(self):
        minutes = [am.Minute(0, 60_000, False, weights={MINER: 1.0}),
                   am.Minute(60_000, 120_000, False),
                   am.Minute(20 * 60_000, 21 * 60_000, False)]
        am.mark_sessions(minutes)
        am.allocate(minutes, am.Options(gap_seconds=600))
        self.assertIn(MINER, minutes[1].allocation)
        self.assertIn(am.UNASSIGNED, minutes[2].allocation)

    def test_deaths_attributed_to_working_job(self):
        miner = self.job(MINER)
        self.assertEqual(miner["deaths"], 1)
        self.assertAlmostEqual(miner["deaths_per_h"], round(1 / (300 / 3600), 3), places=3)
        death = self.result["deaths"][0]
        self.assertEqual(death["job_id"], MINER)
        self.assertEqual(death["attacker"], "minecraft:skeleton")
        self.assertEqual(death["attacker_weapon"], "tacz:ak47")
        player = next(row for row in self.result["players"] if row["player_name"] == "Alpha")
        self.assertEqual(player["deaths"], 1)

    def test_single_job_session_fills_preparation(self):
        # 0분에만 채굴하고 24분 동안 액션 없이 활동: 10분 밖이라도 직업이 하나뿐인 세션이면 채굴 준비로 본다.
        minutes = [am.Minute(index * 60_000, (index + 1) * 60_000, False) for index in range(25)]
        minutes[0].weights = {MINER: 1.0}
        am.mark_sessions(minutes)
        am.allocate(minutes, am.Options(gap_seconds=600))
        self.assertIn(MINER, minutes[20].allocation)

        strict = [am.Minute(index * 60_000, (index + 1) * 60_000, False) for index in range(25)]
        strict[0].weights = {MINER: 1.0}
        am.mark_sessions(strict)
        am.allocate(strict, am.Options(gap_seconds=600, session_fill=False))
        self.assertIn(am.UNASSIGNED, strict[20].allocation)

    def test_multi_job_session_leaves_far_minutes_unassigned(self):
        # 채굴과 사냥 사이 12분 지점은 양쪽 모두 10분 밖이라 어느 직업의 준비인지 알 수 없다.
        minutes = [am.Minute(index * 60_000, (index + 1) * 60_000, False) for index in range(25)]
        minutes[0].weights = {MINER: 1.0}
        minutes[24].weights = {"jobsplus:hunter": 1.0}
        am.mark_sessions(minutes)
        am.allocate(minutes, am.Options(gap_seconds=600))
        self.assertIn(am.UNASSIGNED, minutes[12].allocation)
        self.assertIn("jobsplus:hunter", minutes[20].allocation)

    def test_main_writes_outputs(self):
        out = self.directory / "out"
        code = am.main([str(self.directory), "--out", str(out), "--exclude", "Admin"])
        self.assertEqual(code, 0)
        self.assertTrue((out / "jobs.csv").exists())
        self.assertTrue((out / "actions.csv").exists())
        self.assertTrue((out / "deaths.csv").exists())


if __name__ == "__main__":
    unittest.main()
