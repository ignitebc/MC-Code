# 직업 밸런스 메트릭

실제 서버 플레이에서 직업별 Action/h, EXP/h, BTC/h, 레벨·스킬 성장 속도를 측정하기 위한 기록입니다.
시즌 밸런스 패치 전에 이 기록과 30-Agent 시뮬레이션을 비교합니다.

> [!IMPORTANT]
> Action별 기록은 ArcLib의 `REWARDS_APPLYING`·`REWARDS_APPLIED` 이벤트를 씁니다. ArcLib과 Jobs+ JAR을 **함께** 교체해야 합니다.
> 이전 형식 `logs/jobsplus-metrics/jobsplus_metrics.csv`에는 더 이상 기록하지 않습니다.

## 저장 위치와 시즌 설정

```text
logs/jobsplus-metrics/v2/<시즌>/
├─ actions.csv            5초 × 플레이어 × 직업 Action별 횟수·EXP·BTC
├─ events.csv             접속·레벨업·스킬 구매·쿠폰·관리자 명령 등
├─ snapshots.csv          접속·종료·1시간마다 직업 상태
├─ activity.csv           1분마다 위치·입력 흔적·잠수 여부
├─ balance_rewards.csv    밸런스 버전별 실제 적용 보상표
└─ balance_powerups.csv   밸런스 버전별 스킬 가격·요구 레벨·선행 스킬
```

시즌 폴더 이름은 `config/jobsplus-common.yaml`의 `metrics.season`으로 정합니다. 비우면 `unspecified`입니다.
**시즌을 열기 전에** 새 이름(예: `season3`)을 넣어 두면 이전 시즌 기록과 섞이지 않습니다.

파일은 1분마다 추가 저장합니다. 서버가 비정상 종료되면 마지막 1분 이내 기록이 빠질 수 있습니다.
파일 헤더가 현재 형식과 다르면 기존 파일을 `이름-old-<시각>.csv`로 옮기고 새 파일을 시작합니다.

## actions.csv

직업 Action이 조건을 통과해 보상을 적용할 때마다 1회로 셉니다. EXP 배율 스킬 보너스처럼 그 보상 안에서 생긴 EXP도 같은 Action 행에 붙습니다.
아래 열이 하나라도 다르면 다른 행으로 나뉩니다.

| 열 | 설명 |
| --- | --- |
| `bucket_start_ms`, `bucket_ms` | 5초 버킷 시작 시각(UTC epoch ms)과 길이 |
| `job_id`, `action_id`, `trigger` | 직업, Action 파일 ID, Arc 트리거 |
| `source` | 대상 몹의 스폰 원인. 몹이 없는 Action은 빈 값 |
| `job_level` | Action 시작 시점의 직업 레벨 |
| `game_mode` | `survival`, `creative` 등 |
| `exp_coupon_multiplier`, `btc_coupon_multiplier` | 그 순간의 EXP 쿠폰 배율, BTC 확률 쿠폰 배율 |
| `balance_version` | 적용 중인 보상표 해시 |
| `count` | Action 실행 횟수 |
| `exp_base` | 쿠폰·스킬을 뺀 기본 EXP |
| `exp_coupon_bonus` | 쿠폰으로 늘어난 EXP |
| `exp_skill_bonus` | EXP 배율 스킬 보너스. 쿠폰이 곱해진 EXP에 붙은 값 |
| `exp_total` | 실제 지급 EXP 합 |
| `btc` | 실제 지급 BTC 개수 |

`action_id`가 빈 행은 직업 Action 밖에서 지급된 보상입니다. 정상 경로에서는 생기지 않으므로 생기면 원인을 확인합니다.
최대 레벨(200)에서 쌓이지 않은 EXP는 기록하지 않습니다.

## events.csv

| event | 내용 |
| --- | --- |
| `SERVER_START`, `SERVER_STOP` | 서버 시작·정상 종료. `detail`에 스키마·시즌·Minecraft·Jobs+·Arc 버전 |
| `BALANCE_VERSION` | 보상표 버전 확인·변경. `/reload` 후에도 다시 계산 |
| `LOGIN`, `LOGOUT`, `ONLINE` | 접속·종료, 5분마다 접속 중 표시 |
| `LEVEL_UP` | 직업 레벨업. 레벨 전후와 코인 전후 |
| `POWERUP_BUY` | 스킬 구매. 가격, 코인 전후, 직업 레벨, 요구 레벨, 선행 스킬, 구매 후 보유 수 |
| `POWERUP_BUY_FAILED` | 구매 실패. `reason`이 `not_enough_coins`, `level_too_low`, `parent_missing` |
| `POWERUP_TOGGLE` | 스킬 켜기·끄기 |
| `JOB_START`, `JOB_SLOT_ADD` | 직업 선택, 직업선택권으로 최대 직업 수 증가 |
| `COUPON_USE` | 쿠폰 종류·배율·만료 시각 |
| `COIN_REWARD` | 직업 코인 보상(현재 데이터에서는 사용하지 않음) |
| `ADMIN_*` | `/job set` 명령으로 바뀐 레벨·EXP·코인·스킬, 직업 추가·삭제. `detail`의 `source`가 실행자 |

## snapshots.csv

접속·종료·서버 종료 시와 접속 중 1시간마다 직업마다 한 행을 씁니다.
직업 레벨·EXP, 다음 레벨 필요 EXP, 보유·활성 스킬 수와 활성 스킬 ID, 코인, 최대 직업 수, 게임 모드, 관리자 권한 여부를 담습니다.
기록이 시즌 도중부터 시작돼도 그 시점의 상태를 바로 알 수 있습니다.

## activity.csv와 잠수 판정

1분마다(접속 종료 때는 남은 구간) 한 행을 씁니다. `sample_seconds`가 그 행이 덮는 시간입니다.

- 입력 흔적: 바닐라가 키 입력·공격·상호작용·컨테이너 클릭 때 갱신하는 마지막 행동 시각, 시점 회전, 메뉴 변화, 직업 Action
- `idle_seconds`: 마지막 입력 흔적 이후 지난 시간
- `afk`: `idle_seconds`가 300초 이상이면 `true`
- 이동 거리는 물살·밀림으로도 늘어나므로 입력 흔적으로 보지 않습니다. 한 틱 16블록을 넘는 이동은 `teleports`로 셉니다.

## 몹 스폰 원인

몹이 처음 생성될 때 `jobsplus.spawn.<원인>` 엔티티 태그를 붙입니다(`natural`, `spawner`, `trial_spawner`, `spawn_item_use`, `breeding`, `command` 등).
태그는 몹 저장 데이터에 남습니다. 이 기능 이전에 생긴 몹은 `unknown`입니다.

## 집계 스크립트

```bash
python JobsPlusRemastered-26.2/tools/metrics/analyze_metrics.py <시즌 폴더> --exclude SERVER_ADMIN__ --since 2026-10-01
```

| 옵션 | 기본값 | 설명 |
| --- | --- | --- |
| `--exclude` | 없음 | 제외할 플레이어 이름 또는 UUID. 여러 번 지정 |
| `--since`, `--until` | 전체 | KST 날짜 범위 |
| `--balance` | 전체 | 이 밸런스 버전만 집계 |
| `--include-non-survival` | 꺼짐 | 크리에이티브·관전 기록 포함 |
| `--exclude-sources` | `spawner,trial_spawner` | 기본 집계에서 뺄 스폰 원인 |
| `--afk-seconds` | 300 | 잠수 기준 |
| `--gap-minutes` | 10 | 직업 Action 없는 활동 구간을 붙일 최대 거리 |
| `--stages` | `32,64` | 초반/중반, 중반/후반을 나누는 직업 레벨 |

결과는 `<시즌 폴더>/analysis`에 CSV로 쓰고 직업 요약 표를 출력합니다.

| 파일 | 내용 |
| --- | --- |
| `jobs.csv` | 직업별 작업시간, Action/h, 기본·실제 EXP/h, 기대·실제 BTC/h, 구간별 값 |
| `stages.csv`, `actions.csv` | 초·중·후반별, Action별 같은 지표와 Action의 EXP 비중 |
| `players.csv` | 플레이어별 접속·잠수·활동 시간과 직업별 배분 시간 |
| `level_timeline.csv`, `powerup_timeline.csv` | 레벨업·스킬 구매 시점의 누적 직업 작업시간, 전체 스킬 해방 여부 |
| `powerup_failures.csv` | 스킬 구매 실패 사유별 횟수 |
| `coin_flow.csv` | 직업별 획득·사용 코인과 다른 직업 코인으로 산 몫 |
| `excluded_sources.csv` | 기본 집계에서 뺀 스포너 처치 기록 |
| `warnings.csv` | 데이터 품질 경고 |

### 지표 정의

- **작업시간**: 잠수 구간을 뺀 활동시간. 잠수는 `idle_seconds`만큼 거슬러 올라가 앞 구간까지 포함합니다.
  직업 Action이 있는 1분은 비모험가 직업들의 기본 EXP 비율로 나누고, Action 없는 1분(이동·탐색·재료 준비)은
  같은 세션에서 10분 이내 가장 가까운 직업 구간에 붙입니다. 붙일 곳이 없으면 모험가 이동 구간이면 모험가, 아니면 미배분입니다.
- **기본 EXP/h**: `exp_base` 합 ÷ 작업시간. 쿠폰·스킬 제외.
- **실제 EXP/h(쿠폰 제외)**: 기본 EXP + 쿠폰 배율로 나눈 스킬 보너스.
- **기대 BTC/h**: Action 횟수 × 보상표 확률 × 지급 개수 ÷ 작업시간. 쿠폰 제외. BTC는 드물게 나와 실제 지급량의 오차가 크므로 이 값을 주 지표로 씁니다.
- **실제/기대**: 쿠폰 배율까지 반영한 기대값 대비 실제 지급. 1에서 크게 벗어나면 확률 적용을 확인합니다.
- **모험가**: 다른 직업 중 이동으로 쌓인 EXP가 섞이므로 `note`의 전용 구간 EXP/h를 따로 봅니다.

## 표본 크기

기대 BTC 건수별 95% 오차입니다(`jobs.csv`의 `btc_expected_95pct_error`).

| 직업당 BTC 건수 | 오차 | 6 BTC/h 기준 필요 시간 |
| --: | --: | --: |
| 25 | ±39% | 약 4h |
| 100 | ±20% | 약 17h |
| 385 | ±10% | 약 64h |

## 운영 절차

| 시점 | 할 일 |
| --- | --- |
| 시즌 오픈 전 | ArcLib·Jobs+를 함께 배포하고 `metrics.season`에 새 시즌 이름 입력 |
| 시즌 오픈 전 | 테스트 서버에서 직업별 대표 Action 1회씩 실행 후 `actions.csv` 행 확인 |
| 시즌 오픈 전 | 관리자·테스트 계정 이름을 정리해 집계 때 `--exclude`로 지정 |
| 시즌 중 | 수치 패치 후 `events.csv`에 새 `BALANCE_VERSION`이 찍혔는지 확인 |
| 시즌 중 | 주 1회 시즌 폴더·`world/playerdata`·서버 로그 백업, 집계 스크립트로 직업별 표본시간 점검 |
| 시즌 종료 후 | 밸런스 버전별로 나눠 집계하고 `warnings.csv` 확인 후 시뮬레이션과 비교 |
