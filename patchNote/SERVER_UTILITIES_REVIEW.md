# 서버 공통 기능 분리 검토표 및 실행 체크리스트

검토일: 2026-09-06 · 작업 브랜치: `26.3` · 대상 Minecraft: `26.2`

## 1. 검토 결과와 범위

서버 공통 규칙을 `ServerUtilities-26.2`라는 독립 Fabric 모듈로 모으는 것이 적절하다. 실제 분리 후보는 Jobs+의 전투·사망 규칙과 Advanced Netherite의 크리퍼 피해·배고픔 보정이다. 한 명 수면 기능은 새 모듈에서 바닐라 게임 규칙을 관리하는 방식으로 추가한다.

이 문서는 구현 전 검토 결과와 이후 진행 상태를 함께 기록한다. 2026-09-06 후속 구현에서 U01~U07, B01~B02를 반영했고, B03은 설치 대상 문서화로 반영했다. 아래 현재 동작·줄 번호는 검토 당시 기준이며, 이동한 소스 링크는 새 위치로 연결했다. 실제 설정·설치·롤백 방법은 [Server Utilities 운영 안내](../ServerUtilities-26.2/README.md)를 따른다. 로컬 커밋은 수면 기반·전투·밸런스·사망·빌드 연결·수집 실패 복구·문서로 분리한다.

검토 범위는 10개 모듈의 `src` 아래 Java 1,718개 파일 목록·패턴 검사와 관련 진입점, 명령어, 이벤트, Mixin, 설정, 저장·리스폰 연결 및 빌드 수집 흐름의 상세 검토다. `build`, `bin`, `.gradle`, `.git`은 제외했다. 모든 Java 파일의 모든 줄을 수작업으로 감사하거나 모든 게임 기능을 실행 검증했다는 의미는 아니다. 설치된 실제 서버의 플러그인·데이터팩·게임 규칙은 이 소스 저장소만으로 확인할 수 없다.

소스에서 Bukkit/Paper API 참조, `src` 아래 `plugin.yml`, `paper-plugin.yml`, `.mcfunction`은 발견되지 않았다. caramelChat의 `plugin` 패키지는 다른 클라이언트 모드와의 Mixin 호환 처리다([S10]). 서버 운영 기능과는 별개다.

## 2. 전체 모듈 분류

| 모듈 | Java 파일 | 확인한 역할·연결 | 분리 판단 |
|---|---:|---|---|
| AdvancedNetherite | 93 | 장비·아이템·펫, 크리퍼 피해와 배고픔 Mixin | 크리퍼·배고픔 규칙만 우선 분리. 장비 착용 효과, 펫·아이템 등록은 유지 |
| ArcLib | 238 | 액션·조건·보상, 플레이어 상태, `/arc` 화면 명령 | 공통 라이브러리 역할 유지. 운영 정책을 이 라이브러리에 추가하지 않음 |
| FallingTree | 119 | 벌목·잎 처리, `/fallingtree toggle` | 벌목 기능과 명령을 함께 유지 |
| ItemRestrictions | 47 | 직업별 사용 제한, 청크 구매·소유권 저장, 블록·메뉴 보호 | 사용 제한 유지. 청크 보호는 후속 분리 후보로만 기록 |
| JobsPlusRemastered | 174 | 직업·보상·주식·쿠폰, 전투 규칙, 사망 규칙 | 전투 규칙 우선 분리. 사망 규칙은 저장·리스폰까지 묶어 후속 분리 |
| TACZ-Refabricated | 709 | 총기·탄약·부착물, 총기 팩, `/tacz`, 네트워크·클라이언트 표현 | 모두 TACZ에 유지. 언어·탄약상자 변경과 운영 모듈 작업을 분리 |
| UILib | 63 | 공통 화면·위젯 | 유지. 새 운영 모듈에 UI 의존성 추가 불필요 |
| YamlConfig | 125 | 설정 직렬화·GUI·동기화·서버 설정 | 유지. 새 모듈의 단순 운영 설정에 사용할지는 의존성 비용을 고려 |
| caramelChat | 43 | 한글 입력·IME, EMI/Xaero 호환 Mixin | 클라이언트 전용 유지 |
| Illager Invasion | 107 | 몹·습격·주민 행동·전리품·물약 | 콘텐츠 모듈에 유지. 새 전투 규칙과 상호작용 검증 |
| **합계** | **1,718** | **10개 모듈** | **신규 모듈 도입 시 빌드 수집 대상은 11개** |

## 3. 기능별 수정 대상

아래 우선순위는 구현 순서다. 현재 확정된 결함, 구조 개선, 새 기능을 구분했다.

| ID | 우선순위·구분 | 현재 동작과 근거 | 필요한 변경 | 함께 확인할 부분 |
|---|---|---|---|---|
| U01 | 1 · 신규 | 공통 운영 모듈이 없음. 각 콘텐츠 모드가 직접 정책을 등록 | 독립 Gradle 프로젝트·Fabric 진입점·설정·관리 명령 생성 | MC 26.2 / Java 25, 독립 빌드, 기존 모드로 향하는 불필요한 의존성 없음 |
| U02 | 1 · 신규 | 소스에서 한 명 수면 정책 적용 코드를 찾지 못함 | 바닐라 `minecraft:players_sleeping_percentage`를 0으로 관리 | 기존 값 저장·복원, 재시작, 기능 비활성화, 잠들 수 없는 상황 |
| U03 | 2 · 구조 | Jobs+가 전투 중 활강 플레이어를 즉사시킴([S1], [S2]) | `combat` 기능으로 이동하고 개별 활성화 설정 추가 | 기존 등록 제거, 사망 처리 연쇄, 생존·크리에이티브 처리 정책 |
| U04 | 2 · 구조 | 같은 이벤트가 습격 또는 위더 근처 철골렘도 제거([S2]) | 전투 정책 안에서 겉날개 제한과 별도 설정으로 구분 | 플레이어 기준 80블록 AABB라는 실제 범위 유지, 중복 탐색, 청크 보호와 상호작용 |
| U05 | 2 · 구조 | Advanced Netherite가 크리퍼 폭발의 엔티티 피해를 `4/3`배로 보정([S3]) | `balance` 기능으로 이동하고 배율 설정 추가 | 기존 Mixin 등록 제거. 어려움 전용 조건은 현재 없으므로 다른 난이도도 검증 |
| U06 | 2 · 구조 | 행동으로 발생하는 피로도를 `1.5`배로 보정([S4]) | `balance` 기능으로 이동하고 배율 설정 추가 | 서버 권위로 적용. 자연 회복 피로도와 구분, 중복 Mixin 제거 |
| U07 | 3 · 구조·저장 호환 | 사망 시 한 슬롯 전체 삭제. 보존권이 있으면 1개 소비 후 인벤토리 보호([S5]) | `death` 기능으로 이벤트·보호 상태·드롭 취소·리스폰 복원·NBT 저장을 함께 분리 | Advanced Netherite 보존권 ID 유지, 기존 NBT 읽기, 사망 화면에서 재접속·재시작 |
| U08 | 후속 · 범위 큼 | 청크 구매 문서, `chunk_owners.json`, 이벤트·다수 Mixin으로 보호([S7]) | 당장은 ItemRestrictions 유지. 독립적인 땅 보호 모듈로 뺄지 후속 검토 | 소유권·UUID·차원 키·백업 파일·피스톤·호퍼·유체·폭발 보호 전체 |
| B01 | 1 · 필수 연결 | collector의 `MODULES`에 현재 10개 모듈만 존재([S8]) | `("ServerUtilities-26.2", None, "serverutilities")` 추가, 배포 문서 11개로 갱신 | 루트 `build` 작업, `build/libs`의 릴리스 JAR, 메타데이터 ID 검증 |
| B02 | 별도 수정 · 확인된 동작 문제 | 기존 수집 폴더를 비운 뒤 전체 빌드를 실행. 빌드 실패 시 이전 수집본이 사라짐([S8] 140행, 328행) | 임시 수집 경로에서 빌드 결과를 검증한 뒤 기존 결과 교체 | 실패 시 이전 JAR 유지, 교체 실패 복구, 대상 경로 검증 유지 |
| B03 | 선택 · 배포 개선 | client/server 표시는 출력에만 쓰며 JAR은 한 폴더에 수집([S8]) | 설치 대상 목록을 명시하거나 서버·클라이언트별 수집 옵션 제공 | caramelChat은 클라이언트 전용. `environment`만으로 클라이언트 설치 필수 여부까지 판단하지 않음 |

## 4. 이동 시 반드시 처리할 연결

### 4.1 전투 규칙

`JobsPlus.registerEvents()`의 등록 → `EventKillElytraDuringRaidOrWither`의 플레이어 틱 → 습격/위더 탐색 → 철골렘 제거·활강 플레이어 즉사 순서다.

현재는 기능 토글이 없고, 살아 있는 서버 플레이어를 매 틱 검사한다. 습격이 없으면 플레이어마다 위더 범위 탐색을 수행한다. 인원이 겹치는 지역의 반복 탐색 비용은 존재하지만 실제 성능 저하 정도는 프로파일링 전에는 단정할 수 없다. 우선 동작을 보존해 이동하고 탐색 간격·캐시는 별도 성능 변경으로 검토한다.

현재 크리에이티브·관전자에 대한 명시적 제외 조건이 없다. 해당 모드에서 어디까지 적용해야 하는지는 정책 검증 항목이다. 분리 작업 중 임의로 즉사를 비행 해제로 바꾸거나 범위를 변경하지 않는다.

### 4.2 크리퍼 피해와 피로도

Java 파일만 이동하면 기존 `advancednetherite.mixins.json`의 두 항목이 남는다. 기존 등록 제거와 신규 등록을 같은 기능 변경 단위로 처리해야 한다([S3], [S4]).

두 구현을 동시에 실행하면 피해 보정은 `4/3 × 4/3`, 피로도는 `1.5 × 1.5`가 될 수 있다. 이전 모듈의 JAR과 새 모듈의 JAR을 함께 배포하는 버전 조합까지 검증한다.

`EnderManMixin`, `PhantomMixin`은 Advanced Netherite 방어구 착용 여부를 확인하므로 원래 모듈에 남긴다.

### 4.3 사망·아이템 보존

| 연결 지점 | 현재 책임 | 분리 시 처리 |
|---|---|---|
| `EventDeleteRandomItemOnDeath` | 보존권 소비, 슬롯 전체 삭제, 공지 | 새 사망 정책으로 이동 |
| `JobsServerPlayer` | 사망 보호 상태 접근 API | 새 모듈 자체 상태로 독립. 직업 데이터 API 전체를 이동하지 않음 |
| `MixinPlayerDeathItemProtection` | 보호 중 `dropEquipment` 취소 | 새 보호 상태와 연결 |
| `MixinServerPlayer.restoreFrom` | 사망 전 인벤토리 복원·플래그 해제 | 기존 복원 로직과 중복되지 않게 이동 |
| `addAdditionalSaveData` / `readAdditionalSaveData` | `JobsPlusDeathItemProtected` 저장·읽기 | 롤백 호환을 위해 초기에는 기존 저장 키 유지 권장 |
| Advanced Netherite 등록 아이템 | `advancednetherite:death_item_protection_scroll` | 아이템·레시피·ID는 원래 모듈 유지. 새 모듈에서는 선택적 레지스트리 조회 |
| Jobs+ 언어 리소스 | `jobsplus.death.*` 메시지 | 서버에서 완성한 메시지 또는 번역 대체 문구 사용. 신규 모듈 미설치 클라이언트에서 키가 노출되지 않아야 함 |

사망 이벤트만 먼저 옮기면 드롭 취소·복원 기능이 Jobs+의 상태에 계속 의존한다. 이 기능은 위 연결을 함께 검토·변경·검증할 수 있는 단계에서 이동한다([S5], [S6]).

이미 사망한 상태에서 서버가 꺼진 플레이어의 보호 플래그도 고려해야 한다. 기능을 끄더라도 이미 성립한 보호 상태의 복원까지 중단하면 안 된다. 모듈 제거만으로 삭제된 아이템을 복구할 수 있는 것은 아니므로 코드 롤백과 월드 데이터 복원을 구분한다.

### 4.4 기존 명령어와 콘텐츠 기능

| 기능 | 현재 위치 | 결정 |
|---|---|---|
| `/job` | Jobs+ `JobCommand` | 직업·코인·경험치·관리 기능 유지 |
| `/arc` | ArcLib `ArcCommand` | 액션·홀더 GUI와 패킷을 함께 유지 |
| `/fallingtree toggle` | FallingTree 명령 등록·`ToggleCommand` | 개인 벌목 설정이므로 유지 |
| `/tacz` | TACZ `RootCommand`·하위 명령 | 총기·팩·부착물 관리로 유지 |
| 주식·경제 공지 | Jobs+ `StockMarketTicker` | 시장 상태·계좌·GUI와 연결되어 있으므로 유지 |
| 자동 재파종 | Jobs+ `AutoReplantReward` → `CropReplantManager` | 농부 보상에 종속되므로 유지 |
| 펫 | Advanced Netherite `PetNetworking`·저장·아이템 | 유지 |
| 설정 GUI·동기화 | YamlConfig | 기존 소비 모듈의 동작 유지 |

명령어를 사용한다는 이유만으로 구현을 모두 이동하지 않는다. 새 관리 명령은 새 모듈의 설정·상태만 담당한다.

## 5. 신규 모듈 구조 제안

```text
ServerUtilities-26.2/
  build.gradle
  settings.gradle
  gradle.properties
  gradlew / gradlew.bat / gradle/wrapper/
  src/main/java/<package>/
    ServerUtilities.java
    config/         설정 읽기·검증·저장
    command/        상태 조회·설정 재적용
    sleep/          한 명 수면 게임 규칙 관리
    combat/         겉날개·철골렘 정책
    balance/        크리퍼 피해·피로도 정책
    death/          후속 단계의 사망 정책·저장 상태
  src/main/resources/
    fabric.mod.json
    serverutilities.mixins.json
  README.md
```

위 구조를 기반으로 모듈을 생성했다. 관리 명령은 작은 진입점에 함께 두었고, 피해·피로도는 `mixin` 패키지에서 구현했다. 공지·텔레포트·백업·연동 프레임워크는 추가하지 않았다.

| 설계 항목 | 권장안 | 이유 |
|---|---|---|
| 모듈 수 | 운영 기능용 JAR 1개, 내부 패키지·설정으로 구분 | 작은 기능마다 프로젝트를 늘리지 않고 기능별 변경·롤백 가능 |
| 게임 버전 | 폴더 `26.2`, Git 브랜치 `26.3` 유지 | 현재 모든 모드의 대상 게임 버전은 26.2. 브랜치 이름과 구분 |
| 의존성 | Fabric Loader·Fabric API·Minecraft·Java | 첫 단계에 Jobs+/Arc/UI/Yaml 필수 의존성 불필요 |
| 설정 | 작고 독립적인 파일, 기능별 `enabled` 및 배율·범위 검증 | 단순 운영 기능 때문에 설정 GUI·클라이언트 동기화까지 끌어오지 않음 |
| 관리 명령 | 예: `/serverutilities status`, `/serverutilities reload` | 기존 `/job`, `/arc`, `/tacz`와 책임 분리. 이름은 제안 |
| 권한 | 현재 26.2 코드의 `Permissions` 검사 방식에 맞춤 | 일반 플레이어의 정책 변경 차단, 콘솔에서도 실행 가능 |
| 초기화 | 이벤트 등록은 한 번, 설정 재적용은 별도 | reload 때 핸들러 중복 등록 방지 |
| 기본 동작 | 이관하는 기존 기능의 배율·범위·메시지를 보존 | 구조 이동과 밸런스 변경을 별도 변경 단위로 분리 |

**실행 환경 보완:** 앞서 제안한 `environment: "server"`는 전용 서버만 대상으로 할 때 맞다. 싱글플레이·LAN에서는 로드되지 않는다. 기존 모드에서 옮기는 규칙이 싱글플레이에서도 계속 동작해야 한다면 `environment: "*"` + `main` 진입점 + 논리 서버에서만 처리하는 구성을 사용한다. 클라이언트 전용 클래스나 신규 필수 패킷을 넣지 않으면 원격 접속자에게 이 모듈 설치를 요구하지 않는 설계를 할 수 있다. 이 구분은 [Fabric 메타데이터 공식 문서](https://docs.fabricmc.net/develop/loader/fabric-mod-json)에 따른다.

YamlConfig는 Architectury·UILib 의존성과 COMMON 설정의 접속 시 패킷 동기화를 이미 포함한다([S9]). 재사용 자체가 잘못된 것은 아니지만, 독립 운영 모듈의 최소 의존성 목표에는 우선 맞지 않는다.

## 6. 한 명 수면 구현·복원 정책

바닐라에서 수면 비율 0은 최소 한 명의 수면으로 밤을 넘기는 설정이다. 현재 명령 이름은 다음과 같다. 근거: [수면 비율의 의미](https://www.minecraft.net/en-us/article/minecraft-snapshot-20w51a), [게임 규칙 이름·범위 변경](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-11).

```mcfunction
/gamerule minecraft:players_sleeping_percentage 0
```

| 상황 | 구현할 동작 |
|---|---|
| 처음 활성화 | 기존 값을 월드별로 기록한 뒤 해당 규칙만 변경 |
| 재시작 | 원래 값을 0으로 덮어 저장하지 않고, 관리 상태를 읽어 재적용 |
| 설정 reload | 서버 스레드에서 유효한 새 설정을 한 번 적용 |
| 비활성화 | 관리하던 값·원래 값을 확인해 복원. 운영자가 별도로 바꾼 값은 무조건 덮지 않음 |
| JAR 제거 | 마지막 게임 규칙 값은 월드에 남을 수 있음. 제거 전에 비활성화·복원 절차 제공 |
| 수면 불가능·시간 진행 비활성 | 바닐라 수면 조건 존중. 매 틱 강제로 아침으로 바꾸지 않음 |

0은 잠자는 사람이 없어도 밤이 자동으로 넘어간다는 뜻이 아니다. 구현 시 로컬 26.2 게임 JAR에서 `ServerLevel.getGameRules()`가 서버의 공유 규칙을 반환하고 `GameRules.get/set`을 사용하는 것을 확인했다. 구현은 월드마다 기존 값을 하나 보관한다. 실제 서버에 이 명령을 실행하지 않았다.

## 7. 부수적으로 확인한 개선 후보

| 항목 | 근거·판단 | 처리 순서 |
|---|---|---|
| 크리퍼 보정 설명과 적용 범위 | 주석은 어려움 난이도를 설명하지만 실제 조건은 크리퍼 폭발 여부뿐([S3]) | 기존 동작을 표에 명시. 어려움만 적용할지의 변경은 분리 |
| 전투 검사 비용 | 살아 있는 서버 플레이어마다 매 틱 위더 AABB 탐색([S2]) | 이동 후 실제 인원·엔티티 수로 측정. 성능 개선은 별도 |
| 재파종 대기열 수명 | `CropReplantManager`의 정적 목록이 `ServerLevel`을 보관하며 종료 시 정리가 없음([S11]) | 종료 직전에 요청이 남으면 같은 JVM의 다음 월드에 잔존할 수 있음. 서버 종료·월드 전환 정리 검토 및 재현 |
| collector 버전 검사 | `references_expected_minecraft()`는 문자열 포함 여부를 사용([S8] 181행) | 실제 버전 범위 해석이 아님. 신규 모듈과 별개로 검증 정확도 개선 검토 |

위 후보 전체를 이번 분리 작업에 한꺼번에 넣을 필요는 없다. 현재 실패가 확인된 것과 소스상 위험·후속 재현 대상은 구분해서 처리한다.

## 8. 실행 체크리스트

### 검토 완료

- [x] 10개 모듈의 Java 소스 목록·공통 기능 패턴 확인
- [x] 기존 명령 등록과 콘텐츠 기능 연결 확인
- [x] 전투·사망 규칙 및 크리퍼·피로도 분리 후보 확인
- [x] 사망 보존권·드롭 취소·리스폰·NBT 저장 연결 확인
- [x] 클라이언트 전용 기능·설정 라이브러리 의존성 확인
- [x] 빌드 수집 경로·메타데이터 검사·실패 시 기존 결과 처리 확인
- [x] 수정 대상·우선순위·검증 방법을 이 문서로 작성

### 1단계: 모듈 기반과 수면

- [x] 독립 모듈·Gradle wrapper·26.2/Java 25 설정 생성
- [x] 전용 서버 및 통합 서버 지원 범위를 메타데이터에 반영
- [x] 설정 읽기·숫자 범위 검증·잘못된 설정 오류 처리
- [x] 상태·reload 명령과 권한 검사 추가
- [x] 한 명 수면 적용·월드별 원래 값 저장·복원 구현
- [x] collector와 의존성 문서에 신규 모듈 연결

### 2단계: 기존 서버 규칙 이동

- [x] 전투 이벤트 이동 및 Jobs+의 기존 등록 제거
- [x] 겉날개·철골렘 정책을 각각 설정 가능하게 분리
- [x] 크리퍼 피해·피로도 Mixin 이동 및 기존 JSON 항목 제거
- [x] 기존 배율·범위·메시지 유지 확인(소스 기준)
- [ ] reload 및 재접속 이후 중복 핸들러·배율 적용 없음 확인

### 3단계: 사망 처리 이동

- [x] 보호 상태·드롭 취소·리스폰 복원·기존 NBT 키 호환을 함께 구현
- [x] 보존권 ID·소비량 유지, Advanced Netherite 부재 처리
- [x] 사망 공지에 신규 모듈의 클라이언트 언어 파일이 필수되지 않도록 처리
- [ ] 사망 화면 접속 종료·서버 재시작·설정 비활성화 후 복원 검증
- [x] 사망 처리 변경을 전투 규칙 변경과 별도 커밋 단위로 분리

### 회귀·배포·롤백

- [x] collector 실패 시 이전 JAR 보존 개선을 별도 변경으로 준비
- [x] 서버 전용·클라이언트 전용 설치 대상을 배포 문서에 명시
- [x] 수정된 기존 모드와 신규 JAR을 함께 배포하는 버전 조합 기록
- [ ] 각 기능을 켜고 끈 상태 및 새 모듈 미설치 클라이언트 접속 검증
- [x] 초기 모듈·수면 / 전투 / 밸런스 / 사망 / collector 개선을 목적별 커밋으로 분리
- [ ] 롤백 시 대응하는 기존 모드 JAR 복원 및 수면 규칙 원상 복원 절차 확인

## 9. 빌드·실행 검증 방법과 예상 결과

후속 구현에서 신규 모듈 `build`, Jobs+ `:fabric:build`, Advanced Netherite `:Fabric:build -x :Fabric:runDatagen`을 수행해 통과했다. 기존 두 모드는 관련 출력 폴더를 Gradle clean 후 빌드했으며 JAR에 이전 규칙 클래스가 없는 것을 확인했다. 신규 모듈의 설정·수면 복원 회귀 검사 28개와 collector 실패·복원 검사 8개가 통과했다.

신규 모듈만 사용하는 Fabric 개발 서버를 `--help`, `--initSettings`로 실행해 로딩·설정 초기화와 실제 대상 클래스의 Mixin 변환을 확인했다. EULA 동의나 실제 월드 서버 시작은 하지 않았다. 전체 11개 수집 실행, 다인 접속, 사망 후 재접속, 클라이언트 및 전체 모드 조합 검증은 미수행이다. 아래 표는 수동 검증을 포함한 확인 방법이다.

| 검증 | 방법 | 예상 결과 |
|---|---|---|
| 신규 모듈 빌드 | Java 25 환경, 신규 폴더에서 `./gradlew.bat build` | 릴리스 JAR 생성, 올바른 ID·MC 버전·실행 환경 |
| 기존 모드 빌드 | Jobs+ `:fabric:build`, Advanced Netherite `:Fabric:build` | 삭제·이동한 클래스와 Mixin의 잔존 참조 없음 |
| 통합 수집 | `python Build_File/collect_fabric_jars.py` | 11개 모듈 수집·ID 중복 없음. 빌드·검증·복사 성공 후 기존 수집본 교체 |
| 수면 | 여러 생존 플레이어 중 한 명 수면, 수면 취소·낮·시간 진행 설정도 비교 | 바닐라 조건을 만족할 때 한 명으로 밤 넘김 |
| 복원 | 기존 규칙 값을 기록하고 활성화→재시작→비활성화 | 원래 값 보존·복원, 설정 reload 중복 적용 없음 |
| 전투 | 습격/위더 각각에서 활강·철골렘·거리 경계 확인 | 이관 전과 동일한 조건·효과, 각 기능 off 시 미적용 |
| 배율 | 동일 조건의 크리퍼 피해·행동 피로도를 이동 전후 비교 | 기존 `4/3`, `1.5` 보정이 한 번만 적용 |
| 사망 | 보존권 유무·빈 인벤토리·장비/보조손·keep inventory 설정 조합 | 슬롯 전체 삭제 또는 보존권 1개 소비·복원이라는 의도 확인 |
| 복합 사망 | 전투 규칙으로 사망, 사망 화면에서 재접속·서버 재시작 | 중복 삭제·보존권 중복 소비·복원 누락 없음 |
| 번역 | 신규 모듈 없는 클라이언트로 공지·명령 결과 확인 | 번역 키 대신 읽을 수 있는 메시지 출력 |
| 기존 기능 | 직업·주식·청크 보호·펫·벌목·총기 동작 비교 | 관련 없는 콘텐츠 기능 회귀 없음 |
| 수집 실패 | 별도 테스트 경로·대체 빌드 실행기로 실패 재현 | B02 구현 후 이전 성공 산출물 유지 |

## 10. 주요 소스 근거

- [S1] [JobsPlus.java](../JobsPlusRemastered-26.2/common/src/main/java/com/daqem/jobsplus/JobsPlus.java): 49~59행, 공통 이벤트 등록.
- [S2] [EventKillElytraDuringRaidOrWither.java](../ServerUtilities-26.2/src/main/java/com/mcserver/serverutilities/combat/CombatRules.java): 37~84행 이벤트, 87~133행 탐색·제거·즉사.
- [S3] [CreeperExplosionDamageMixin.java](../ServerUtilities-26.2/src/main/java/com/mcserver/serverutilities/mixin/CreeperExplosionDamageMixin.java): 21~34행. [등록 JSON](../AdvancedNetherite-26.2/Fabric/src/main/resources/advancednetherite.mixins.json).
- [S4] [HungerExhaustionMixin.java](../ServerUtilities-26.2/src/main/java/com/mcserver/serverutilities/mixin/HungerExhaustionMixin.java): 17~22행.
- [S5] [EventDeleteRandomItemOnDeath.java](../ServerUtilities-26.2/src/main/java/com/mcserver/serverutilities/death/DeathRules.java): 29~30행 아이템 ID, 48~109행 사망·소비 처리.
- [S6] [MixinPlayerDeathItemProtection.java](../ServerUtilities-26.2/src/main/java/com/mcserver/serverutilities/mixin/PlayerDeathItemProtectionMixin.java), [MixinServerPlayer.java](../JobsPlusRemastered-26.2/common/src/main/java/com/daqem/jobsplus/mixin/MixinServerPlayer.java): 296~347행 복원·저장, [JobsServerPlayer.java](../JobsPlusRemastered-26.2/common/src/main/java/com/daqem/jobsplus/player/JobsServerPlayer.java), [Mixin 등록](../JobsPlusRemastered-26.2/common/src/main/resources/jobsplus-common.mixins.json).
- [S7] [ChunkProtectionEvents.java](../ItemRestrictions-26.2/common/src/main/java/com/daqem/itemrestrictions/event/ChunkProtectionEvents.java), [ChunkOwnership.java](../ItemRestrictions-26.2/common/src/main/java/com/daqem/itemrestrictions/chunk/ChunkOwnership.java).
- [S8] [collect_fabric_jars.py](../Build_File/collect_fabric_jars.py): 14행 모듈 목록, 140행 출력 초기화, 181행 버전 검사, 226행 환경 표시, 328행 이후 빌드·수집. [배포 의존성 문서](../Build_File/FABRIC_DEPENDENCIES.md).
- [S9] [YamlConfig 메타데이터](../YamlConfig-26.2/fabric/src/main/resources/fabric.mod.json), [PlayerJoinEvent.java](../YamlConfig-26.2/common/src/main/java/com/daqem/yamlconfig/event/PlayerJoinEvent.java).
- [S10] [MixinPlugin.java](../caramelChat-26.2/common/src/main/java/moe/caramel/chat/plugin/MixinPlugin.java), [Compatibilities.java](../caramelChat-26.2/common/src/main/java/moe/caramel/chat/plugin/Compatibilities.java).
- [S11] [CropReplantManager.java](../JobsPlusRemastered-26.2/common/src/main/java/com/daqem/jobsplus/event/block/CropReplantManager.java): 15행 정적 목록, 21~51행 처리·예약. [AutoReplantReward.java](../JobsPlusRemastered-26.2/common/src/main/java/com/daqem/jobsplus/integration/arc/reward/rewards/farmer/AutoReplantReward.java): 51행 호출.
