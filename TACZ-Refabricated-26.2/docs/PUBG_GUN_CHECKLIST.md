# PUBG 총기 반영 및 신규 제작 체크리스트

기준: 2026-09-11, PUBG: BATTLEGROUNDS PC·콘솔. 모바일 전용 총기는 제외합니다.
이번 반영은 요청한 5종의 표시 이름 변경과 SLR 기본 총기팩 추가입니다.

## 표시 이름 변경 — 5종

| 반영 | 기존 표시 이름 | 변경 이름 | 유지되는 내부 ID |
|---|---|---|---|
| ☑ | HK-416A5 | M416 | `tacz:hk416d` |
| ☑ | Glock 17 | P18C | `tacz:glock_17` |
| ☑ | DB-2 Durin | 소드오프 / Sawed-Off | `tacz:db_short` |
| ☑ | HK-MP5A5 | MP5K | `tacz:hk_mp5a5` |
| ☑ | UZI | Micro UZI | `tacz:uzi` |

한국어·영어 번역의 `.name` 값만 변경했습니다. 기존 총기 설정·모델·텍스처·애니메이션·설명·탄약·제작법과 아이템 ID는 유지합니다. 기존 보유 총기에도 리소스를 갱신하면 새 이름이 표시됩니다.

P18C의 자동사격 추가, 소드오프의 발사 모드 변경, MP5K·Micro UZI의 외형 축소는 이번 범위에 포함하지 않습니다. 이 항목들의 PUBG 외형·성능을 완전히 재현했다는 의미는 아닙니다.

## SLR 기본 총기팩 추가

| 반영 | 항목 | 내용 |
|---|---|---|
| ☑ | 등록 | 새 ID `tacz:slr`, 번역 키 `tacz.gun.slr.name`·`tacz.gun.slr.desc` |
| ☑ | 외형 | 검은 합성재 개머리판·핸드가드·그립, 금속 몸체, 긴 총구, 기계식 조준기 |
| ☑ | 모델 | 기본 247개 큐브·LOD 102개 큐브. 전체 92개 뼈대, 손 표시와 탄창 변형 포함 |
| ☑ | 텍스처·아이콘 | 전용 256×256 텍스처, 인벤토리·HUD 아이콘 |
| ☑ | 발사·탄창 | 반자동, `tacz:308`, 기본 10발·확장 1~3단계 모두 20발. 약실 탄약은 기존 로직으로 별도 처리 |
| ☑ | 부착물 | 조준경·총구·확장 탄창 허용 태그와 장착 위치 |
| ☑ | 애니메이션·사운드 | 기본 팩 FN FAL 애니메이션·상태 머신·장전 키프레임 사운드 재사용 |
| ☑ | 제작 | 총기 제작대 레시피 추가 |
| ☐ | 실행 검증 | 1인칭 조준·손 위치·장전·부착물·멀티플레이 확인 필요 |

SLR의 피해량·반동·장전 시간 등은 기존 FN FAL 설정을 바탕으로 합니다. PUBG 최신 수치와 동일하게 조정한 밸런스 팩은 아닙니다. 기존 FN FAL은 별도 총기로 계속 남습니다.

전용 외형·텍스처는 새로 구성했고, 호환을 위한 뼈대 구조·피벗과 기초 설정은 같은 기본 팩의 FN FAL 리소스를 활용했습니다. PUBG 원본 이미지·메시·텍스처를 복사하지 않았습니다. 기존 코드·자산의 출처는 모듈의 `LICENSE`와 `LICENSES.md`를 따릅니다.

## 신규 제작 대기 — 일반 총기 27종

기존 신규 제작 24종에 G36C·M24·P92를 합쳤습니다. 세 총기도 이번부터 신규 제작 목록에서 관리합니다. 모델 일부나 뼈대·공통 로직의 재사용 가능성과 별개로, 해당 총기용 외형 제작 작업이 남아 있다는 뜻입니다.

| 제작 | 분류 | 총기 | 참고 |
|---|---|---|---|
| ☐ | AR | Groza | 전용 외형 필요 |
| ☐ | AR | Beryl M762 | AKM 개명 대상에서 제외 |
| ☐ | AR | ACE32 | 전용 외형 필요 |
| ☐ | AR | FAMAS | 전용 외형 필요 |
| ☐ | AR | K2 | 전용 외형 필요 |
| ☐ | AR | Mk47 Mutant | 전용 외형 필요 |
| ☐ | AR | G36C | 기존 G36K는 유지, 이번에 신규 제작 목록으로 이동 |
| ☐ | DMR | Mini14 | 전용 외형 필요 |
| ☐ | DMR | Mk12 | SPR15HB 개명 대상에서 제외 |
| ☐ | DMR | VSS | 전용 외형 필요 |
| ☐ | DMR | Dragunov | 전용 외형 필요 |
| ☐ | SMG | Tommy Gun | 전용 외형 필요 |
| ☐ | SMG | MP9 | 전용 외형 필요 |
| ☐ | SMG | JS9 | 전용 외형 필요 |
| ☐ | SR | Win94 | Springfield 1873 개명 대상에서 제외 |
| ☐ | SR | Lynx AMR | M95·M107 개명 대상에서 제외 |
| ☐ | SR | M24 | 기존 M700은 유지, 이번에 신규 제작 목록으로 이동 |
| ☐ | 산탄총 | S1897 | M870 개명 대상에서 제외 |
| ☐ | 산탄총 | S686 | DB-4 개명 대상에서 제외 |
| ☐ | 산탄총 | S12K | 전용 외형 필요 |
| ☐ | 산탄총 | DBS | DB-2·DB-4 개명 대상에서 제외 |
| ☐ | 산탄총 | O12 | AA-12 개명 대상에서 제외 |
| ☐ | LMG | MG3 | 전용 외형 필요 |
| ☐ | LMG | RPD | RPK 개명 대상에서 제외 |
| ☐ | 권총 | Skorpion | CZ75·B93R 개명 대상에서 제외 |
| ☐ | 권총 | R1895 | 전용 외형 필요 |
| ☐ | 권총 | P92 | 기존 M9A4는 유지, 이번에 신규 제작 목록으로 이동 |

| 분류 | 기존 대응 총기 | 이번 이름 변경 | SLR 추가 | 신규 제작 대기 | 합계 |
|---|---:|---:|---:|---:|---:|
| AR | 5 | 1 | 0 | 7 | 13 |
| DMR | 2 | 0 | 1 | 4 | 7 |
| SMG | 3 | 2 | 0 | 3 | 8 |
| SR | 2 | 0 | 0 | 3 | 5 |
| 산탄총 | 0 | 1 | 0 | 5 | 6 |
| LMG | 1 | 0 | 0 | 2 | 3 |
| 권총 | 1 | 1 | 0 | 3 | 5 |
| **합계** | **14** | **5** | **1** | **27** | **47** |

기존 대응 14종은 AKM, M16A4, SCAR-L, AUG, QBZ, SKS, Mk14, UMP, Vector, P90, Kar98k, AWM, Deagle, M249입니다. 대응 총기 보유 수이며 PUBG의 외형·성능을 완전히 재현한 수는 아닙니다.

## 제외·별도 관리

PUBG Update 42.1에서 **월드 스폰 제외**된 Mosin Nagant, R45, DP-28, PP-19 Bizon, P1911, QBU는 신규 제작 목록에서 제외합니다. 모든 모드의 데이터에서 영구 삭제되었다는 뜻은 아닙니다. 프로젝트 내 기존 총기를 삭제하지 않습니다.

| 제작 | 별도 특수 총기 | 필요한 작업 |
|---|---|---|
| ☐ | M79 | 전용 외형·연막탄 발사 동작 |
| ☐ | Panzerfaust | 전용 외형·일회용 발사 동작 |
| ☐ | Stun Gun | 전용 외형·게임 내 기절 효과 |
| ☐ | Flare Gun | 전용 외형·보급 호출 시스템 |

특수 총기 4종은 일반 총기 27종과 별도입니다. 석궁·박격포·투척물·근접무기는 이번 집계에서 제외합니다.

## 변경 파일과 적용

모든 리소스 경로는 `src/main/resources/assets/tacz/custom/tacz_default_gun/` 기준입니다.

| 파일 | 변경 목적 |
|---|---|
| `assets/tacz/lang/ko_kr.json`, `assets/tacz/lang/en_us.json` | 5종 이름 변경과 SLR 이름·설명 추가 |
| `data/tacz/index/guns/slr.json` | 새 총기 ID 등록 |
| `data/tacz/data/guns/slr_data.json` | SLR 발사·탄창·부착물 설정 |
| `data/tacz/recipe/gun/slr.json` | 제작대 레시피 |
| `data/tacz/tacz_tags/attachments/allow_attachments/slr.json` | 허용 부착물 |
| `assets/tacz/display/guns/slr_display.json` | 모델·애니메이션·사운드·텍스처 연결 |
| `assets/tacz/geo_models/gun/slr_geo.json`, `assets/tacz/geo_models/gun/lod/slr.json` | 기본·LOD 외형 |
| `assets/tacz/textures/gun/uv/slr.png`, `slot/slr.png`, `hud/slr.png` | 전용 텍스처와 GUI 아이콘 |

모듈의 README와 이 문서도 갱신합니다. 일반 Java 로직·기존 총기의 전투 설정은 수정하지 않습니다.

1. Java 25 환경에서 `TACZ-Refabricated-26.2` 폴더의 `./gradlew.bat build`로 빌드합니다.
2. 빌드된 모드 JAR을 서버·클라이언트에 동일하게 설치하고 다시 시작합니다.
3. 현재 기본 총기팩 로더는 시작 시 내장 리소스의 변경을 감지해 기본 팩을 갱신합니다. `DefaultPackDebug=true`로 기본 팩 덮어쓰기를 막아 둔 환경에서는 변경 리소스를 별도로 반영해야 합니다. 실제 설정은 `PreLoadConfig`가 읽는 `tacz-pre.toml`을 확인합니다.
4. 앞서 제공된 외부 `mcslr_addon` 팩을 설치했다면, 그 팩의 `mcslr:slr`과 이번 `tacz:slr`은 서로 다른 총기입니다. 기본 팩 버전만 사용할 경우 외부 팩의 중복 설치 상태를 확인합니다. 기존 `mcslr:slr` 아이템의 자동 변환은 포함하지 않습니다.

## 검증

- [x] 기존 5종은 번역 이름 값만 변경하고 내부 ID·설정·모델·제작법을 유지하는지 확인
- [x] SLR 등록→설정→display→모델·LOD·텍스처 연결 확인
- [x] FN FAL 애니메이션의 대상 뼈대·부모·피벗, 참조 사운드·스크립트 존재 확인
- [x] JSON 구문, 모델 크기·UV·뼈대 구조, PNG 파일 확인
- [ ] Gradle 빌드
- [ ] 기존 보유 총기 5종의 한국어·영어 표시 이름 확인
- [ ] SLR의 크리에이티브 목록·제작대 노출과 `tacz:308` 장전 확인
- [ ] 반자동·기본 10발·확장 20발·약실 처리 확인
- [ ] 1인칭 조준·장전·손 위치·부착물·LOD·서버/클라이언트 동기화 확인

## PUBG 기준 자료

- [AR](https://pubg.com/en/game-info/weapons/ar), [DMR](https://pubg.com/en/game-info/weapons/dmr), [SMG](https://pubg.com/en/game-info/weapons/smg), [SR](https://pubg.com/en/game-info/weapons/sr)
- [산탄총](https://pubg.com/en/game-info/weapons/sg), [권총](https://pubg.com/en/game-info/weapons/pistol), [LMG·특수 무기](https://pubg.com/en/game-info/weapons/etc)
- [Update 42.1 — 월드 스폰 제외 목록](https://pubg.com/en/news/10179)
