# TACZ-Refabricated-26.2

Minecraft 26.2용 Fabric 전용 TaCZ Refabricated 모듈입니다.

## 개발 환경

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.154.2+26.2
- Java 25
- Gradle 9.5.1

## 의존성

- Fabric Loader
- Fabric API
- Forge Config API Port 26.2.1

Forge Config API Port는 TaCZ 설정 구현에 필요한 Fabric 라이브러리입니다.

## 몬스터 총기 연동

- Server Utilities와 함께 사용하면 20% 확률로 지급되는 몬스터 풀세트의 무기 후보에 로드된 총기를 포함합니다. 별도 총기 장착 파츠를 넣지 않습니다.
- 몬스터는 탄약 아이템을 검사·소비하지 않습니다. 장탄수가 직접 줄어드는 스크립트도 서버 틱에서 보충하여 계속 발사할 수 있습니다. 플레이어의 탄약 규칙은 기존 설정을 사용합니다.
- 몬스터가 원래 선택한 공격 대상 및 Brain의 공격 대상을 사용합니다. 드래곤은 주변 생존 플레이어를 대상으로 합니다. 시야가 차단되거나 대상이 사망·크리에이티브·관전자이면 발사하지 않습니다.
- 사격 거리는 총기의 유효사거리(거리별 피해표의 첫 구간 거리)이며, 최소 12블록이고 상한은 없습니다. 피해표가 없는 RPG-7·M320처럼 유효사거리를 정할 수 없는 총기는 32블록입니다. 발사 간격·충전·과열은 서버 총기 API를 사용합니다.
- 총을 든 몬스터는 추적 범위를 사격 거리까지 늘려 그 거리의 상대도 공격 대상으로 잡습니다. 원래 추적 범위가 더 길면(좀비 35블록) 줄이지 않으며, 총을 내려놓으면 원래 값으로 돌아갑니다.
- 임의 장비 지급은 Server Utilities, 총기 발사는 TACZ에서 담당합니다. 두 모듈을 함께 업데이트합니다. Server Utilities 미설치 시에도 TACZ는 로드됩니다.
- 비인간형 몬스터의 총기·방어구 외형은 기존 렌더러의 장비 레이어 지원 범위에 따릅니다. 일반 몬스터 모델을 새로 제작하지는 않습니다.
- Server Utilities의 몬스터 레벨 계산에 총기 등급(F=1 ~ S=7)을 알려 줍니다. 등급은 `src/main/resources/tacz/gun_grades.json`에서 읽으며, 이 파일은 `tools/apply_tier_colors.py`가 이름 색과 같은 표에서 만듭니다.
- Server Utilities의 시작 장비에 등록된 총기 중 무작위 1정(빈 탄창)과 그 총의 탄약을 기본 장탄수만큼 넣습니다.

## PUBG 총기 명칭 및 SLR

- M416, P18C, 소드오프, MP5K, Micro UZI의 표시 이름과 내부 ID·번역 키·파일명·리소스 참조를 통일했습니다. 기존 외형·발사 설정은 유지하며, 이전 ID로 저장된 총기는 호환 매핑으로 읽습니다.
- 기본 총기팩에 `tacz:slr`을 추가했습니다. 전용 모델·LOD·텍스처·아이콘, 반자동 10/20발 탄창, 제작대 레시피를 포함합니다.
- PUBG 계열 신규 총기 22종(Groza, Beryl M762, ACE32, FAMAS, K2, Mk47 Mutant, Mini14, Mk12, VSS, Dragunov, Tommy Gun, MP9, JS9, Win94, M24, S12K, DBS, O12, MG3, RPD, Skorpion, R1895)을 기본 총기팩에 추가했습니다. 수치와 제작 구조는 [22종 설계 문서](docs/PUBG_GUN_EXPANSION_22.md)에 있고, 외형은 `tools/pubg_guns/build.py`로 다시 만들 수 있습니다.
- 이름 변경·SLR·22종 반영 내역은 [PUBG 총기 제작 체크리스트](docs/PUBG_GUN_CHECKLIST.md)에 정리했습니다.
- 리소스 연결·뼈대·변경 범위는 정적으로 확인했습니다.

## 빌드

```powershell
./gradlew.bat build
```

빌드 결과는 `build/libs`에 생성됩니다.

## 기준 소스

- TaCZ Refabricated 1.21.1
- Minecraft 26.2 초기 포팅 기준: q14433686-arch/TaCZ_Refabricated_Unofficial commit `2afb606497511dfad2cc18e9510246d629ca1528`

본 모듈은 비공식 Fabric 포트이며 원 TaCZ 개발팀의 공식 배포물이 아닙니다.
