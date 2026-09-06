# Server Utilities — Minecraft 26.2

서버 공통 수면·전투·밸런스·사망 규칙을 관리하는 Fabric 모듈입니다. 기존 Jobs+의 전투·사망 처리와 Advanced Netherite의 크리퍼 피해·행동 피로도 보정을 이 모듈로 옮겼습니다.

## 설치

- Minecraft 26.2, Java 25, Fabric Loader 0.19.3 이상, Fabric API 0.154.2+26.2 이상이 필요합니다.
- 전용 서버의 `mods`에 설치합니다. 원격 접속 클라이언트에는 이 모듈이 필수가 아닙니다.
- 싱글플레이·LAN 호스트에서도 적용하려면 해당 클라이언트에 설치합니다. `environment: "*"`로 로드하며 게임 규칙은 서버에서만 처리합니다.
- **이 작업에서 빌드한 Jobs+·Advanced Netherite·Server Utilities를 함께 교체하세요.** 이전 Jobs+·Advanced Netherite의 규칙 클래스가 발견되면 중복 적용을 막기 위해 기동을 중단합니다.
- Jobs+·Advanced Netherite·TACZ 등 기존 모드의 클라이언트 설치 요구는 그대로입니다.
- Server Utilities 자체는 Jobs+·Advanced Netherite·ArcLib·YamlConfig·UILib에 의존하지 않습니다. 보존권은 기존 아이템 ID로 찾아 사용하므로 Advanced Netherite가 없으면 보존권 소비 없이 설정된 사망 손실 정책을 적용합니다.

## 설정과 명령어

서버 시작 시 `config/serverutilities.properties`를 생성합니다. 수정 후 관리자 또는 서버 콘솔에서 다음 명령을 실행합니다. 콘솔에서는 앞의 `/`를 생략합니다.

```text
/serverutilities status
/serverutilities reload
```

`status`는 설정과 실제 수면 비율을 표시하고, `reload`는 설정을 검증한 뒤 다시 적용합니다. 이벤트를 재등록하지 않습니다. 알 수 없는 키·잘못된 불리언·범위를 벗어난 숫자는 오류로 처리하며 설정 파일을 기본값으로 덮어쓰지 않습니다. 기동 중 오류는 로그에 기록하고 기동을 중단합니다. reload 오류 시 새 설정 객체를 활성화하지 않습니다.

| 키 | 기본값 | 허용값 | 동작 |
|---|---|---|---|
| `sleep.enabled` | `true` | `true` / `false` | 한 명이 정상적으로 잠들면 밤을 넘기도록 수면 비율을 0으로 설정 |
| `combat.elytra.enabled` | `true` | `true` / `false` | 습격 중 또는 위더 근처에서 활강 시 즉사 |
| `combat.golems.enabled` | `true` | `true` / `false` | 같은 전투 조건에서 주변 철골렘 제거 |
| `combat.range` | `80.0` | 1~128 | 위더 탐색·철골렘 제거 범위. 플레이어 경계 상자를 각 축으로 확장하는 AABB |
| `balance.creeper.enabled` | `true` | `true` / `false` | 크리퍼 폭발의 엔티티 피해 보정 |
| `balance.creeper.multiplier` | `1.3333334` | 0~100, 유한수 | 기존 `4/3`배와 같은 float 값. 모든 난이도에 적용, 지형 파괴 반경은 변경하지 않음 |
| `balance.hunger.enabled` | `true` | `true` / `false` | 행동으로 발생하는 피로도 보정 |
| `balance.hunger.multiplier` | `1.5` | 0~100, 유한수 | 행동 피로도 배율. 자연 회복이 직접 추가하는 피로도에는 미적용 |
| `death.penalty.enabled` | `true` | `true` / `false` | 보존권 보호가 없으면 사망 시 비어 있지 않은 슬롯 하나 전체 삭제 |
| `death.protection.enabled` | `true` | `true` / `false` | 사망 시 보존권 1개를 소비하고 인벤토리 보호 |

기존 전투 규칙의 크리에이티브·관전자 예외 정책은 바꾸지 않았습니다. 사망 손실·보존권 소비는 기존처럼 크리에이티브·관전자를 제외합니다. 사망 손실과 보존권은 각각 켜고 끌 수 있고, 두 설정 모두 끄면 신규 사망에 대해 이 모듈이 아이템을 변경하지 않습니다. 이미 성립한 보호 상태는 설정을 꺼도 리스폰까지 복원합니다.

`keep_inventory`가 켜져 있어도 사망 손실·보존권 소비는 기존처럼 별도로 적용됩니다. 손실이 꺼져 있고 보존권도 없으면 아이템 유지 여부는 바닐라 게임 규칙을 따릅니다.

## 수면 규칙 보존·복원

26.2의 `ServerLevel.getGameRules()`는 서버의 공유 규칙을 사용합니다. 처음 활성화할 때 해당 월드 루트의 `serverutilities-sleep.properties`에 이전 `players_sleeping_percentage` 값을 기록하고, 실제 값을 0으로 바꿉니다. 재시작·reload 시 최초 값을 덮어쓰지 않습니다.

`sleep.enabled=false`로 바꾼 뒤 reload하면 현재 값이 관리 값인 0일 때 원래 값을 복원합니다. 관리자가 별도로 다른 숫자를 설정했다면 그 값을 유지합니다. 복원이 끝나면 관리 기록을 제거합니다. 활성화 상태에서 reload하면 수면 비율은 다시 0으로 적용됩니다.

복원 기록이 손상되면 그대로 보존하고 적용을 중단합니다. 기록 저장 실패 시 게임 규칙을 바꾸지 않습니다. 비활성화 과정에서 기록 삭제가 실패하면 규칙은 이미 복원되었을 수 있으므로 `status`와 로그를 확인하고 다시 reload합니다.

이 기능은 바닐라 수면 조건을 따릅니다. 잠자는 사람이 없거나 잠을 잘 수 없는 상황을 무시하고 강제로 아침으로 만들지 않습니다. 시간 진행·날씨 게임 규칙은 변경하지 않습니다.

## 사망 데이터 호환

- 보존권 ID는 `advancednetherite:death_item_protection_scroll`입니다. 아이템 등록·레시피·텍스처는 Advanced Netherite에 유지했습니다.
- 보호 상태는 이전과 같은 플레이어 저장 키 `JobsPlusDeathItemProtected`를 읽고 씁니다.
- 실제 사망의 아이템 드롭 직전에 한 번 처리합니다. 보호된 인벤토리는 드롭을 취소하고 `restoreFrom`에서 복원합니다.
- 사망 화면에서 접속을 끊거나 서버를 재시작하는 경우를 위해 보호 상태를 저장합니다.
- 사망·전투 공지는 서버가 완성한 한글 문장으로 보냅니다. 신규 모듈의 클라이언트 언어 파일이 필요하지 않습니다.
- 기존 직업·코인·주식·추가 직업 슬롯 데이터는 Jobs+가 계속 처리합니다.

## 빌드와 검증

Java 25 환경에서 모듈 폴더 안에서 실행합니다.

```powershell
.\gradlew.bat build
```

릴리스 파일은 `build/libs/ServerUtilities-26.2-1.0.0.jar`입니다. `build`의 `check`에서 외부 테스트 라이브러리 없이 설정·수면 복원 회귀 검사를 실행합니다.

작업 공간 루트에서 수집기 회귀 검사는 다음과 같습니다. 임시 폴더와 대체 빌드 함수를 사용하므로 실제 수집 폴더나 전체 모드를 빌드하지 않습니다.

```powershell
python -m unittest discover -s Build_File -p test_collect_fabric_jars.py
```

전체 빌드·수집은 `python Build_File/collect_fabric_jars.py`로 수행합니다. 대상은 11개이며, 빌드·검증·임시 복사가 성공한 후 결과를 교체합니다.

실제 게임에서 확인할 항목:

- [ ] 여러 플레이어 중 한 명 수면, 수면 취소, 낮·시간 진행 비활성 조건
- [ ] 재시작·reload·기능 비활성화 후 수면 원래 값 복원
- [ ] 습격/위더 각각의 활강 즉사·철골렘 제거와 각 기능 off 상태
- [ ] 동일 조건의 크리퍼 피해·행동 피로도 이동 전후 비교
- [ ] 보존권 유무·빈 인벤토리·장비·보조손·`keep_inventory` 조합
- [ ] 전투 규칙에 의한 사망, 사망 화면에서 재접속·재시작 후 복원
- [ ] 새 모듈 없는 원격 클라이언트 접속·메시지 표시
- [ ] 기존 직업·주식·펫·청크 보호·총기 동작

## 배포와 롤백 단위

| 변경 단위 | 함께 관리할 파일 |
|---|---|
| 모듈 기반·수면 | 신규 Gradle 구성·설정·진입점·수면 관리자 |
| 전투 | 신규 `combat`와 Jobs+의 이전 전투 등록·클래스 제거 |
| 밸런스 | 신규 피해·피로도 Mixin과 Advanced Netherite의 이전 등록·클래스 제거 |
| 사망 | 신규 `death`·보호 Mixin과 Jobs+의 이전 이벤트·상태·저장·복원 제거 |
| 빌드 수집 | collector·테스트·배포 의존성 문서 |

각 기능 추가와 기존 모드의 해당 구현 제거를 같은 커밋으로 관리합니다. 수면 기반 커밋 위에 전투, 밸런스, 사망 처리를 순서대로 적용하며 빌드 수집 연결과 수집 실패 복구는 별도 커밋으로 관리합니다. 전체 기능을 되돌릴 때는 의존하는 변경부터 역순으로 되돌립니다.

전체 이전 버전으로 되돌릴 때:

1. `sleep.enabled=false`로 수정하고 `/serverutilities reload` 및 `status`로 복원을 확인합니다.
2. 서버를 종료합니다.
3. Server Utilities JAR을 제거하고 대응하는 이전 Jobs+·Advanced Netherite JAR을 함께 복원합니다.
4. 이미 사망 화면에 있는 플레이어의 보존 상태가 있으면, 같은 저장 키를 지원하는 이전 Jobs+로 복원합니다.

JAR만 제거하면 수면 비율 0은 월드에 남을 수 있습니다. 미리 복원하지 못했다면 기록된 원래 숫자로 `/gamerule minecraft:players_sleeping_percentage <원래값>`을 적용합니다. 기능이 실행되어 이미 삭제된 아이템은 코드 롤백으로 복구되지 않습니다.

청크 보호 분리, 재파종 대기열 개선, 탐색 성능 최적화, 일반 버전 범위 파서 개선은 이번 구현에 포함하지 않았습니다.
