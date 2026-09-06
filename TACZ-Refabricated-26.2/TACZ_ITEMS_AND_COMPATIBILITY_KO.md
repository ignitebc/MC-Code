# TACZ 기본 팩 아이템·탄약·부착물 호환 안내서

작성 기준: 2026-09-06 / `26.3` 브랜치 / `TACZ-Refabricated-26.2` / Minecraft 26.2 / 모드 버전 `1.1.8+fabric.26.2`.

현재 저장소에 포함된 기본 총기 팩의 등록 코드, JSON 데이터, 한국어 번역과 실제 부착물 판정 코드를 대조한 문서입니다. 추가 총기 팩, 서버에서 수정한 설정, 개별 아이템의 부착물 잠금은 기본 호환표에 반영하지 않았습니다. 게임을 실행해서 모든 조합을 시험한 결과는 아닙니다.

총기 이름을 누르면 해당 총기의 슬롯별 전체 호환표로, 부품 이름을 누르면 효과와 사용 가능한 모든 총기 목록으로 이동합니다. 브라우저나 편집기의 찾기 기능으로 영문 총기 이름, 한글 부품 이름 또는 `tacz:` ID를 검색할 수도 있습니다.

언어 파일은 한국어(`ko_kr`)와 영어(`en_us`)만 제공합니다. 한국어로 플레이해도 총기 54종의 이름은 영어 원문으로 표시하며, 설명·안내·분류와 탄약·부착물 이름은 한국어 번역을 유지합니다. 이 문서의 총기 이름도 영어 표시 이름과 일치시켰습니다.

| 목차 | 내용 |
| --- | --- |
| [1. 집계와 읽는 방법](#overview) | 아이템 수, 장착 조건, 표기와 수치 해석 |
| [2. 작업대·표적·탄약상자](#equipment) | 기본 장비 9종과 사용법 |
| [3. 총기 전체 목록](#guns) | 총기 54종의 영문 이름·탄약·기본 장탄수·발사 방식·제작 여부 |
| [4. 탄약 전체 목록](#ammo) | 탄약 24종과 사용하는 모든 총기·보관량 |
| [5. 총기별 전체 부착물 호환표](#gun-compatibility) | 총기 54종 각각의 슬롯별 모든 부품과 전용 수치 |
| [6. 부착물별 효과·사용 총기](#attachments) | 부착물 99종의 세부 효과·제작 여부·역방향 호환표 |
| [7. 특수탄과 탄창 사용법](#special-ammo) | 확장 탄창과 특수탄 개조의 차이 |
| [8. 숨김·누락·설정 불일치](#exceptions) | 일반 사용 목록과 구분해야 하는 항목 |
| [9. 확인 근거와 검증 범위](#sources) | 근거 파일과 정적 대조 범위 |

<a id="overview"></a>
## 1. 집계와 읽는 방법

| 분류 | 일반 크리에이티브 목록 | 숨김 정의 |
| --- | --- | --- |
| 총기 | 54 | 0 |
| 탄약 | 24 | 0 |
| 조준경 | 30 | 2 |
| 총구 부착물·총검 | 19 | 0 |
| 손잡이 | 11 | 1 |
| 개머리판 | 14 | 0 |
| 레이저 | 4 | 1 |
| 탄창·특수탄 개조 | 17 | 0 |
| 작업대·표적·석상·탄약상자 | 9 | 0 |
| 합계 | 182 | 4 |

이 수치는 플레이어가 구분하는 총기·탄약·부품 변형 기준입니다. Minecraft의 독립 등록 ID 개수와는 다릅니다. 예를 들어 여러 총기는 공통 아이템 `tacz:modern_kinetic_gun`에 총기 ID를 저장하는 방식이며, 탄약과 부착물도 각각 `tacz:ammo`, `tacz:attachment`를 공유합니다. 확장 탄창 1·2·3단계와 탄약상자 3종은 각각 별도 변형으로 셌습니다.

| 표기·조건 | 의미 |
| --- | --- |
| 장착 가능 | 총기 허용 태그를 재귀적으로 펼친 목록에 부품 ID가 있고, 부품 정의가 존재하며, 총기에 해당 종류의 슬롯도 있는 조합입니다. |
| [숨김] | 아이템 데이터는 있지만 일반 부착물 크리에이티브 탭에서 숨깁니다. 호환된다고 해서 일반적으로 획득 가능하다는 뜻은 아닙니다. |
| 기본 내장 부품 | 외부 장착 목록과 별개인 builtin_attachments 설정입니다. AUG와 P90의 기본 조준경을 따로 표시했습니다. |
| 레시피 있음 | 현재 기본 리소스에서 결과 ID가 일치하는 제작식 파일을 확인했습니다. 링크에서 정확한 재료를 확인할 수 있습니다. 서버의 제작 제한까지 보장하는 뜻은 아닙니다. |
| 기본 레시피 파일 없음 | 이 저장소의 기본 제작식에서는 해당 결과 ID를 찾지 못했습니다. 크리에이티브 표시 여부와는 별개입니다. |
| 장탄수 | ammo_amount 기준이며 약실 추가 1발은 제외합니다. 확장 탄창 수치는 총기별 설정이며, 실제 지원 단계는 해당 호환표를 확인해야 합니다. |
| 배율 | 조준경 display의 zoom 설정값입니다. 번역 이름의 숫자나 현실 제품 사양과 다를 수 있습니다. |
| 효과 수치 | 기본 부착물 설정값입니다. 여러 부품·사격 모드·서버 배율을 적용한 최종 피해량 또는 최종 반동을 의미하지 않습니다. |
| 전용 수치 | exclusive_attachments는 호환 목록이 아니라 해당 총기에서 기본 부품 데이터를 대체하는 수치입니다. 해당 총기 상세표를 우선합니다. |
| 잠긴 총기 | 부착물 잠금이 설정된 개별 총기는 호환 목록에 있어도 서버가 개조를 거부합니다. |
| 같은 슬롯 | 동일 슬롯의 부품은 하나만 장착합니다. 소음기와 총검, 일반 확장 탄창과 특수탄 개조는 각각 같은 슬롯을 공유합니다. |

<a id="equipment"></a>
## 2. 작업대·표적·탄약상자

| 한글 이름 | 아이템·변형 ID | 용도와 사용법 |
| --- | --- | --- |
| TaCZ 총기 작업대 | `tacz:gun_smith_table` | 총기와 기타 장비 제작. 기본 팩의 총기 분류 탭을 제공합니다. |
| TaCZ 탄약 작업대 | `tacz:workbench_a` / BlockId `tacz:ammo_workbench` | 탄약 제작. 탄종별 탭에서 필요한 탄약을 찾습니다. |
| TaCZ 부착물 작업대 | `tacz:workbench_c` / BlockId `tacz:attachment_workbench` | 조준경·총구 부품·손잡이·개머리판·탄창·레이저 제작. |
| 인간형 표적판 | `tacz:target` | 고정 사격 표적. 총탄 명중 피해량·거리 표시, 명중 위치에 따른 레드스톤 신호 처리. |
| 표적판이 실린 광산 수레 | `tacz:target_minecart` | 레일에 놓는 이동 표적. 이동 표적 사격과 피해량·거리 확인. |
| 석상 | `tacz:statue` | 총기 전시용. 총기를 들고 상호작용하면 넣고, 빈손으로 상호작용하면 꺼냅니다. |
| 철 탄약상자 | `tacz:ammo_box` / 철 등급 | 한 종류의 탄약 보관. 기본 설정에서 해당 탄약 3스택. |
| 금 탄약상자 | `tacz:ammo_box` / 금 등급 | 한 종류의 탄약 보관. 기본 설정에서 해당 탄약 6스택. |
| 다이아몬드 탄약상자 | `tacz:ammo_box` / 다이아몬드 등급 | 한 종류의 탄약 보관. 기본 설정에서 해당 탄약 9스택. |

일반 탄약상자는 인벤토리에서 상자를 집은 채 탄약 슬롯을 우클릭하면 넣고, 빈 슬롯을 우클릭하면 꺼냅니다. 서로 다른 탄종을 한 상자에 섞을 수 없습니다. 기본 용량은 `탄약 1스택 개수 × AmmoBoxStackSize(기본 3) × 등급 배수(철 1·금 2·다이아몬드 3)`입니다.

무한 탄약상자 2종은 제거되었습니다. 기존 저장 데이터에 `Creative` 또는 `AllTypeCreative`가 켜진 상자가 남아 있으면 빈 일반 탄약상자로 취급하며, 기존의 가상 탄약 수량은 사용할 수 없습니다. 정상 탄약을 다시 넣으면 이전 무한 공급 표시와 가상 수량을 정리하고 실제로 넣은 탄약만 저장합니다.

| 기본 조작 | 키·방법 | 설명 |
| --- | --- | --- |
| 총기 개조 | Z | 총기를 든 상태에서 개조 화면을 열고, 인벤토리의 호환 부품을 선택합니다. |
| 재장전 | R | 총기에 맞는 탄약을 준비하고 재장전합니다. 인벤토리 급탄식 미니건은 일반 탄창 총기와 다릅니다. |
| 사격 방식 변경 | G | 총기에 정의된 단발·자동·점사 모드 사이를 전환합니다. |
| 조준 배율 변경 | 조준 중 V | 여러 배율이 정의된 조준경에서 배율을 전환합니다. |
| 근접 공격 | 비조준 상태 V | 총기 근접 공격. 총검·개머리판의 근접 공격 설정이 영향을 줄 수 있습니다. |
| 총기 살펴보기 | H | 총기 확인 동작을 재생합니다. |

키는 현재 코드의 기본값이며 사용자가 조작 설정에서 바꾼 값이 우선합니다.

<a id="guns"></a>
## 3. 총기 전체 목록 — 54종

### 권총 — 14종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [B93R](#gun-b93r) | `tacz:b93r` | [9mm 탄약](#ammo-9mm) | 20 | 점사 / 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/b93r.json) |
| [CZ 75](#gun-cz75) | `tacz:cz75` | [9mm 탄약](#ammo-9mm) | 16 | 자동 | 현재 팩에서는 자동 사격용 권총 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/cz75.json) |
| [Deagle 50](#gun-deagle) | `tacz:deagle` | [.50 AE 탄약](#ammo-50ae) | 7 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/deagle.json) |
| [Golden Deagle 357](#gun-deagle_golden) | `tacz:deagle_golden` | [.357 매그넘 탄약](#ammo-357mag) | 9 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/deagle_golden.json) |
| [Glock 17](#gun-glock_17) | `tacz:glock_17` | [9mm 탄약](#ammo-9mm) | 17 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/glock_17.json) |
| [MK23 Offensive Pistol](#gun-hk_mk23) | `tacz:hk_mk23` | [.45 ACP 탄약](#ammo-45acp) | 12 | 단발 / 점사 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk_mk23.json) |
| [.30-06 Lonetrail Hand Cannon](#gun-lonetrail) | `tacz:lonetrail` | [.30-06 스프링필드 탄약](#ammo-30_06) | 1 | 단발 | 소총탄을 쓰는 1발 장전식 핸드캐논 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/lonetrail.json) |
| [M1911](#gun-m1911) | `tacz:m1911` | [.45 ACP 탄약](#ammo-45acp) | 7 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m1911.json) |
| [M9A4](#gun-m9a4) | `tacz:m9a4` | [9mm 탄약](#ammo-9mm) | 17 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m9a4.json) |
| [P320](#gun-p320) | `tacz:p320` | [.45 ACP 탄약](#ammo-45acp) | 12 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/p320.json) |
| [.357 Rhino Revolver](#gun-rhino357) | `tacz:rhino357` | [.357 매그넘 탄약](#ammo-357mag) | 6 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/rhino357.json) |
| [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500) | `tacz:taurus500` | [.500 매그넘](#ammo-500mag) | 5 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/taurus500.json) |
| [.22 Modle 943 Revolver](#gun-taurus943) | `tacz:taurus943` | [.22 윈체스터 매그넘](#ammo-22wmr) | 8 | 단발 | 단거리 보조 사격 | 기본 레시피 파일 없음 |
| [Timeless .50 Z-Type](#gun-timeless50) | `tacz:timeless50` | [.50 AE 탄약](#ammo-50ae) | 8 | 단발 | 단거리 보조 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/timeless50.json) |

### 소총 — 17종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [AKM](#gun-ak47) | `tacz:ak47` | [7.62x39mm 탄약](#ammo-762x39) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/ak47.json) |
| [AUG](#gun-aug) | `tacz:aug` | [5.56x45mm 탄약](#ammo-556x45) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/aug.json) |
| [FN FAL Battle Rifle](#gun-fn_fal) | `tacz:fn_fal` | [.308 윈체스터 탄약](#ammo-308) | 20 | 단발 / 자동 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/fn_fal.json) |
| [G36K](#gun-g36k) | `tacz:g36k` | [5.56x45mm 탄약](#ammo-556x45) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/g36k.json) |
| [HK-416A5](#gun-hk416d) | `tacz:hk416d` | [5.56x45mm 탄약](#ammo-556x45) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk416d.json) |
| [HK G3 Battle rifle](#gun-hk_g3) | `tacz:hk_g3` | [.308 윈체스터 탄약](#ammo-308) | 20 | 단발 / 자동 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk_g3.json) |
| [M16A1 Service Rifle](#gun-m16a1) | `tacz:m16a1` | [5.56x45mm 탄약](#ammo-556x45) | 20 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m16a1.json) |
| [M16A4 Service Rifle](#gun-m16a4) | `tacz:m16a4` | [5.56x45mm 탄약](#ammo-556x45) | 30 | 점사 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m16a4.json) |
| [M4A1 Carbine](#gun-m4a1) | `tacz:m4a1` | [5.56x45mm 탄약](#ammo-556x45) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m4a1.json) |
| [MK14 EBR](#gun-mk14) | `tacz:mk14` | [.308 윈체스터 탄약](#ammo-308) | 10 | 단발 / 자동 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/mk14.json) |
| [QBZ-191 Assault Rifle](#gun-qbz_191) | `tacz:qbz_191` | [5.8mm DBP87 탄약](#ammo-58x42) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/qbz_191.json) |
| [QBZ-95 "Longbow"](#gun-qbz_95) | `tacz:qbz_95` | [5.8mm DBP87 탄약](#ammo-58x42) | 30 | 자동 / 단발 / 점사 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/qbz_95.json) |
| [SCAR-H Battle Rifle](#gun-scar_h) | `tacz:scar_h` | [.308 윈체스터 탄약](#ammo-308) | 20 | 단발 / 자동 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/scar_h.json) |
| [SCAR-L Assault Rifle](#gun-scar_l) | `tacz:scar_l` | [5.56x45mm 탄약](#ammo-556x45) | 30 | 자동 / 점사 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/scar_l.json) |
| [Sks Tactical Rifle](#gun-sks_tactical) | `tacz:sks_tactical` | [7.62x39mm 탄약](#ammo-762x39) | 10 | 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/sks_tactical.json) |
| [SPR-15 HB "Sagittarius"](#gun-spr15hb) | `tacz:spr15hb` | [5.56x45mm 탄약](#ammo-556x45) | 15 | 단발 / 점사 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/spr15hb.json) |
| [Type 81-1 Service Rifle](#gun-type_81) | `tacz:type_81` | [7.62x39mm 탄약](#ammo-762x39) | 30 | 자동 / 단발 | 일반 소총 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/type_81.json) |

### 기관단총 — 5종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [HK-MP5A5](#gun-hk_mp5a5) | `tacz:hk_mp5a5` | [9mm 탄약](#ammo-9mm) | 30 | 자동 / 점사 / 단발 | 근거리 연속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk_mp5a5.json) |
| [P90 PDW](#gun-p90) | `tacz:p90` | [5.7x28mm 철갑탄](#ammo-57x28) | 50 | 자동 / 점사 | 근거리 연속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/p90.json) |
| [UMP45 SMG](#gun-ump45) | `tacz:ump45` | [.45 ACP 탄약](#ammo-45acp) | 25 | 자동 / 점사 | 근거리 연속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/ump45.json) |
| [UZI](#gun-uzi) | `tacz:uzi` | [9mm 탄약](#ammo-9mm) | 20 | 자동 | 근거리 연속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/uzi.json) |
| [Vector SMG](#gun-vector45) | `tacz:vector45` | [.45 ACP 탄약](#ammo-45acp) | 20 | 자동 / 점사 / 단발 | 근거리 연속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/vector45.json) |

### 산탄총 — 6종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [AA12 Shotgun](#gun-aa12) | `tacz:aa12` | [12 게이지 산탄](#ammo-12g) | 8 | 단발 / 자동 | 근거리 산탄 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/aa12.json) |
| [DB-4 Ursus](#gun-db_long) | `tacz:db_long` | [12 게이지 산탄](#ammo-12g) | 2 | 단발 | 근거리 산탄 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/db_long.json) |
| [DB-2 Durin](#gun-db_short) | `tacz:db_short` | [12 게이지 산탄](#ammo-12g) | 2 | 점사 / 단발 | 근거리 산탄 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/db_short.json) |
| [M1014 Battle Shotgun](#gun-m1014) | `tacz:m1014` | [12 게이지 산탄](#ammo-12g) | 6 | 단발 | 근거리 산탄 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m1014.json) |
| [M870](#gun-m870) | `tacz:m870` | [12 게이지 산탄](#ammo-12g) | 5 | 단발 | 근거리 산탄 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m870.json) |
| [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) | `tacz:spas_12` | [12 게이지 산탄](#ammo-12g) | 5 | 단발 / 점사 | 근거리 산탄 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/spas_12.json) |

### 저격소총 — 6종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [Accuracy International AWM](#gun-ai_awp) | `tacz:ai_awp` | [.338 라푸아 매그넘 탄약](#ammo-338) | 5 | 단발 | 원거리 정밀 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/ai_awp.json) |
| [Mauser Kar98k Rifle](#gun-kar98) | `tacz:kar98` | [8mm 마우저 탄약](#ammo-792x57) | 4 | 단발 | 원거리 정밀 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/kar98.json) |
| [M107 Sniper Rifle](#gun-m107) | `tacz:m107` | [.50 BMG 탄약](#ammo-50bmg) | 10 | 단발 | 대구경 정밀 사격; 현재 설정은 수동 장전 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m107.json) |
| [M700 Sniper Rifle](#gun-m700) | `tacz:m700` | [.30-06 스프링필드 탄약](#ammo-30_06) | 5 | 단발 | 원거리 정밀 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m700.json) |
| [M95 .50 Cal Antimaterial](#gun-m95) | `tacz:m95` | [.50 BMG 탄약](#ammo-50bmg) | 5 | 단발 | 원거리 정밀 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m95.json) |
| [Springfield 1873 Trapdoor Rifle](#gun-springfield1873) | `tacz:springfield1873` | [45-70 탄약](#ammo-45_70) | 1 | 단발 | 1발 장전식 소총 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/springfield1873.json) |

### 기관총 — 4종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [FN EVOLYS Machine Gun](#gun-fn_evolys) | `tacz:fn_evolys` | [.308 윈체스터 탄약](#ammo-308) | 75 | 자동 | 지속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/fn_evolys.json) |
| [M249 Machine Gun](#gun-m249) | `tacz:m249` | [5.56x45mm 탄약](#ammo-556x45) | 75 | 자동 | 지속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m249.json) |
| [M134 Minigun](#gun-minigun) | `tacz:minigun` | [.308 윈체스터 탄약](#ammo-308) | 인벤토리 급탄 | 자동 / 점사 | 인벤토리 탄약으로 고속 연사; 과열 관리 필요 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/minigun.json) |
| [RPK](#gun-rpk) | `tacz:rpk` | [7.62x39mm 탄약](#ammo-762x39) | 40 | 자동 / 단발 | 지속 사격 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/rpk.json) |

### 중화기 — 2종

| 영문 총기 이름·상세 호환표 | 총기 ID | 사용 탄약 | 기본 장탄수 | 발사 모드 | 용도 | 제작 |
| --- | --- | --- | --- | --- | --- | --- |
| [M320 Grenade Launcher](#gun-m320) | `tacz:m320` | [40mm 유탄](#ammo-40mm) | 1 | 단발 | 1발 장전식 유탄 발사 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m320.json) |
| [RPG-7](#gun-rpg7) | `tacz:rpg7` | [RPG-7 로켓](#ammo-rpg_rocket) | 1 | 단발 | 1발 장전식 로켓 발사 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/rpg7.json) |

<a id="ammo"></a>
## 4. 탄약 전체 목록 — 24종

사용 총기는 기본 총기 54종의 ammo 필드를 역으로 대조했습니다. 현실 총기의 구경을 추정해서 연결하지 않았습니다. 보관 발수는 기본 탄약상자 설정 기준입니다.

| 한글 이름 | 탄약 ID | 사용 가능한 모든 기본 총기 | 1스택 | 철 / 금 / 다이아 상자 발수 | 제작 |
| --- | --- | --- | --- | --- | --- |
| <a id="ammo-12g"></a>12 게이지 산탄 | `tacz:12g` | [AA12 Shotgun](#gun-aa12), [DB-4 Ursus](#gun-db_long), [DB-2 Durin](#gun-db_short), [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) | 36 | 108 / 216 / 324 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/12g.json) |
| <a id="ammo-22wmr"></a>.22 윈체스터 매그넘 | `tacz:22wmr` | [.22 Modle 943 Revolver](#gun-taurus943) | 64 | 192 / 384 / 576 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/22wmr.json) |
| <a id="ammo-308"></a>.308 윈체스터 탄약 | `tacz:308` | [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [HK G3 Battle rifle](#gun-hk_g3), [M134 Minigun](#gun-minigun), [MK14 EBR](#gun-mk14), [SCAR-H Battle Rifle](#gun-scar_h) | 48 | 144 / 288 / 432 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/308.json) |
| <a id="ammo-30_06"></a>.30-06 스프링필드 탄약 | `tacz:30_06` | [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M700 Sniper Rifle](#gun-m700) | 36 | 108 / 216 / 324 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/30_06.json) |
| <a id="ammo-338"></a>.338 라푸아 매그넘 탄약 | `tacz:338` | [Accuracy International AWM](#gun-ai_awp) | 30 | 90 / 180 / 270 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/338.json) |
| <a id="ammo-357mag"></a>.357 매그넘 탄약 | `tacz:357mag` | [Golden Deagle 357](#gun-deagle_golden), [.357 Rhino Revolver](#gun-rhino357) | 48 | 144 / 288 / 432 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/357mag.json) |
| <a id="ammo-40mm"></a>40mm 유탄 | `tacz:40mm` | [M320 Grenade Launcher](#gun-m320) | 6 | 18 / 36 / 54 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/40mm.json) |
| <a id="ammo-45_70"></a>45-70 탄약 | `tacz:45_70` | [Springfield 1873 Trapdoor Rifle](#gun-springfield1873) | 48 | 144 / 288 / 432 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/45_70.json) |
| <a id="ammo-45acp"></a>.45 ACP 탄약 | `tacz:45acp` | [MK23 Offensive Pistol](#gun-hk_mk23), [M1911](#gun-m1911), [P320](#gun-p320), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/45acp.json) |
| <a id="ammo-46x30"></a>4.6mm 철갑탄 | `tacz:46x30` | **기본 팩에 사용 총기 없음** | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/46x30.json) |
| <a id="ammo-500mag"></a>.500 매그넘 | `tacz:500mag` | [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500) | 42 | 126 / 252 / 378 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/500mag.json) |
| <a id="ammo-50ae"></a>.50 AE 탄약 | `tacz:50ae` | [Deagle 50](#gun-deagle), [Timeless .50 Z-Type](#gun-timeless50) | 48 | 144 / 288 / 432 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/50ae.json) |
| <a id="ammo-50bmg"></a>.50 BMG 탄약 | `tacz:50bmg` | [M107 Sniper Rifle](#gun-m107), [M95 .50 Cal Antimaterial](#gun-m95) | 30 | 90 / 180 / 270 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/50bmg.json) |
| <a id="ammo-545x39"></a>5.45x39mm 탄약 | `tacz:545x39` | **기본 팩에 사용 총기 없음** | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/545x39.json) |
| <a id="ammo-556x45"></a>5.56x45mm 탄약 | `tacz:556x45` | [AUG](#gun-aug), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [SCAR-L Assault Rifle](#gun-scar_l), [SPR-15 HB "Sagittarius"](#gun-spr15hb) | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/556x45.json) |
| <a id="ammo-57x28"></a>5.7x28mm 철갑탄 | `tacz:57x28` | [P90 PDW](#gun-p90) | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/57x28.json) |
| <a id="ammo-58x42"></a>5.8mm DBP87 탄약 | `tacz:58x42` | [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95) | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/58x42.json) |
| <a id="ammo-68x51fury"></a>6.8x51mm 퓨리 탄약 | `tacz:68x51fury` | **기본 팩에 사용 총기 없음** | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/68x51fury.json) |
| <a id="ammo-762x25"></a>7.62x25mm 토카레프 탄약 | `tacz:762x25` | **기본 팩에 사용 총기 없음** | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/762x25.json) |
| <a id="ammo-762x39"></a>7.62x39mm 탄약 | `tacz:762x39` | [AKM](#gun-ak47), [RPK](#gun-rpk), [Sks Tactical Rifle](#gun-sks_tactical), [Type 81-1 Service Rifle](#gun-type_81) | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/762x39.json) |
| <a id="ammo-762x54"></a>7.62x54mm 탄약 | `tacz:762x54` | **기본 팩에 사용 총기 없음** | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/762x54.json) |
| <a id="ammo-792x57"></a>8mm 마우저 탄약 | `tacz:792x57` | [Mauser Kar98k Rifle](#gun-kar98) | 36 | 108 / 216 / 324 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/792x57.json) |
| <a id="ammo-9mm"></a>9mm 탄약 | `tacz:9mm` | [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [HK-MP5A5](#gun-hk_mp5a5), [M9A4](#gun-m9a4), [UZI](#gun-uzi) | 60 | 180 / 360 / 540 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/9mm.json) |
| <a id="ammo-rpg_rocket"></a>RPG-7 로켓 | `tacz:rpg_rocket` | [RPG-7](#gun-rpg7) | 6 | 18 / 36 / 54 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/ammo/rpg_rocket.json) |

<a id="gun-compatibility"></a>
## 5. 총기별 전체 부착물 호환표

각 슬롯의 목록은 전부 개별 부품입니다. 같은 슬롯의 부품을 동시에 장착할 수 있다는 뜻은 아닙니다. 숨김 부품도 데이터상 호환되면 표시하되 [숨김]으로 구분합니다. 슬롯 미지원·정의 누락은 8절에 따로 정리했습니다.

<a id="gun-aa12"></a>
### AA12 Shotgun — `tacz:aa12`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 산탄총 / 근거리 산탄 사격 |
| 사용 탄약 | [12 게이지 산탄](#ammo-12g) (`tacz:12g`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 8발 / 16발 / 24발 / 32발 |
| 발사 모드 / 장전 구조 | 단발 / 자동 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/aa12.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/aa12_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/aa12.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 15 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 3 | [마스티프 산탄총 총구 제동기](#attachment-muzzle_brake_mastiff_sg) (`muzzle_brake_mastiff_sg`)<br>[산탄총 초크](#attachment-muzzle_choke_sg) (`muzzle_choke_sg`)<br>[12 게이지 소음기](#attachment-muzzle_silencer_sg) (`muzzle_silencer_sg`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[산탄총 슬러그 탄환](#attachment-ammo_mod_slug) (`ammo_mod_slug`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-ai_awp"></a>
### Accuracy International AWM — `tacz:ai_awp`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 저격소총 / 원거리 정밀 사격 |
| 사용 탄약 | [.338 라푸아 매그넘 탄약](#ammo-338) (`tacz:338`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 5발 / 6발 / 7발 / 8발 |
| 발사 모드 / 장전 구조 | 단발 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/ai_awp.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/ai_awp_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/ai_awp.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 3 | [나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[저격소총 확장 탄창 1단계](#attachment-sniper_extended_mag_1) (`sniper_extended_mag_1`)<br>[저격소총 확장 탄창 2단계](#attachment-sniper_extended_mag_2) (`sniper_extended_mag_2`)<br>[저격소총 확장 탄창 3단계](#attachment-sniper_extended_mag_3) (`sniper_extended_mag_3`) |

<a id="gun-ak47"></a>
### AKM — `tacz:ak47`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [7.62x39mm 탄약](#ammo-762x39) (`tacz:762x39`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 34발 / 37발 / 40발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/ak47.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/ak47_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/ak47.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 9 | [6H3 총검](#attachment-bayonet_6h3) (`bayonet_6h3`)<br>[크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 12 | [공장제 중량 개머리판](#attachment-oem_stock_heavy) (`oem_stock_heavy`)<br>[공장제 경량 개머리판](#attachment-oem_stock_light) (`oem_stock_light`)<br>[공장제 전술 개머리판](#attachment-oem_stock_tactical) (`oem_stock_tactical`)<br>[AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-aug"></a>
### AUG — `tacz:aug`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 34발 / 39발 / 50발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 조준경: [AUG 일체형 조준경 [숨김]](#attachment-scope_aug_default) |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/aug.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/aug_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/aug.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[AUG 일체형 조준경 [숨김]](#attachment-scope_aug_default) (`scope_aug_default`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 9 | [M9 총검](#attachment-bayonet_m9) (`bayonet_m9`)<br>[크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-b93r"></a>
### B93R — `tacz:b93r`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [9mm 탄약](#ammo-9mm) (`tacz:9mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 23발 / 26발 / 32발 |
| 발사 모드 / 장전 구조 | 점사 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/b93r.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/b93r_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/b93r.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-cz75"></a>
### CZ 75 — `tacz:cz75`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 현재 팩에서는 자동 사격용 권총 |
| 사용 탄약 | [9mm 탄약](#ammo-9mm) (`tacz:9mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 16발 / 19발 / 23발 / 27발 |
| 발사 모드 / 장전 구조 | 자동 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/cz75.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/cz75_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/cz75.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-db_long"></a>
### DB-4 Ursus — `tacz:db_long`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 산탄총 / 근거리 산탄 사격 |
| 사용 탄약 | [12 게이지 산탄](#ammo-12g) (`tacz:12g`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 2발 / 별도 배열 없음 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/db_long.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/db_long_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/db_long.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 4 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[산탄총 슬러그 탄환](#attachment-ammo_mod_slug) (`ammo_mod_slug`) |

<a id="gun-db_short"></a>
### DB-2 Durin — `tacz:db_short`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 산탄총 / 근거리 산탄 사격 |
| 사용 탄약 | [12 게이지 산탄](#ammo-12g) (`tacz:12g`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 2발 / 별도 배열 없음 |
| 발사 모드 / 장전 구조 | 점사 / 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/db_short.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/db_short_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/db_short.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 1 | [공장제 전술 개머리판](#attachment-oem_stock_tactical) (`oem_stock_tactical`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 4 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[산탄총 슬러그 탄환](#attachment-ammo_mod_slug) (`ammo_mod_slug`) |

이 총기에서는 아래 전용 설정이 해당 부품의 기본 데이터를 대체합니다. 기본 효과를 그대로 더해서 계산하면 안 됩니다.

| 부품 | 해당 총기 전용 설정 |
| --- | --- |
| [공장제 전술 개머리판](#attachment-oem_stock_tactical) | 무게 설정 +2<br>조준 전환 시간 0.04초 추가<br>일반 비조준 탄 퍼짐 0.125 감소<br>수직 반동 기준값 대비 -30% 가산<br>수평 반동 기준값 대비 -20% 가산 |

<a id="gun-deagle"></a>
### Deagle 50 — `tacz:deagle`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.50 AE 탄약](#ammo-50ae) (`tacz:50ae`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 7발 / 8발 / 10발 / 12발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/deagle.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/deagle_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/deagle.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 2 | [컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[T1 레드 도트](#attachment-sight_t1) (`sight_t1`) |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-deagle_golden"></a>
### Golden Deagle 357 — `tacz:deagle_golden`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.357 매그넘 탄약](#ammo-357mag) (`tacz:357mag`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 9발 / 12발 / 15발 / 17발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/deagle_golden.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/deagle_golden_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/deagle_golden.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 2 | [컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[T1 레드 도트](#attachment-sight_t1) (`sight_t1`) |
| 총구 부착물·총검 | 2 | [.357 황금 데저트 이글 장총열](#attachment-deagle_golden_long_barrel) (`deagle_golden_long_barrel`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-fn_evolys"></a>
### FN EVOLYS Machine Gun — `tacz:fn_evolys`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관총 / 지속 사격 |
| 사용 탄약 | [.308 윈체스터 탄약](#ammo-308) (`tacz:308`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 75발 / 100발 / 125발 / 150발 |
| 발사 모드 / 장전 구조 | 자동 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/fn_evolys.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/fn_evolys_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/fn_evolys.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-fn_fal"></a>
### FN FAL Battle Rifle — `tacz:fn_fal`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [.308 윈체스터 탄약](#ammo-308) (`tacz:308`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 25발 / 30발 / 35발 |
| 발사 모드 / 장전 구조 | 단발 / 자동 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/fn_fal.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/fn_fal_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/fn_fal.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-g36k"></a>
### G36K — `tacz:g36k`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 50발 / 75발 / 100발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/g36k.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/g36k_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/g36k.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-glock_17"></a>
### Glock 17 — `tacz:glock_17`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [9mm 탄약](#ammo-9mm) (`tacz:9mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 17발 / 20발 / 25발 / 30발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/glock_17.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/glock_17_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/glock_17.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-hk416d"></a>
### HK-416A5 — `tacz:hk416d`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 40발 / 50발 / 100발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk416d.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/hk416d_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/hk416d.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 15 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-hk_g3"></a>
### HK G3 Battle rifle — `tacz:hk_g3`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [.308 윈체스터 탄약](#ammo-308) (`tacz:308`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 30발 / 35발 / 40발 |
| 발사 모드 / 장전 구조 | 단발 / 자동 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk_g3.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/hk_g3_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/hk_g3.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 12 | [공장제 중량 개머리판](#attachment-oem_stock_heavy) (`oem_stock_heavy`)<br>[공장제 경량 개머리판](#attachment-oem_stock_light) (`oem_stock_light`)<br>[공장제 전술 개머리판](#attachment-oem_stock_tactical) (`oem_stock_tactical`)<br>[AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-hk_mk23"></a>
### MK23 Offensive Pistol — `tacz:hk_mk23`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.45 ACP 탄약](#ammo-45acp) (`tacz:45acp`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 12발 / 16발 / 20발 / 24발 |
| 발사 모드 / 장전 구조 | 단발 / 점사 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk_mk23.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/hk_mk23_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/hk_mk23.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 3 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ6 ILLM](#attachment-laser_peq6) (`laser_peq6`) |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-hk_mp5a5"></a>
### HK-MP5A5 — `tacz:hk_mp5a5`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관단총 / 근거리 연속 사격 |
| 사용 탄약 | [9mm 탄약](#ammo-9mm) (`tacz:9mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 40발 / 50발 / 60발 |
| 발사 모드 / 장전 구조 | 자동 / 점사 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/hk_mp5a5.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/hk_mp5a5_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/hk_mp5a5.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 16 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 11 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 12 | [공장제 중량 개머리판](#attachment-oem_stock_heavy) (`oem_stock_heavy`)<br>[공장제 경량 개머리판](#attachment-oem_stock_light) (`oem_stock_light`)<br>[공장제 전술 개머리판](#attachment-oem_stock_tactical) (`oem_stock_tactical`)<br>[AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-kar98"></a>
### Mauser Kar98k Rifle — `tacz:kar98`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 저격소총 / 원거리 정밀 사격 |
| 사용 탄약 | [8mm 마우저 탄약](#ammo-792x57) (`tacz:792x57`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 4발 / 4발 / 4발 / 4발 |
| 발사 모드 / 장전 구조 | 단발 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/kar98.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/kar98_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/kar98.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 1 | [마우저 4배율 경량 조준경](#attachment-scope_98k) (`scope_98k`) |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 3 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`) |

<a id="gun-lonetrail"></a>
### .30-06 Lonetrail Hand Cannon — `tacz:lonetrail`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 소총탄을 쓰는 1발 장전식 핸드캐논 |
| 사용 탄약 | [.30-06 스프링필드 탄약](#ammo-30_06) (`tacz:30_06`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 1발 / 1발 / 1발 / 1발 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/lonetrail.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/lonetrail_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/lonetrail.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 3 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`) |

<a id="gun-m1014"></a>
### M1014 Battle Shotgun — `tacz:m1014`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 산탄총 / 근거리 산탄 사격 |
| 사용 탄약 | [12 게이지 산탄](#ammo-12g) (`tacz:12g`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 6발 / 8발 / 10발 / 12발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m1014.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m1014_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m1014.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 11 | [밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 3 | [마스티프 산탄총 총구 제동기](#attachment-muzzle_brake_mastiff_sg) (`muzzle_brake_mastiff_sg`)<br>[산탄총 초크](#attachment-muzzle_choke_sg) (`muzzle_choke_sg`)<br>[12 게이지 소음기](#attachment-muzzle_silencer_sg) (`muzzle_silencer_sg`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 8 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[산탄총 슬러그 탄환](#attachment-ammo_mod_slug) (`ammo_mod_slug`)<br>[산탄총 탄약 확장 탄창 1단계](#attachment-shotgun_extended_mag_1) (`shotgun_extended_mag_1`)<br>[산탄총 탄약 확장 탄창 2단계](#attachment-shotgun_extended_mag_2) (`shotgun_extended_mag_2`)<br>[산탄총 탄약 확장 탄창 3단계](#attachment-shotgun_extended_mag_3) (`shotgun_extended_mag_3`) |

<a id="gun-m107"></a>
### M107 Sniper Rifle — `tacz:m107`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 저격소총 / 대구경 정밀 사격; 현재 설정은 수동 장전 |
| 사용 탄약 | [.50 BMG 탄약](#ammo-50bmg) (`tacz:50bmg`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 10발 / 12발 / 14발 / 20발 |
| 발사 모드 / 장전 구조 | 단발 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m107.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m107_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m107.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 1 | [벌처 .50 구경 소음기](#attachment-muzzle_silencer_vulture) (`muzzle_silencer_vulture`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[저격소총 확장 탄창 1단계](#attachment-sniper_extended_mag_1) (`sniper_extended_mag_1`)<br>[저격소총 확장 탄창 2단계](#attachment-sniper_extended_mag_2) (`sniper_extended_mag_2`)<br>[저격소총 확장 탄창 3단계](#attachment-sniper_extended_mag_3) (`sniper_extended_mag_3`) |

<a id="gun-m16a1"></a>
### M16A1 Service Rifle — `tacz:m16a1`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 24발 / 27발 / 30발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m16a1.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m16a1_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m16a1.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 1 | [Retro 3배율 조준경](#attachment-scope_retro_2x) (`scope_retro_2x`) |
| 총구 부착물·총검 | 9 | [M9 총검](#attachment-bayonet_m9) (`bayonet_m9`)<br>[크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-m16a4"></a>
### M16A4 Service Rifle — `tacz:m16a4`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 33발 / 39발 / 45발 |
| 발사 모드 / 장전 구조 | 점사 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m16a4.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m16a4_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m16a4.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 9 | [M9 총검](#attachment-bayonet_m9) (`bayonet_m9`)<br>[크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-m1911"></a>
### M1911 — `tacz:m1911`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.45 ACP 탄약](#ammo-45acp) (`tacz:45acp`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 7발 / 9발 / 12발 / 14발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m1911.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m1911_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m1911.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-m249"></a>
### M249 Machine Gun — `tacz:m249`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관총 / 지속 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 75발 / 100발 / 150발 / 200발 |
| 발사 모드 / 장전 구조 | 자동 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m249.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m249_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m249.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 15 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-m320"></a>
### M320 Grenade Launcher — `tacz:m320`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 중화기 / 1발 장전식 유탄 발사 |
| 사용 탄약 | [40mm 유탄](#ammo-40mm) (`tacz:40mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 1발 / 확장 탄창 슬롯 미지원 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m320.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m320_data.json) / 부착물 허용 파일 없음 |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 0 | 슬롯 미지원 |

<a id="gun-m4a1"></a>
### M4A1 Carbine — `tacz:m4a1`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 40발 / 50발 / 60발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m4a1.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m4a1_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m4a1.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 15 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 9 | [M9 총검](#attachment-bayonet_m9) (`bayonet_m9`)<br>[크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-m700"></a>
### M700 Sniper Rifle — `tacz:m700`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 저격소총 / 원거리 정밀 사격 |
| 사용 탄약 | [.30-06 스프링필드 탄약](#ammo-30_06) (`tacz:30_06`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 5발 / 6발 / 8발 / 10발 |
| 발사 모드 / 장전 구조 | 단발 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m700.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m700_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m700.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[저격소총 확장 탄창 1단계](#attachment-sniper_extended_mag_1) (`sniper_extended_mag_1`)<br>[저격소총 확장 탄창 2단계](#attachment-sniper_extended_mag_2) (`sniper_extended_mag_2`)<br>[저격소총 확장 탄창 3단계](#attachment-sniper_extended_mag_3) (`sniper_extended_mag_3`) |

<a id="gun-m870"></a>
### M870 — `tacz:m870`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 산탄총 / 근거리 산탄 사격 |
| 사용 탄약 | [12 게이지 산탄](#ammo-12g) (`tacz:12g`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 5발 / 6발 / 7발 / 8발 |
| 발사 모드 / 장전 구조 | 단발 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m870.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m870_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m870.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 3 | [마스티프 산탄총 총구 제동기](#attachment-muzzle_brake_mastiff_sg) (`muzzle_brake_mastiff_sg`)<br>[산탄총 초크](#attachment-muzzle_choke_sg) (`muzzle_choke_sg`)<br>[12 게이지 소음기](#attachment-muzzle_silencer_sg) (`muzzle_silencer_sg`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 8 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[산탄총 슬러그 탄환](#attachment-ammo_mod_slug) (`ammo_mod_slug`)<br>[산탄총 탄약 확장 탄창 1단계](#attachment-shotgun_extended_mag_1) (`shotgun_extended_mag_1`)<br>[산탄총 탄약 확장 탄창 2단계](#attachment-shotgun_extended_mag_2) (`shotgun_extended_mag_2`)<br>[산탄총 탄약 확장 탄창 3단계](#attachment-shotgun_extended_mag_3) (`shotgun_extended_mag_3`) |

<a id="gun-m95"></a>
### M95 .50 Cal Antimaterial — `tacz:m95`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 저격소총 / 원거리 정밀 사격 |
| 사용 탄약 | [.50 BMG 탄약](#ammo-50bmg) (`tacz:50bmg`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 5발 / 6발 / 8발 / 10발 |
| 발사 모드 / 장전 구조 | 단발 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m95.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m95_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m95.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 1 | [벌처 .50 구경 소음기](#attachment-muzzle_silencer_vulture) (`muzzle_silencer_vulture`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[저격소총 확장 탄창 1단계](#attachment-sniper_extended_mag_1) (`sniper_extended_mag_1`)<br>[저격소총 확장 탄창 2단계](#attachment-sniper_extended_mag_2) (`sniper_extended_mag_2`)<br>[저격소총 확장 탄창 3단계](#attachment-sniper_extended_mag_3) (`sniper_extended_mag_3`) |

<a id="gun-m9a4"></a>
### M9A4 — `tacz:m9a4`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [9mm 탄약](#ammo-9mm) (`tacz:9mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 17발 / 20발 / 25발 / 30발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/m9a4.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/m9a4_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/m9a4.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-minigun"></a>
### M134 Minigun — `tacz:minigun`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관총 / 인벤토리 탄약으로 고속 연사; 과열 관리 필요 |
| 사용 탄약 | [.308 윈체스터 탄약](#ammo-308) (`tacz:308`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 인벤토리 급탄 / 확장 탄창 슬롯 미지원 |
| 발사 모드 / 장전 구조 | 자동 / 점사 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/minigun.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/minigun_data.json) / 부착물 허용 파일 없음 |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 0 | 슬롯 미지원 |

<a id="gun-mk14"></a>
### MK14 EBR — `tacz:mk14`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [.308 윈체스터 탄약](#ammo-308) (`tacz:308`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 10발 / 15발 / 18발 / 20발 |
| 발사 모드 / 장전 구조 | 단발 / 자동 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/mk14.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/mk14_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/mk14.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-p320"></a>
### P320 — `tacz:p320`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.45 ACP 탄약](#ammo-45acp) (`tacz:45acp`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 12발 / 14발 / 16발 / 18발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/p320.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/p320_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/p320.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 3 | [미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-p90"></a>
### P90 PDW — `tacz:p90`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관단총 / 근거리 연속 사격 |
| 사용 탄약 | [5.7x28mm 철갑탄](#ammo-57x28) (`tacz:57x28`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 50발 / 확장 탄창 슬롯 미지원 (데이터 배열: 35발 / 45발 / 50발) |
| 발사 모드 / 장전 구조 | 자동 / 점사 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 조준경: [P90 기본 조준경 [숨김]](#attachment-sight_p90) |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/p90.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/p90_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/p90.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 17 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[P90 기본 조준경 [숨김]](#attachment-sight_p90) (`sight_p90`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 11 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 0 | 슬롯 미지원 |

<a id="gun-qbz_191"></a>
### QBZ-191 Assault Rifle — `tacz:qbz_191`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.8mm DBP87 탄약](#ammo-58x42) (`tacz:58x42`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 40발 / 50발 / 75발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/qbz_191.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/qbz_191_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/qbz_191.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-qbz_95"></a>
### QBZ-95 "Longbow" — `tacz:qbz_95`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.8mm DBP87 탄약](#ammo-58x42) (`tacz:58x42`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 33발 / 36발 / 75발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 점사 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/qbz_95.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/qbz_95_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/qbz_95.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-rhino357"></a>
### .357 Rhino Revolver — `tacz:rhino357`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.357 매그넘 탄약](#ammo-357mag) (`tacz:357mag`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 6발 / 6발 / 6발 / 6발 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/rhino357.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/rhino357_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/rhino357.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 4 | [에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`) |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 3 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`) |

<a id="gun-rpg7"></a>
### RPG-7 — `tacz:rpg7`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 중화기 / 1발 장전식 로켓 발사 |
| 사용 탄약 | [RPG-7 로켓](#ammo-rpg_rocket) (`tacz:rpg_rocket`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 1발 / 확장 탄창 슬롯 미지원 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/rpg7.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/rpg7_data.json) / 부착물 허용 파일 없음 |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 0 | 슬롯 미지원 |

<a id="gun-rpk"></a>
### RPK — `tacz:rpk`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관총 / 지속 사격 |
| 사용 탄약 | [7.62x39mm 탄약](#ammo-762x39) (`tacz:762x39`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 40발 / 50발 / 60발 / 75발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/rpk.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/rpk_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/rpk.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 12 | [공장제 중량 개머리판](#attachment-oem_stock_heavy) (`oem_stock_heavy`)<br>[공장제 경량 개머리판](#attachment-oem_stock_light) (`oem_stock_light`)<br>[공장제 전술 개머리판](#attachment-oem_stock_tactical) (`oem_stock_tactical`)<br>[AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-scar_h"></a>
### SCAR-H Battle Rifle — `tacz:scar_h`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [.308 윈체스터 탄약](#ammo-308) (`tacz:308`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 25발 / 30발 / 40발 |
| 발사 모드 / 장전 구조 | 단발 / 자동 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/scar_h.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/scar_h_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/scar_h.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

이 총기에서는 아래 전용 설정이 해당 부품의 기본 데이터를 대체합니다. 기본 효과를 그대로 더해서 계산하면 안 됩니다.

| 부품 | 해당 총기 전용 설정 |
| --- | --- |
| [표준 5-10x 망원조준경](#attachment-scope_standard_8x) | 무게 설정 +2<br>조준 전환 시간 0.04초 추가<br>수평 반동 기준값 대비 -20% 가산 |

<a id="gun-scar_l"></a>
### SCAR-L Assault Rifle — `tacz:scar_l`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 40발 / 55발 / 70발 |
| 발사 모드 / 장전 구조 | 자동 / 점사 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/scar_l.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/scar_l_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/scar_l.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 19 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 4 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[로프로 전술 레이저](#attachment-laser_lopro) (`laser_lopro`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) (`laser_peq15`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-sks_tactical"></a>
### Sks Tactical Rifle — `tacz:sks_tactical`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [7.62x39mm 탄약](#ammo-762x39) (`tacz:762x39`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 10발 / 15발 / 20발 / 25발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/sks_tactical.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/sks_tactical_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/sks_tactical.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-spas_12"></a>
### SPAS-12 Multi-purpose Shotgun — `tacz:spas_12`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 산탄총 / 근거리 산탄 사격 |
| 사용 탄약 | [12 게이지 산탄](#ammo-12g) (`tacz:12g`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 5발 / 6발 / 7발 / 8발 |
| 발사 모드 / 장전 구조 | 단발 / 점사 / 수동 장전(manual_action) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/spas_12.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/spas_12_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/spas_12.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 11 | [밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 3 | [마스티프 산탄총 총구 제동기](#attachment-muzzle_brake_mastiff_sg) (`muzzle_brake_mastiff_sg`)<br>[산탄총 초크](#attachment-muzzle_choke_sg) (`muzzle_choke_sg`)<br>[12 게이지 소음기](#attachment-muzzle_silencer_sg) (`muzzle_silencer_sg`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 11 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[프랑키 중형 개머리판](#attachment-stock_heavy_spas_12) (`stock_heavy_spas_12`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`)<br>[프랑키 전술 개머리판](#attachment-stock_tactical_spas_12) (`stock_tactical_spas_12`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 8 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[산탄총 슬러그 탄환](#attachment-ammo_mod_slug) (`ammo_mod_slug`)<br>[산탄총 탄약 확장 탄창 1단계](#attachment-shotgun_extended_mag_1) (`shotgun_extended_mag_1`)<br>[산탄총 탄약 확장 탄창 2단계](#attachment-shotgun_extended_mag_2) (`shotgun_extended_mag_2`)<br>[산탄총 탄약 확장 탄창 3단계](#attachment-shotgun_extended_mag_3) (`shotgun_extended_mag_3`) |

<a id="gun-spr15hb"></a>
### SPR-15 HB "Sagittarius" — `tacz:spr15hb`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [5.56x45mm 탄약](#ammo-556x45) (`tacz:556x45`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 15발 / 20발 / 25발 / 30발 |
| 발사 모드 / 장전 구조 | 단발 / 점사 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/spr15hb.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/spr15hb_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/spr15hb.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 20 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[1-6x 저배율 가변 광학조준경](#attachment-scope_lpvo_1_6) (`scope_lpvo_1_6`)<br>[Mark 5 HD 5-25x 가변 복합 광학 조준경](#attachment-scope_mk5hd) (`scope_mk5hd`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[표준 5-10x 망원조준경](#attachment-scope_standard_8x) (`scope_standard_8x`)<br>[Vudu 1-6x 가변 복합 광학 조준경](#attachment-scope_vudu) (`scope_vudu`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 9 | [AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-springfield1873"></a>
### Springfield 1873 Trapdoor Rifle — `tacz:springfield1873`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 저격소총 / 1발 장전식 소총 |
| 사용 탄약 | [45-70 탄약](#ammo-45_70) (`tacz:45_70`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 1발 / 1발 / 1발 / 1발 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/springfield1873.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/springfield1873_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/springfield1873.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 1 | [빈티지 스프링필드 조준경](#attachment-scope_1873_6x) (`scope_1873_6x`) |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 3 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`) |

<a id="gun-taurus500"></a>
### Taurus "Raging Hunter" Hand Cannon — `tacz:taurus500`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.500 매그넘](#ammo-500mag) (`tacz:500mag`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 5발 / 5발 / 5발 / 5발 |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/taurus500.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/taurus500_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/taurus500.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[T1 레드 도트](#attachment-sight_t1) (`sight_t1`) |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 4 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`) |

<a id="gun-taurus943"></a>
### .22 Modle 943 Revolver — `tacz:taurus943`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.22 윈체스터 매그넘](#ammo-22wmr) (`tacz:22wmr`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 8발 / 확장 탄창 슬롯 미지원 (데이터 배열: 8발 / 8발 / 8발) |
| 발사 모드 / 장전 구조 | 단발 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | 기본 레시피 파일 없음 |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/taurus943_data.json) / 부착물 허용 파일 없음 |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 0 | 슬롯 미지원 |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 0 | 슬롯 미지원 |

<a id="gun-timeless50"></a>
### Timeless .50 Z-Type — `tacz:timeless50`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 권총 / 단거리 보조 사격 |
| 사용 탄약 | [.50 AE 탄약](#ammo-50ae) (`tacz:50ae`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 8발 / 9발 / 11발 / 13발 |
| 발사 모드 / 장전 구조 | 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/timeless50.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/timeless50_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/timeless50.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 6 | [에임포인트 ACRO P-1 반사 조준경](#attachment-sight_acro_pistol) (`sight_acro_pistol`)<br>[델타포인트 반사 조준경](#attachment-sight_deltapoint_pistol) (`sight_deltapoint_pistol`)<br>[패스트파이어 반사 조준경](#attachment-sight_fastfire_pistol) (`sight_fastfire_pistol`)<br>[PK06 반사 조준경](#attachment-sight_pk06_pistol) (`sight_pk06_pistol`)<br>[RMR 미니 레드도트](#attachment-sight_rmr_dot) (`sight_rmr_dot`)<br>[SRO 미니 레드 도트](#attachment-sight_sro_dot) (`sight_sro_dot`) |
| 총구 부착물·총검 | 1 | [Timeless .50 구경 소염기](#attachment-muzzle_brake_timeless50) (`muzzle_brake_timeless50`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 7 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[고폭탄](#attachment-ammo_mod_he) (`ammo_mod_he`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-type_81"></a>
### Type 81-1 Service Rifle — `tacz:type_81`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 소총 / 일반 소총 사격 |
| 사용 탄약 | [7.62x39mm 탄약](#ammo-762x39) (`tacz:762x39`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 30발 / 33발 / 36발 / 40발 |
| 발사 모드 / 장전 구조 | 자동 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/type_81.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/type_81_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/type_81.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 0 | 슬롯 미지원 |
| 총구 부착물·총검 | 8 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[중량 탄약 확장 탄창 1단계](#attachment-extended_mag_1) (`extended_mag_1`)<br>[중량 탄약 확장 탄창 2단계](#attachment-extended_mag_2) (`extended_mag_2`)<br>[중량 탄약 확장 탄창 3단계](#attachment-extended_mag_3) (`extended_mag_3`) |

<a id="gun-ump45"></a>
### UMP45 SMG — `tacz:ump45`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관단총 / 근거리 연속 사격 |
| 사용 탄약 | [.45 ACP 탄약](#ammo-45acp) (`tacz:45acp`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 25발 / 32발 / 40발 / 48발 |
| 발사 모드 / 장전 구조 | 자동 / 점사 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/ump45.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/ump45_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/ump45.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 16 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 11 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 12 | [SI 전방 손잡이](#attachment-grip_cobra) (`grip_cobra`)<br>[헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) (`grip_cqr`)<br>[탈론 AFG1 핸드스탑](#attachment-grip_magpul_afg_2) (`grip_magpul_afg_2`)<br>[P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[SE-5 Express 전방 손잡이](#attachment-grip_se_5) (`grip_se_5`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-uzi"></a>
### UZI — `tacz:uzi`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관단총 / 근거리 연속 사격 |
| 사용 탄약 | [9mm 탄약](#ammo-9mm) (`tacz:9mm`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 32발 / 40발 / 50발 |
| 발사 모드 / 장전 구조 | 자동 / 개방형(open_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/uzi.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/uzi_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/uzi.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 15 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 11 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 0 | 슬롯 미지원 |
| 개머리판 | 0 | 슬롯 미지원 |
| 레이저 | 0 | 슬롯 미지원 |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="gun-vector45"></a>
### Vector SMG — `tacz:vector45`

| 항목 | 내용 |
| --- | --- |
| 분류·용도 | 기관단총 / 근거리 연속 사격 |
| 사용 탄약 | [.45 ACP 탄약](#ammo-45acp) (`tacz:45acp`) |
| 기본 장탄수 / 확장 1·2·3단계 설정 | 20발 / 30발 / 40발 / 50발 |
| 발사 모드 / 장전 구조 | 자동 / 점사 / 단발 / 폐쇄형(closed_bolt) |
| 기본 내장 부품 | 별도 내장 부착물 정의 없음 |
| 제작 여부 | [레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/gun/vector45.json) |
| 근거 | [총기 데이터](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/data/guns/vector45_data.json) / [부착물 허용 목록](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/tacz_tags/attachments/allow_attachments/vector45.json) |

| 장착 슬롯 | 허용 부품 수 | 장착 가능한 전체 부품 |
| --- | --- | --- |
| 조준경 | 16 | [TA31 2배율 홀로그래픽](#attachment-scope_acog_ta31) (`scope_acog_ta31`)<br>[컨텐더 4배율 조준경](#attachment-scope_contender) (`scope_contender`)<br>[Elcan 4배율 조준경](#attachment-scope_elcan_4x) (`scope_elcan_4x`)<br>[HAMR 3배율 복합 광학 조준경](#attachment-scope_hamr) (`scope_hamr`)<br>[QMK-152 3x 화이트 조준경](#attachment-scope_qmk152) (`scope_qmk152`)<br>[밀리텍 552 홀로그래픽](#attachment-sight_552) (`sight_552`)<br>[에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)](#attachment-sight_acro_rifle) (`sight_acro_rifle`)<br>[코요테 조준경](#attachment-sight_coyote) (`sight_coyote`)<br>[델타포인트 반사 조준경 (라이저 마운트)](#attachment-sight_deltapoint_rifle) (`sight_deltapoint_rifle`)<br>[EXP3 홀로그래픽](#attachment-sight_exp3) (`sight_exp3`)<br>[패스트파이어 반사 조준경 (라이저 마운트)](#attachment-sight_fastfire_rifle) (`sight_fastfire_rifle`)<br>[OKP-7 반사 조준경](#attachment-sight_okp7) (`sight_okp7`)<br>[PK06 반사 조준경 (라이저 마운트)](#attachment-sight_pk06_rifle) (`sight_pk06_rifle`)<br>[트리지콘 SRS-02 반사 조준경](#attachment-sight_srs_02) (`sight_srs_02`)<br>[T2 레드 도트](#attachment-sight_t2) (`sight_t2`)<br>[UH-1 홀로그래픽](#attachment-sight_uh1) (`sight_uh1`) |
| 총구 부착물·총검 | 11 | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu) (`muzzle_brake_cthulhu`)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2) (`muzzle_brake_cyclone_d2`)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer) (`muzzle_brake_pioneer`)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex) (`muzzle_brake_trex`)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident) (`muzzle_compensator_trident`)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd) (`muzzle_silencer_knight_qd`)<br>[미라지 소음기](#attachment-muzzle_silencer_mirage) (`muzzle_silencer_mirage`)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1) (`muzzle_silencer_phantom_s1`)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis) (`muzzle_silencer_ptilopsis`)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) (`muzzle_silencer_ursus`)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) (`muzzle_silencer_wraith`) |
| 손잡이 | 8 | [P-2 전방 손잡이](#attachment-grip_osovets_black) (`grip_osovets_black`)<br>[RK-0 전방 손잡이](#attachment-grip_rk0) (`grip_rk0`)<br>[RK-1 B25U 전방 손잡이](#attachment-grip_rk1_b25u) (`grip_rk1_b25u`)<br>[RK-6 전방 손잡이](#attachment-grip_rk6) (`grip_rk6`)<br>[TD 전방 손잡이](#attachment-grip_td) (`grip_td`)<br>[나고마 군용 표준 수직손잡이](#attachment-grip_vertical_military) (`grip_vertical_military`)<br>[Koch 레인저 중형 수직손잡이](#attachment-grip_vertical_ranger) (`grip_vertical_ranger`)<br>[탈론 SG2 전방 손잡이](#attachment-grip_vertical_talon) (`grip_vertical_talon`) |
| 개머리판 | 11 | [공장제 중량 개머리판](#attachment-oem_stock_heavy) (`oem_stock_heavy`)<br>[공장제 전술 개머리판](#attachment-oem_stock_tactical) (`oem_stock_tactical`)<br>[AK-12 공장제 개머리판](#attachment-stock_ak12) (`stock_ak12`)<br>[카본 골격 C5 개머리판](#attachment-stock_carbon_bone_c5) (`stock_carbon_bone_c5`)<br>[HK 슬림라인 개머리판](#attachment-stock_hk_slim_line) (`stock_hk_slim_line`)<br>[M4SS 개머리판](#attachment-stock_m4ss) (`stock_m4ss`)<br>[밀리텍 B5 개머리판](#attachment-stock_militech_b5) (`stock_militech_b5`)<br>[맥풀 MOE 개머리판](#attachment-stock_moe) (`stock_moe`)<br>[CMMG 립스톡 개머리판](#attachment-stock_ripstock) (`stock_ripstock`)<br>[SBA3 개머리판](#attachment-stock_sba3) (`stock_sba3`)<br>[맥풀 CTR 개머리판](#attachment-stock_tactical_ar) (`stock_tactical_ar`) |
| 레이저 | 2 | [밀리텍 컴팩트 레이저](#attachment-laser_compact) (`laser_compact`)<br>[나이트스틱 컴팩트 레이저](#attachment-laser_nightstick) (`laser_nightstick`) |
| 탄창·특수탄 개조 | 6 | [FMJ탄](#attachment-ammo_mod_fmj) (`ammo_mod_fmj`)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp) (`ammo_mod_hp`)<br>[소이탄](#attachment-ammo_mod_i) (`ammo_mod_i`)<br>[경량 탄약 확장 탄창 1단계](#attachment-light_extended_mag_1) (`light_extended_mag_1`)<br>[경량 탄약 확장 탄창 2단계](#attachment-light_extended_mag_2) (`light_extended_mag_2`)<br>[경량 탄약 확장 탄창 3단계](#attachment-light_extended_mag_3) (`light_extended_mag_3`) |

<a id="attachments"></a>
## 6. 부착물별 효과·사용 가능한 모든 총기 — 99종

수치에서 반동·탄 퍼짐·조준 전환 시간이 감소하면 각각 제어·집탄·조준 전환에 유리합니다. 수치 증가가 항상 장점인 것은 아닙니다. 무게는 게임 설정값이며 현실 제품 무게로 해석하지 않습니다. 표의 효과는 개별 부품 기본값이고 총기별 전용 설정은 5절이 우선합니다.

### 조준경 — 32종

| 한글 이름·부착물 ID | 표시·제작 | 용도·기본 효과 | 장착 가능한 모든 기본 총기 |
| --- | --- | --- | --- |
| <a id="attachment-scope_1873_6x"></a>빈티지 스프링필드 조준경<br>`tacz:scope_1873_6x` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_1873_6x.json) | 조준 배율 설정 6×<br>무게 설정 +1<br>조준 전환 시간 0.15초 추가<br>조준 탄 퍼짐 34% 감소 | 1종: [Springfield 1873 Trapdoor Rifle](#gun-springfield1873) |
| <a id="attachment-scope_98k"></a>마우저 4배율 경량 조준경<br>`tacz:scope_98k` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_98k.json) | 조준 배율 설정 4.25×<br>무게 설정 +1.3<br>조준 전환 시간 0.03초 추가 | 1종: [Mauser Kar98k Rifle](#gun-kar98) |
| <a id="attachment-scope_acog_ta31"></a>TA31 2배율 홀로그래픽<br>`tacz:scope_acog_ta31` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_acog_ta31.json) | 조준 배율 설정 2.5×<br>무게 설정 +1.2<br>조준 전환 시간 0.015초 추가 | 30종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-scope_aug_default"></a>AUG 일체형 조준경 [숨김]<br>`tacz:scope_aug_default` | 숨김<br>기본 레시피 파일 없음 | 조준 배율 설정 4.25×<br>무게 설정 +0.1<br>조준 전환 시간 0.1초 추가 | 1종: [AUG](#gun-aug) |
| <a id="attachment-scope_contender"></a>컨텐더 4배율 조준경<br>`tacz:scope_contender` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_contender.json) | 조준 배율 설정 4.25×<br>무게 설정 +1.6<br>조준 전환 시간 0.05초 추가<br>조준 탄 퍼짐 15% 감소 | 17종: [Accuracy International AWM](#gun-ai_awp), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-scope_elcan_4x"></a>Elcan 4배율 조준경<br>`tacz:scope_elcan_4x` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_elcan_4x.json) | 조준 배율 설정 4.25× / 1.25×<br>무게 설정 +1.6<br>조준 전환 시간 0.05초 추가 | 30종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-scope_hamr"></a>HAMR 3배율 복합 광학 조준경<br>`tacz:scope_hamr` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_hamr.json) | 조준 배율 설정 3.25× / 1.25×<br>무게 설정 +0.85<br>조준 전환 시간 0.015초 추가<br>조준 탄 퍼짐 15% 감소 | 29종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-scope_lpvo_1_6"></a>1-6x 저배율 가변 광학조준경<br>`tacz:scope_lpvo_1_6` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_lpvo_1_6.json) | 조준 배율 설정 6.25× / 1.25×<br>무게 설정 +1.3<br>조준 전환 시간 0.03초 추가 | 21종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK G3 Battle rifle](#gun-hk_g3), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb) |
| <a id="attachment-scope_mk5hd"></a>Mark 5 HD 5-25x 가변 복합 광학 조준경<br>`tacz:scope_mk5hd` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_mk5hd.json) | 조준 배율 설정 5× / 25× / 1.25×<br>무게 설정 +0.85<br>조준 전환 시간 0.13초 추가<br>조준 탄 퍼짐 34% 감소 | 20종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK G3 Battle rifle](#gun-hk_g3), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb) |
| <a id="attachment-scope_qmk152"></a>QMK-152 3x 화이트 조준경<br>`tacz:scope_qmk152` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_qmk152.json) | 조준 배율 설정 3×<br>무게 설정 +1<br>조준 전환 시간 0.02초 추가 | 29종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-scope_retro_2x"></a>Retro 3배율 조준경<br>`tacz:scope_retro_2x` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_retro_2x.json) | 조준 배율 설정 3.25×<br>무게 설정 +1.6<br>조준 전환 시간 0.02초 추가 | 1종: [M16A1 Service Rifle](#gun-m16a1) |
| <a id="attachment-scope_standard_8x"></a>표준 5-10x 망원조준경<br>`tacz:scope_standard_8x` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_standard_8x.json) | 조준 배율 설정 4.5× / 10×<br>무게 설정 +2<br>조준 전환 시간 0.09초 추가<br>조준 탄 퍼짐 25% 감소<br>**전용 수치 우선: [SCAR-H Battle Rifle](#gun-scar_h)** | 21종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK G3 Battle rifle](#gun-hk_g3), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb) |
| <a id="attachment-scope_vudu"></a>Vudu 1-6x 가변 복합 광학 조준경<br>`tacz:scope_vudu` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/scope_vudu.json) | 조준 배율 설정 6.5× / 1.35×<br>무게 설정 +0.85<br>조준 전환 시간 0.015초 추가<br>조준 탄 퍼짐 10% 감소 | 20종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK G3 Battle rifle](#gun-hk_g3), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb) |
| <a id="attachment-sight_552"></a>밀리텍 552 홀로그래픽<br>`tacz:sight_552` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_552.json) | 조준 배율 설정 2×<br>무게 설정 +0.4<br>조준 전환 시간 0.015초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_acro_pistol"></a>에임포인트 ACRO P-1 반사 조준경<br>`tacz:sight_acro_pistol` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_acro_pistol.json) | 조준 배율 설정 2×<br>무게 설정 +0.4<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 8종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-sight_acro_rifle"></a>에임포인트 ACRO P-1 반사 조준경 (라이저 마운트)<br>`tacz:sight_acro_rifle` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_acro_rifle.json) | 조준 배율 설정 2×<br>무게 설정 +0.5<br>조준 탄 퍼짐 10% 감소<br>조준 전환 시간 0.02초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_coyote"></a>코요테 조준경<br>`tacz:sight_coyote` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_coyote.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.25<br>조준 전환 시간 0.03초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_deltapoint_pistol"></a>델타포인트 반사 조준경<br>`tacz:sight_deltapoint_pistol` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_deltapoint_pistol.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.4<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 8종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-sight_deltapoint_rifle"></a>델타포인트 반사 조준경 (라이저 마운트)<br>`tacz:sight_deltapoint_rifle` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_deltapoint_rifle.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.5<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_exp3"></a>EXP3 홀로그래픽<br>`tacz:sight_exp3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_exp3.json) | 조준 배율 설정 2×<br>무게 설정 +0.35<br>조준 전환 시간 0.02초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_fastfire_pistol"></a>패스트파이어 반사 조준경<br>`tacz:sight_fastfire_pistol` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_fastfire_pistol.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.4<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 8종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-sight_fastfire_rifle"></a>패스트파이어 반사 조준경 (라이저 마운트)<br>`tacz:sight_fastfire_rifle` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_fastfire_rifle.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.5<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_okp7"></a>OKP-7 반사 조준경<br>`tacz:sight_okp7` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_okp7.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.4<br>조준 전환 시간 0.01초 감소 | 30종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_p90"></a>P90 기본 조준경 [숨김]<br>`tacz:sight_p90` | 숨김; 한글 이름은 설명용 보충<br>기본 레시피 파일 없음 | 조준 배율 설정 1.35×<br>무게 설정 +0.35<br>조준 전환 시간 0.02초 감소 | 1종: [P90 PDW](#gun-p90) |
| <a id="attachment-sight_pk06_pistol"></a>PK06 반사 조준경<br>`tacz:sight_pk06_pistol` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_pk06_pistol.json) | 조준 배율 설정 2×<br>무게 설정 +0.4<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 8종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-sight_pk06_rifle"></a>PK06 반사 조준경 (라이저 마운트)<br>`tacz:sight_pk06_rifle` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_pk06_rifle.json) | 조준 배율 설정 2×<br>무게 설정 +0.5<br>조준 탄 퍼짐 5% 감소<br>조준 전환 시간 0.02초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_rmr_dot"></a>RMR 미니 레드도트<br>`tacz:sight_rmr_dot` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_rmr_dot.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.1<br>조준 전환 시간 0.05초 감소 | 8종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-sight_sro_dot"></a>SRO 미니 레드 도트<br>`tacz:sight_sro_dot` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_sro_dot.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.1<br>조준 전환 시간 0.04초 감소 | 8종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-sight_srs_02"></a>트리지콘 SRS-02 반사 조준경<br>`tacz:sight_srs_02` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_srs_02.json) | 조준 배율 설정 1.5×<br>무게 설정 +0.8<br>조준 전환 시간 0.025초 감소 | 30종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_t1"></a>T1 레드 도트<br>`tacz:sight_t1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_t1.json) | 조준 배율 설정 2.5×<br>무게 설정 +0.2<br>조준 전환 시간 0.02초 감소 | 3종: [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500) |
| <a id="attachment-sight_t2"></a>T2 레드 도트<br>`tacz:sight_t2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_t2.json) | 조준 배율 설정 2.5×<br>무게 설정 +0.25<br>조준 전환 시간 0.015초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-sight_uh1"></a>UH-1 홀로그래픽<br>`tacz:sight_uh1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sight_uh1.json) | 조준 배율 설정 2.5×<br>무게 설정 +0.3<br>조준 전환 시간 0.01초 감소 | 31종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |

### 총구 부착물·총검 — 19종

| 한글 이름·부착물 ID | 표시·제작 | 용도·기본 효과 | 장착 가능한 모든 기본 총기 |
| --- | --- | --- | --- |
| <a id="attachment-bayonet_6h3"></a>6H3 총검<br>`tacz:bayonet_6h3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/bayonet_6h3.json) | 무게 설정 +0.34<br>조준 전환 시간 0.03초 추가<br>근접 공격: 거리 2, 각도 45, 피해 설정 5, 밀쳐내기 0.4, 준비 시간 0.1 | 1종: [AKM](#gun-ak47) |
| <a id="attachment-bayonet_m9"></a>M9 총검<br>`tacz:bayonet_m9` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/bayonet_m9.json) | 무게 설정 +0.34<br>조준 전환 시간 0.03초 추가<br>근접 공격: 거리 2, 각도 45, 대기 시간 0, 피해 설정 6, 밀쳐내기 0.4, 준비 시간 0.1 | 4종: [AUG](#gun-aug), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1) |
| <a id="attachment-deagle_golden_long_barrel"></a>.357 황금 데저트 이글 장총열<br>`tacz:deagle_golden_long_barrel` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/deagle_golden_long_barrel.json) | 무게 설정 +0.4<br>조준 전환 시간 0.04초 추가<br>일반 비조준 탄 퍼짐 10% 감소<br>조준 탄 퍼짐 15% 감소<br>수직 반동 20% 감소<br>수평 반동 20% 감소<br>유효 사거리 5블록 추가<br>발사음 도달 거리 -12블록<br>소음기 발사음 사용 | 1종: [Golden Deagle 357](#gun-deagle_golden) |
| <a id="attachment-muzzle_brake_cthulhu"></a>크툴루 K7 소염기<br>`tacz:muzzle_brake_cthulhu` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_brake_cthulhu.json) | 조준 전환 시간 0.02초 추가<br>일반 비조준 탄 퍼짐 10% 증가<br>조준 탄 퍼짐 10% 감소<br>수직 반동 15% 감소<br>수평 반동 20% 감소 | 26종: [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_brake_cyclone_d2"></a>사이클론 D2 소염기<br>`tacz:muzzle_brake_cyclone_d2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_brake_cyclone_d2.json) | 조준 전환 시간 0.02초 추가<br>일반 비조준 탄 퍼짐 10% 증가<br>수직 반동 20% 감소<br>수평 반동 30% 감소 | 26종: [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_brake_mastiff_sg"></a>마스티프 산탄총 총구 제동기<br>`tacz:muzzle_brake_mastiff_sg` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_brake_mastiff_sg.json) | 무게 설정 +0.25<br>조준 전환 시간 0.08초 추가<br>일반 비조준 탄 퍼짐 25% 증가<br>조준 탄 퍼짐 10% 증가<br>수직 반동 35% 감소<br>수평 반동 30% 감소 | 4종: [AA12 Shotgun](#gun-aa12), [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-muzzle_brake_pioneer"></a>파이오니어 A3 소염기<br>`tacz:muzzle_brake_pioneer` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_brake_pioneer.json) | 조준 전환 시간 0.02초 추가<br>일반 비조준 탄 퍼짐 15% 증가<br>수직 반동 67% 감소<br>수평 반동 33% 증가 | 26종: [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_brake_timeless50"></a>Timeless .50 구경 소염기<br>`tacz:muzzle_brake_timeless50` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_brake_timeless50.json) | 조준 전환 시간 0.03초 추가<br>수직 반동 65% 감소<br>수평 반동 25% 감소<br>일반 비조준 탄 퍼짐 40% 감소<br>웅크린 자세 탄 퍼짐 50% 감소 | 1종: [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-muzzle_brake_trex"></a>T-Rex 헤비 소염기<br>`tacz:muzzle_brake_trex` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_brake_trex.json) | 무게 설정 +0.5<br>조준 전환 시간 0.03초 추가<br>수직 반동 34% 감소<br>수평 반동 5% 감소 | 26종: [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_choke_sg"></a>산탄총 초크<br>`tacz:muzzle_choke_sg` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_choke_sg.json) | 조준 전환 시간 0.05초 추가<br>일반 비조준 탄 퍼짐 35% 감소<br>조준 탄 퍼짐 35% 감소<br>웅크린 자세 탄 퍼짐 35% 감소<br>엎드린 자세 탄 퍼짐 35% 감소<br>수직 반동 45% 증가<br>수평 반동 45% 증가 | 4종: [AA12 Shotgun](#gun-aa12), [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-muzzle_compensator_trident"></a>템페스트 트라이던트 소염기<br>`tacz:muzzle_compensator_trident` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_compensator_trident.json) | 조준 전환 시간 0.01초 추가<br>일반 비조준 탄 퍼짐 15% 감소<br>수평 반동 40% 감소 | 26종: [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_silencer_knight_qd"></a>나이트 QD 소음기<br>`tacz:muzzle_silencer_knight_qd` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_knight_qd.json) | 무게 설정 +0.35<br>조준 전환 시간 0.02초 추가<br>조준 탄 퍼짐 25% 감소<br>수직 반동 20% 감소<br>발사음 도달 거리 -20블록<br>소음기 발사음 사용 | 27종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_silencer_mirage"></a>미라지 소음기<br>`tacz:muzzle_silencer_mirage` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_mirage.json) | 무게 설정 +0.15<br>헤드샷 배율 25% 증가<br>일반 비조준 탄 퍼짐 10% 증가<br>유효 사거리 20% 감소<br>발사음 도달 거리 -24블록<br>소음기 발사음 사용 | 12종: [B93R](#gun-b93r), [Deagle 50](#gun-deagle), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [M1911](#gun-m1911), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_silencer_phantom_s1"></a>팬텀 S1 소음기<br>`tacz:muzzle_silencer_phantom_s1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_phantom_s1.json) | 무게 설정 +0.25<br>조준 전환 시간 0.02초 추가<br>일반 비조준 탄 퍼짐 10% 증가<br>유효 사거리 50% 증가<br>연사 속도 5% 감소<br>발사음 도달 거리 -20블록<br>소음기 발사음 사용 | 28종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [Golden Deagle 357](#gun-deagle_golden), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_silencer_ptilopsis"></a>PO-2 "프틸롭시스" 소음기<br>`tacz:muzzle_silencer_ptilopsis` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_ptilopsis.json) | 무게 설정 +0.4<br>조준 전환 시간 0.06초 추가<br>일반 비조준 탄 퍼짐 20% 감소<br>유효 사거리 25% 증가<br>발사음 도달 거리 -24블록<br>소음기 발사음 사용 | 12종: [B93R](#gun-b93r), [Deagle 50](#gun-deagle), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [M1911](#gun-m1911), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_silencer_sg"></a>12 게이지 소음기<br>`tacz:muzzle_silencer_sg` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_sg.json) | 무게 설정 +0.4<br>조준 전환 시간 0.1초 추가<br>일반 비조준 탄 퍼짐 5% 감소<br>조준 탄 퍼짐 10% 감소<br>유효 사거리 50% 증가<br>연사 속도 5% 감소<br>수직 반동 10% 감소<br>발사음 도달 거리 -20블록<br>소음기 발사음 사용 | 4종: [AA12 Shotgun](#gun-aa12), [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-muzzle_silencer_ursus"></a>Ursus 군용 표준 소음기<br>`tacz:muzzle_silencer_ursus` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_ursus.json) | 무게 설정 +0.35<br>조준 전환 시간 0.035초 추가<br>일반 비조준 탄 퍼짐 15% 증가<br>수직 반동 25% 감소<br>수평 반동 20% 감소<br>유효 사거리 20% 증가<br>발사음 도달 거리 -18블록<br>소음기 발사음 사용 | 27종: [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [MK14 EBR](#gun-mk14), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-muzzle_silencer_vulture"></a>벌처 .50 구경 소음기<br>`tacz:muzzle_silencer_vulture` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_vulture.json) | 무게 설정 +1.55<br>조준 전환 시간 0.09초 추가<br>일반 비조준 탄 퍼짐 10% 증가<br>유효 사거리 25% 증가<br>헤드샷 배율 0.25 추가<br>수직 반동 34% 감소<br>수평 반동 25% 감소<br>발사음 도달 거리 -25블록<br>소음기 발사음 사용 | 2종: [M107 Sniper Rifle](#gun-m107), [M95 .50 Cal Antimaterial](#gun-m95) |
| <a id="attachment-muzzle_silencer_wraith"></a>Wraith 소음기<br>`tacz:muzzle_silencer_wraith` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/muzzle_silencer_wraith.json) | 무게 설정 +0.35<br>조준 전환 시간 0.04초 추가<br>헤드샷 배율 0.25 추가<br>유효 사거리 15% 증가<br>조준 탄 퍼짐 25% 감소<br>탄속 20% 증가<br>발사음 도달 거리 -20블록<br>소음기 발사음 사용 | 12종: [B93R](#gun-b93r), [Deagle 50](#gun-deagle), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [M1911](#gun-m1911), [M9A4](#gun-m9a4), [P320](#gun-p320), [P90 PDW](#gun-p90), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |

### 손잡이 — 12종

| 한글 이름·부착물 ID | 표시·제작 | 용도·기본 효과 | 장착 가능한 모든 기본 총기 |
| --- | --- | --- | --- |
| <a id="attachment-grip_cobra"></a>SI 전방 손잡이<br>`tacz:grip_cobra` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_cobra.json) | 무게 설정 +0.08<br>조준 전환 시간 15% 감소 | 18종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45) |
| <a id="attachment-grip_cqr"></a>헤라 암스 CQR 전방 손잡이 [숨김]<br>`tacz:grip_cqr` | 숨김<br>기본 레시피 파일 없음 | 무게 설정 +0.167<br>조준 전환 시간 15% 감소<br>수직 반동 8% 감소<br>수평 반동 8% 감소 | 18종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45) |
| <a id="attachment-grip_magpul_afg_2"></a>탈론 AFG1 핸드스탑<br>`tacz:grip_magpul_afg_2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_magpul_afg_2.json) | 무게 설정 +0.2<br>조준 전환 시간 5% 감소<br>일반 비조준 탄 퍼짐 10% 증가<br>수직 반동 15% 감소 | 18종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45) |
| <a id="attachment-grip_osovets_black"></a>P-2 전방 손잡이<br>`tacz:grip_osovets_black` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_osovets_black.json) | 무게 설정 +0.125<br>수평 반동 25% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_rk0"></a>RK-0 전방 손잡이<br>`tacz:grip_rk0` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_rk0.json) | 무게 설정 +0.138<br>수직 반동 20% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_rk1_b25u"></a>RK-1 B25U 전방 손잡이<br>`tacz:grip_rk1_b25u` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_rk1_b25u.json) | 무게 설정 +0.18<br>조준 전환 시간 0.18초 추가<br>수평 반동 34% 감소<br>일반 비조준 탄 퍼짐 52% 감소<br>조준 탄 퍼짐 25% 증가 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_rk6"></a>RK-6 전방 손잡이<br>`tacz:grip_rk6` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_rk6.json) | 무게 설정 +0.1<br>조준 전환 시간 15% 감소<br>일반 비조준 탄 퍼짐 12% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_se_5"></a>SE-5 Express 전방 손잡이<br>`tacz:grip_se_5` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_se_5.json) | 무게 설정 +0.09<br>조준 전환 시간 15% 감소<br>수직 반동 7% 감소<br>수평 반동 8% 감소 | 18종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45) |
| <a id="attachment-grip_td"></a>TD 전방 손잡이<br>`tacz:grip_td` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_td.json) | 무게 설정 +0.133<br>조준 탄 퍼짐 12% 감소<br>일반 비조준 탄 퍼짐 12% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_vertical_military"></a>나고마 군용 표준 수직손잡이<br>`tacz:grip_vertical_military` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_vertical_military.json) | 무게 설정 +0.25<br>조준 전환 시간 2% 증가<br>조준 탄 퍼짐 15% 감소<br>수직 반동 20% 감소<br>수평 반동 20% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_vertical_ranger"></a>Koch 레인저 중형 수직손잡이<br>`tacz:grip_vertical_ranger` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_vertical_ranger.json) | 무게 설정 +0.8<br>조준 전환 시간 5% 증가<br>일반 비조준 탄 퍼짐 30% 감소<br>웅크린 자세 탄 퍼짐 25% 감소<br>수직 반동 20% 감소<br>수평 반동 30% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-grip_vertical_talon"></a>탈론 SG2 전방 손잡이<br>`tacz:grip_vertical_talon` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/grip_vertical_talon.json) | 무게 설정 +0.2<br>조준 전환 시간 1% 증가<br>조준 탄 퍼짐 20% 감소<br>수직 반동 15% 감소 | 19종: [AA12 Shotgun](#gun-aa12), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |

### 개머리판 — 14종

| 한글 이름·부착물 ID | 표시·제작 | 용도·기본 효과 | 장착 가능한 모든 기본 총기 |
| --- | --- | --- | --- |
| <a id="attachment-oem_stock_heavy"></a>공장제 중량 개머리판<br>`tacz:oem_stock_heavy` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/oem_stock_heavy.json) | 무게 설정 +0.5<br>조준 전환 시간 0.05초 추가<br>조준 탄 퍼짐 10% 감소<br>수직 반동 25% 감소<br>수평 반동 40% 감소<br>근접 공격: 거리 2, 각도 40, 대기 시간 0.4, 피해 설정 5, 밀쳐내기 0.8, 준비 시간 0.1 | 5종: [AKM](#gun-ak47), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [RPK](#gun-rpk), [Vector SMG](#gun-vector45) |
| <a id="attachment-oem_stock_light"></a>공장제 경량 개머리판<br>`tacz:oem_stock_light` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/oem_stock_light.json) | 무게 설정 +0.3<br>조준 전환 시간 0.02초 감소<br>조준 탄 퍼짐 10% 증가<br>수직 반동 15% 감소<br>수평 반동 20% 감소<br>근접 공격: 거리 2, 각도 40, 대기 시간 0.1, 피해 설정 3, 밀쳐내기 0.4, 준비 시간 0.1 | 4종: [AKM](#gun-ak47), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [RPK](#gun-rpk) |
| <a id="attachment-oem_stock_tactical"></a>공장제 전술 개머리판<br>`tacz:oem_stock_tactical` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/oem_stock_tactical.json) | 무게 설정 +0.4<br>조준 전환 시간 0.035초 추가<br>일반 비조준 탄 퍼짐 10% 감소<br>수직 반동 20% 감소<br>수평 반동 30% 감소<br>근접 공격: 거리 2, 각도 40, 대기 시간 0.2, 피해 설정 4, 밀쳐내기 0.6, 준비 시간 0.1<br>**전용 수치 우선: [DB-2 Durin](#gun-db_short)** | 6종: [AKM](#gun-ak47), [DB-2 Durin](#gun-db_short), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [RPK](#gun-rpk), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_ak12"></a>AK-12 공장제 개머리판<br>`tacz:stock_ak12` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_ak12.json) | 무게 설정 +0.148<br>조준 전환 시간 0.01초 추가<br>조준 탄 퍼짐 10% 감소<br>수직 반동 20% 감소<br>수평 반동 20% 감소 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_carbon_bone_c5"></a>카본 골격 C5 개머리판<br>`tacz:stock_carbon_bone_c5` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_carbon_bone_c5.json) | 무게 설정 +0.3<br>조준 전환 시간 0.02초 감소<br>조준 탄 퍼짐 10% 증가<br>수직 반동 15% 감소<br>수평 반동 20% 감소<br>근접 공격: 거리 2, 각도 40, 대기 시간 0.1, 피해 설정 3, 밀쳐내기 0.4, 준비 시간 0.1 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_heavy_spas_12"></a>프랑키 중형 개머리판<br>`tacz:stock_heavy_spas_12` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_heavy_spas_12.json) | 무게 설정 +0.5<br>조준 전환 시간 0.02초 추가<br>일반 비조준 탄 퍼짐 10% 감소<br>조준 탄 퍼짐 30% 감소<br>수직 반동 28% 감소<br>수평 반동 28% 감소 | 1종: [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-stock_hk_slim_line"></a>HK 슬림라인 개머리판<br>`tacz:stock_hk_slim_line` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_hk_slim_line.json) | 무게 설정 +0.695<br>조준 전환 시간 0.01초 추가<br>수직 반동 25% 감소<br>수평 반동 28% 감소 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_m4ss"></a>M4SS 개머리판<br>`tacz:stock_m4ss` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_m4ss.json) | 무게 설정 +0.695<br>조준 전환 시간 0.012초 추가<br>수직 반동 15% 감소<br>수평 반동 10% 감소 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_militech_b5"></a>밀리텍 B5 개머리판<br>`tacz:stock_militech_b5` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_militech_b5.json) | 무게 설정 +0.5<br>조준 전환 시간 10% 증가<br>조준 탄 퍼짐 13% 감소<br>수직 반동 32% 감소<br>수평 반동 40% 감소<br>근접 공격: 거리 2, 각도 40, 대기 시간 0.4, 피해 설정 5, 밀쳐내기 0.8, 준비 시간 0.1 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_moe"></a>맥풀 MOE 개머리판<br>`tacz:stock_moe` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_moe.json) | 무게 설정 +0.15<br>조준 전환 시간 0.03초 감소<br>수직 반동 15% 감소 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_ripstock"></a>CMMG 립스톡 개머리판<br>`tacz:stock_ripstock` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_ripstock.json) | 무게 설정 +0.12<br>조준 전환 시간 18% 감소<br>조준 탄 퍼짐 5% 증가 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_sba3"></a>SBA3 개머리판<br>`tacz:stock_sba3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_sba3.json) | 무게 설정 +0.1<br>수평 반동 20% 감소<br>일반 비조준 탄 퍼짐 22% 감소 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_tactical_ar"></a>맥풀 CTR 개머리판<br>`tacz:stock_tactical_ar` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_tactical_ar.json) | 무게 설정 +0.4<br>수직 반동 25% 감소<br>수평 반동 25% 감소<br>근접 공격: 거리 2, 각도 40, 대기 시간 0.2, 피해 설정 4, 밀쳐내기 0.6, 준비 시간 0.1 | 17종: [AKM](#gun-ak47), [FN EVOLYS Machine Gun](#gun-fn_evolys), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [HK-MP5A5](#gun-hk_mp5a5), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Vector SMG](#gun-vector45) |
| <a id="attachment-stock_tactical_spas_12"></a>프랑키 전술 개머리판<br>`tacz:stock_tactical_spas_12` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/stock_tactical_spas_12.json) | 무게 설정 +0.3<br>수직 반동 20% 감소<br>수평 반동 20% 감소<br>일반 비조준 탄 퍼짐 30% 감소 | 1종: [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |

### 레이저 — 5종

| 한글 이름·부착물 ID | 표시·제작 | 용도·기본 효과 | 장착 가능한 모든 기본 총기 |
| --- | --- | --- | --- |
| <a id="attachment-laser_compact"></a>밀리텍 컴팩트 레이저<br>`tacz:laser_compact` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/laser_compact.json) | 무게 설정 +0.13<br>조준 탄 퍼짐 40% 감소<br>일반 비조준 탄 퍼짐 30% 감소<br>웅크린 자세 탄 퍼짐 30% 감소 | 24종: [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [Glock 17](#gun-glock_17), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [M9A4](#gun-m9a4), [MK14 EBR](#gun-mk14), [P320](#gun-p320), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [.357 Rhino Revolver](#gun-rhino357), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [Timeless .50 Z-Type](#gun-timeless50), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-laser_lopro"></a>로프로 전술 레이저<br>`tacz:laser_lopro` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/laser_lopro.json) | 무게 설정 +1<br>조준 전환 시간 0.12초 추가<br>조준 탄 퍼짐 75% 감소<br>일반 비조준 탄 퍼짐 25% 감소<br>웅크린 자세 탄 퍼짐 50% 감소 | 12종: [AUG](#gun-aug), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l) |
| <a id="attachment-laser_nightstick"></a>나이트스틱 컴팩트 레이저<br>`tacz:laser_nightstick` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/laser_nightstick.json) | 무게 설정 +0.2<br>조준 전환 시간 0.06초 추가<br>웅크린 자세 탄 퍼짐 25% 감소<br>일반 비조준 탄 퍼짐 40% 감소 | 24종: [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [Glock 17](#gun-glock_17), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [M9A4](#gun-m9a4), [MK14 EBR](#gun-mk14), [P320](#gun-p320), [P90 PDW](#gun-p90), [QBZ-191 Assault Rifle](#gun-qbz_191), [.357 Rhino Revolver](#gun-rhino357), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [Timeless .50 Z-Type](#gun-timeless50), [UMP45 SMG](#gun-ump45), [Vector SMG](#gun-vector45) |
| <a id="attachment-laser_peq15"></a>PEQ-15 전술 레이저 [숨김]<br>`tacz:laser_peq15` | 숨김; 한글 이름은 설명용 보충<br>기본 레시피 파일 없음 | 무게 설정 +0.133<br>조준 전환 시간 0.03초 감소<br>조준 탄 퍼짐 12% 감소<br>웅크린 자세 탄 퍼짐 50% 감소 | 12종: [AUG](#gun-aug), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [M16A4 Service Rifle](#gun-m16a4), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l) |
| <a id="attachment-laser_peq6"></a>PEQ6 ILLM<br>`tacz:laser_peq6` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/laser_peq6.json) | 무게 설정 +0.25<br>조준 전환 시간 0.07초 추가<br>헤드샷 배율 0.25 추가<br>웅크린 자세 탄 퍼짐 65% 감소<br>일반 비조준 탄 퍼짐 50% 감소<br>엎드린 자세 탄 퍼짐 25% 감소<br>조준 탄 퍼짐 50% 감소 | 1종: [MK23 Offensive Pistol](#gun-hk_mk23) |

### 탄창·특수탄 개조 — 17종

| 한글 이름·부착물 ID | 표시·제작 | 용도·기본 효과 | 장착 가능한 모든 기본 총기 |
| --- | --- | --- | --- |
| <a id="attachment-ammo_mod_fmj"></a>FMJ탄<br>`tacz:ammo_mod_fmj` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/ammo_mod_fmj.json) | 탄환 관통 특성 변경<br>무게 설정 +0.6<br>조준 전환 시간 0.02초 추가<br>관통 수가 2 초과이면 2 추가, 그 외 유지<br>방어구 관통률이 0.5 초과이면 ×1.5, 그 외 ×1.75<br>피해량 10% 감소<br>탄속 10% 증가<br>확장 레벨 3 적용 | 49종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [B93R](#gun-b93r), [CZ 75](#gun-cz75), [DB-4 Ursus](#gun-db_long), [DB-2 Durin](#gun-db_short), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [Glock 17](#gun-glock_17), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [Mauser Kar98k Rifle](#gun-kar98), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M1911](#gun-m1911), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M870](#gun-m870), [M95 .50 Cal Antimaterial](#gun-m95), [M9A4](#gun-m9a4), [MK14 EBR](#gun-mk14), [P320](#gun-p320), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Springfield 1873 Trapdoor Rifle](#gun-springfield1873), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [Timeless .50 Z-Type](#gun-timeless50), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-ammo_mod_he"></a>고폭탄<br>`tacz:ammo_mod_he` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/ammo_mod_he.json) | 폭발 탄환 개조; 산탄총에서는 단일 발사체로 전환<br>무게 설정 +0.6<br>폭발 활성화<br>방어구 관통률 50% 감소<br>헤드샷 배율 값을 1로 설정<br>관통 수 값을 1로 설정<br>연사 속도 15% 감소<br>확장 레벨 1 적용<br>조준 탄 퍼짐 75% 감소 | 14종: [Accuracy International AWM](#gun-ai_awp), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [MK23 Offensive Pistol](#gun-hk_mk23), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M870](#gun-m870), [M95 .50 Cal Antimaterial](#gun-m95), [MK14 EBR](#gun-mk14), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [Timeless .50 Z-Type](#gun-timeless50) |
| <a id="attachment-ammo_mod_hp"></a>할로우 포인트 탄<br>`tacz:ammo_mod_hp` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/ammo_mod_hp.json) | 피해량 증가형 탄환 개조<br>무게 설정 +0.6<br>조준 전환 시간 0.02초 추가<br>방어구 관통률 50% 감소<br>피해량 30% 증가<br>탄속 10% 감소<br>관통 수 100% 감소<br>확장 레벨 3 적용 | 49종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [B93R](#gun-b93r), [CZ 75](#gun-cz75), [DB-4 Ursus](#gun-db_long), [DB-2 Durin](#gun-db_short), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [Glock 17](#gun-glock_17), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [Mauser Kar98k Rifle](#gun-kar98), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M1911](#gun-m1911), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M870](#gun-m870), [M95 .50 Cal Antimaterial](#gun-m95), [M9A4](#gun-m9a4), [MK14 EBR](#gun-mk14), [P320](#gun-p320), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Springfield 1873 Trapdoor Rifle](#gun-springfield1873), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [Timeless .50 Z-Type](#gun-timeless50), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-ammo_mod_i"></a>소이탄<br>`tacz:ammo_mod_i` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/ammo_mod_i.json) | 대상을 불태우는 탄환 개조<br>무게 설정 +0.6<br>조준 전환 시간 0.02초 추가<br>명중한 엔티티 점화<br>방어구 관통률 20% 감소<br>피해량 10% 증가<br>확장 레벨 3 적용 | 49종: [AA12 Shotgun](#gun-aa12), [Accuracy International AWM](#gun-ai_awp), [AKM](#gun-ak47), [AUG](#gun-aug), [B93R](#gun-b93r), [CZ 75](#gun-cz75), [DB-4 Ursus](#gun-db_long), [DB-2 Durin](#gun-db_short), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [Glock 17](#gun-glock_17), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [Mauser Kar98k Rifle](#gun-kar98), [.30-06 Lonetrail Hand Cannon](#gun-lonetrail), [M1014 Battle Shotgun](#gun-m1014), [M107 Sniper Rifle](#gun-m107), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M1911](#gun-m1911), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [M700 Sniper Rifle](#gun-m700), [M870](#gun-m870), [M95 .50 Cal Antimaterial](#gun-m95), [M9A4](#gun-m9a4), [MK14 EBR](#gun-mk14), [P320](#gun-p320), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [.357 Rhino Revolver](#gun-rhino357), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Springfield 1873 Trapdoor Rifle](#gun-springfield1873), [Taurus "Raging Hunter" Hand Cannon](#gun-taurus500), [Timeless .50 Z-Type](#gun-timeless50), [Type 81-1 Service Rifle](#gun-type_81), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-ammo_mod_slug"></a>산탄총 슬러그 탄환<br>`tacz:ammo_mod_slug` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/ammo_mod_slug.json) | 산탄을 단일 슬러그 발사체로 전환<br>무게 설정 +0.6<br>조준 탄 퍼짐 96% 감소<br>방어구 관통률이 0.5 초과이면 ×1.5, 그 외 ×1.75<br>피해량 10% 감소<br>탄속 10% 증가<br>확장 레벨 3 적용 | 6종: [AA12 Shotgun](#gun-aa12), [DB-4 Ursus](#gun-db_long), [DB-2 Durin](#gun-db_short), [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-extended_mag_1"></a>중량 탄약 확장 탄창 1단계<br>`tacz:extended_mag_1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/extended_mag_1.json) | 무게 설정 +0.4<br>조준 전환 시간 0.01초 추가<br>확장 레벨 1 적용 | 21종: [AA12 Shotgun](#gun-aa12), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81) |
| <a id="attachment-extended_mag_2"></a>중량 탄약 확장 탄창 2단계<br>`tacz:extended_mag_2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/extended_mag_2.json) | 무게 설정 +0.6<br>조준 전환 시간 0.025초 추가<br>확장 레벨 2 적용 | 21종: [AA12 Shotgun](#gun-aa12), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81) |
| <a id="attachment-extended_mag_3"></a>중량 탄약 확장 탄창 3단계<br>`tacz:extended_mag_3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/extended_mag_3.json) | 무게 설정 +0.8<br>조준 전환 시간 0.045초 추가<br>확장 레벨 3 적용 | 21종: [AA12 Shotgun](#gun-aa12), [AKM](#gun-ak47), [AUG](#gun-aug), [FN EVOLYS Machine Gun](#gun-fn_evolys), [FN FAL Battle Rifle](#gun-fn_fal), [G36K](#gun-g36k), [HK-416A5](#gun-hk416d), [HK G3 Battle rifle](#gun-hk_g3), [M16A1 Service Rifle](#gun-m16a1), [M16A4 Service Rifle](#gun-m16a4), [M249 Machine Gun](#gun-m249), [M4A1 Carbine](#gun-m4a1), [MK14 EBR](#gun-mk14), [QBZ-191 Assault Rifle](#gun-qbz_191), [QBZ-95 "Longbow"](#gun-qbz_95), [RPK](#gun-rpk), [SCAR-H Battle Rifle](#gun-scar_h), [SCAR-L Assault Rifle](#gun-scar_l), [Sks Tactical Rifle](#gun-sks_tactical), [SPR-15 HB "Sagittarius"](#gun-spr15hb), [Type 81-1 Service Rifle](#gun-type_81) |
| <a id="attachment-light_extended_mag_1"></a>경량 탄약 확장 탄창 1단계<br>`tacz:light_extended_mag_1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/light_extended_mag_1.json) | 무게 설정 +0.2<br>조준 전환 시간 0.01초 추가<br>확장 레벨 1 적용 | 14종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [M1911](#gun-m1911), [M9A4](#gun-m9a4), [P320](#gun-p320), [Timeless .50 Z-Type](#gun-timeless50), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-light_extended_mag_2"></a>경량 탄약 확장 탄창 2단계<br>`tacz:light_extended_mag_2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/light_extended_mag_2.json) | 무게 설정 +0.3<br>조준 전환 시간 0.02초 추가<br>확장 레벨 2 적용 | 14종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [M1911](#gun-m1911), [M9A4](#gun-m9a4), [P320](#gun-p320), [Timeless .50 Z-Type](#gun-timeless50), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-light_extended_mag_3"></a>경량 탄약 확장 탄창 3단계<br>`tacz:light_extended_mag_3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/light_extended_mag_3.json) | 무게 설정 +0.4<br>조준 전환 시간 0.03초 추가<br>확장 레벨 3 적용 | 14종: [B93R](#gun-b93r), [CZ 75](#gun-cz75), [Deagle 50](#gun-deagle), [Golden Deagle 357](#gun-deagle_golden), [Glock 17](#gun-glock_17), [MK23 Offensive Pistol](#gun-hk_mk23), [HK-MP5A5](#gun-hk_mp5a5), [M1911](#gun-m1911), [M9A4](#gun-m9a4), [P320](#gun-p320), [Timeless .50 Z-Type](#gun-timeless50), [UMP45 SMG](#gun-ump45), [UZI](#gun-uzi), [Vector SMG](#gun-vector45) |
| <a id="attachment-shotgun_extended_mag_1"></a>산탄총 탄약 확장 탄창 1단계<br>`tacz:shotgun_extended_mag_1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/shotgun_extended_mag_1.json) | 무게 설정 +0.4<br>조준 전환 시간 0.01초 추가<br>확장 레벨 1 적용 | 3종: [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-shotgun_extended_mag_2"></a>산탄총 탄약 확장 탄창 2단계<br>`tacz:shotgun_extended_mag_2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/shotgun_extended_mag_2.json) | 무게 설정 +0.4<br>조준 전환 시간 0.01초 추가<br>확장 레벨 2 적용 | 3종: [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-shotgun_extended_mag_3"></a>산탄총 탄약 확장 탄창 3단계<br>`tacz:shotgun_extended_mag_3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/shotgun_extended_mag_3.json) | 무게 설정 +0.4<br>조준 전환 시간 0.01초 추가<br>확장 레벨 3 적용 | 3종: [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) |
| <a id="attachment-sniper_extended_mag_1"></a>저격소총 확장 탄창 1단계<br>`tacz:sniper_extended_mag_1` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sniper_extended_mag_1.json) | 무게 설정 +0.5<br>조준 전환 시간 0.03초 추가<br>확장 레벨 1 적용 | 4종: [Accuracy International AWM](#gun-ai_awp), [M107 Sniper Rifle](#gun-m107), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95) |
| <a id="attachment-sniper_extended_mag_2"></a>저격소총 확장 탄창 2단계<br>`tacz:sniper_extended_mag_2` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sniper_extended_mag_2.json) | 무게 설정 +0.8<br>조준 전환 시간 0.05초 추가<br>확장 레벨 2 적용 | 4종: [Accuracy International AWM](#gun-ai_awp), [M107 Sniper Rifle](#gun-m107), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95) |
| <a id="attachment-sniper_extended_mag_3"></a>저격소총 확장 탄창 3단계<br>`tacz:sniper_extended_mag_3` | 일반 표시<br>[레시피 있음](src/main/resources/assets/tacz/custom/tacz_default_gun/data/tacz/recipe/attachments/sniper_extended_mag_3.json) | 무게 설정 +1.2<br>조준 전환 시간 0.08초 추가<br>확장 레벨 3 적용 | 4종: [Accuracy International AWM](#gun-ai_awp), [M107 Sniper Rifle](#gun-m107), [M700 Sniper Rifle](#gun-m700), [M95 .50 Cal Antimaterial](#gun-m95) |

<a id="special-ammo"></a>
## 7. 특수탄과 탄창 사용법

FMJ탄·고폭탄·할로우 포인트 탄·소이탄·산탄총 슬러그 탄환은 탄약 아이템이 아니라 extended_mag 슬롯에 장착하는 부착물입니다. 기본 탄약을 해당 특수탄 아이템으로 교체하여 소모하는 방식이 아닙니다. 총기에 맞는 원래 탄약을 계속 준비해야 합니다.

| 종류 | 사용 방식과 제한 |
| --- | --- |
| 일반 확장 탄창 12종 | 중량·경량·저격소총·산탄총 계열마다 1·2·3단계가 있습니다. 이름이 같아도 ID와 등급이 다르며, 호환 총기와 증가 발수는 5·6절을 확인합니다. |
| FMJ / 할로우 포인트 / 소이탄 | 호환 총기의 확장 탄창 슬롯을 사용합니다. 기본 데이터는 확장 레벨 3도 지정합니다. 실제 장탄수는 총기 설정을 따릅니다. |
| 고폭탄 | 폭발 특성을 부여하며 기본 확장 레벨은 1입니다. 모든 총기가 허용하는 부품이 아닙니다. 실제 사용 총기는 위 고폭탄 행에 전부 표시했습니다. |
| 산탄총 슬러그 탄환 | 호환 산탄총을 단일 발사체 방식으로 바꿉니다. 기본 확장 레벨 3이 지정되어 있습니다. |
| 탄창·특수탄 교체 | 같은 슬롯의 기존 부품과 교환합니다. 서버 개조 처리에서 EXTENDED_MAG 교체 시 장전된 탄약을 배출하는 처리가 있습니다. |
| 총검과 소음기 | 둘 다 muzzle 슬롯이므로 동시에 장착할 수 없습니다. 호환 총기도 서로 다릅니다. |

<a id="exceptions"></a>
## 8. 숨김·누락·설정 불일치

아래 내용은 문서 작성 중 발견한 현재 소스 상태입니다. 문서를 작성하면서 게임 코드나 데이터를 수정하지 않았습니다.

### 8.1 허용 태그에는 있지만 슬롯이 없는 조합

| 총기 | 태그에만 포함된 부품 | 판정 |
| --- | --- | --- |
| [CZ 75](#gun-cz75) | [미라지 소음기](#attachment-muzzle_silencer_mirage)<br>[PO-2 "프틸롭시스" 소음기](#attachment-muzzle_silencer_ptilopsis)<br>[Wraith 소음기](#attachment-muzzle_silencer_wraith) | 해당 슬롯 미지원으로 정상 장착·사용 호환표에서 제외 |
| [FN EVOLYS Machine Gun](#gun-fn_evolys) | [로프로 전술 레이저](#attachment-laser_lopro)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) | 해당 슬롯 미지원으로 정상 장착·사용 호환표에서 제외 |
| [.30-06 Lonetrail Hand Cannon](#gun-lonetrail) | [크툴루 K7 소염기](#attachment-muzzle_brake_cthulhu)<br>[사이클론 D2 소염기](#attachment-muzzle_brake_cyclone_d2)<br>[파이오니어 A3 소염기](#attachment-muzzle_brake_pioneer)<br>[T-Rex 헤비 소염기](#attachment-muzzle_brake_trex)<br>[템페스트 트라이던트 소염기](#attachment-muzzle_compensator_trident)<br>[나이트 QD 소음기](#attachment-muzzle_silencer_knight_qd)<br>[팬텀 S1 소음기](#attachment-muzzle_silencer_phantom_s1)<br>[Ursus 군용 표준 소음기](#attachment-muzzle_silencer_ursus) | 해당 슬롯 미지원으로 정상 장착·사용 호환표에서 제외 |
| [M249 Machine Gun](#gun-m249) | [로프로 전술 레이저](#attachment-laser_lopro)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) | 해당 슬롯 미지원으로 정상 장착·사용 호환표에서 제외 |
| [P90 PDW](#gun-p90) | [FMJ탄](#attachment-ammo_mod_fmj)<br>[할로우 포인트 탄](#attachment-ammo_mod_hp)<br>[소이탄](#attachment-ammo_mod_i) | 해당 슬롯 미지원으로 정상 장착·사용 호환표에서 제외 |
| [Sks Tactical Rifle](#gun-sks_tactical) | [로프로 전술 레이저](#attachment-laser_lopro)<br>[PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) | 해당 슬롯 미지원으로 정상 장착·사용 호환표에서 제외 |

서버의 개조 요청 처리에서는 허용 태그를 검사하지만, 부착물 읽기와 효과 계산 경로는 지원 슬롯도 확인합니다. 따라서 위 조합을 정상 사용 가능하다고 안내하지 않습니다. 단순히 허용 태그에 이름이 있다는 이유로 설치를 권장하지 않습니다.

### 8.2 정의 누락과 부착물 없는 총기

| 항목 | 관련 총기 | 설명 |
| --- | --- | --- |
| `tacz:muzzle_duckbill_sg` — 산탄총 덕빌 총구 부착물 | [AA12 Shotgun](#gun-aa12), [M1014 Battle Shotgun](#gun-m1014), [M870](#gun-m870), [SPAS-12 Multi-purpose Shotgun](#gun-spas_12) | 허용 태그에는 있으나 index/attachments에 아이템 정의가 없습니다. 등록된 99종과 장착 가능 목록에서 제외했습니다. |
| 현재 외부 부착물 없음 | [M320 Grenade Launcher](#gun-m320), [M134 Minigun](#gun-minigun), [RPG-7](#gun-rpg7), [.22 Modle 943 Revolver](#gun-taurus943) | 총기별 허용 파일이 없으므로 부착물 매칭 코드가 false를 반환합니다. |

### 8.3 숨김 부품과 미사용 탄약

| 항목 | 구분 | 설명 |
| --- | --- | --- |
| [헤라 암스 CQR 전방 손잡이 [숨김]](#attachment-grip_cqr) | 숨김 부착물 | 일반 부착물 탭에는 표시되지 않습니다. 실제 데이터 호환 총기는 6절에 표시했습니다. |
| [PEQ-15 전술 레이저 [숨김]](#attachment-laser_peq15) | 숨김 부착물 | 일반 부착물 탭에는 표시되지 않습니다. 실제 데이터 호환 총기는 6절에 표시했습니다. |
| [AUG 일체형 조준경 [숨김]](#attachment-scope_aug_default) | 숨김 부착물 | 일반 부착물 탭에는 표시되지 않습니다. 실제 데이터 호환 총기는 6절에 표시했습니다. 기본 내장 조준경입니다. |
| [P90 기본 조준경 [숨김]](#attachment-sight_p90) | 숨김 부착물 | 일반 부착물 탭에는 표시되지 않습니다. 실제 데이터 호환 총기는 6절에 표시했습니다. 기본 내장 조준경입니다. |
| [4.6mm 철갑탄](#ammo-46x30) | 기본 총기에서 미사용 | 탄약 정의는 있지만 기본 총기 54종 중 ammo 필드로 참조하는 총기가 없습니다. |
| [5.45x39mm 탄약](#ammo-545x39) | 기본 총기에서 미사용 | 탄약 정의는 있지만 기본 총기 54종 중 ammo 필드로 참조하는 총기가 없습니다. |
| [6.8x51mm 퓨리 탄약](#ammo-68x51fury) | 기본 총기에서 미사용 | 탄약 정의는 있지만 기본 총기 54종 중 ammo 필드로 참조하는 총기가 없습니다. |
| [7.62x25mm 토카레프 탄약](#ammo-762x25) | 기본 총기에서 미사용 | 탄약 정의는 있지만 기본 총기 54종 중 ammo 필드로 참조하는 총기가 없습니다. |
| [7.62x54mm 탄약](#ammo-762x54) | 기본 총기에서 미사용 | 탄약 정의는 있지만 기본 총기 54종 중 ammo 필드로 참조하는 총기가 없습니다. |

### 8.4 번역·내부 항목

| 항목 | 현재 상태 |
| --- | --- |
| AKM / Accuracy International AWM / HK-416A5 | 내부 ID는 각각 ak47 / ai_awp / hk416d입니다. 문서와 게임의 총기 이름은 영어 원문을 따르며, 한국어 설정에서도 동일하게 표시합니다. |
| 조준경 배율 이름 | TA31 이름은 2배율이지만 zoom은 2.5, 표준 5-10x는 4.5/10 등 차이가 있습니다. 6절은 실제 display 설정을 기재했습니다. |
| ACRO P-1 소형 조준경 | 한국어 원문 앞에 잘못된 § 문자가 있어 문서에서는 표시 제어 문자를 제거하고 에임포인트로 표기했습니다. |
| PEQ-15 / P90 기본 조준경 | 한국어 번역 키가 없어 설명용 한글 이름을 붙였습니다. 게임 번역 파일을 수정한 것은 아닙니다. |
| 작업대 B — tacz:workbench_b | ModItems에는 등록되지만 기본 팩의 block index에서 사용하는 작업대 정의가 없습니다. 일반 장비 9종에 포함하지 않았습니다. 한글 번역도 없습니다. |
| BloodStrike 협업 그림 — tacz:blood_strike_1 | 2×2 크기의 그림 변형 1종입니다. 독립 TACZ 아이템이 아니라 원래 minecraft:painting으로 배치되는 그림 종류입니다. 기본 그림 레시피 결과에는 해당 변형이 고정되어 있지 않습니다. |
| 두 번째 BloodStrike 그림 | 번역 문자열은 있지만 현재 painting_variant 정의를 확인하지 못했습니다. 실제 추가 그림으로 세지 않았습니다. |
| M67 수류탄 | 아이템 등록 코드가 주석 처리되어 있습니다. 사용 가능 아이템에 포함하지 않았습니다. |

<a id="sources"></a>
## 9. 확인 근거와 검증 범위

| 근거 파일 | 확인한 내용 |
| --- | --- |
| [src/main/java/com/tacz/guns/init/ModItems.java](src/main/java/com/tacz/guns/init/ModItems.java) | 기본 아이템 등록 |
| [src/main/java/com/tacz/guns/init/ModCreativeTabs.java](src/main/java/com/tacz/guns/init/ModCreativeTabs.java) | 기본 탭 구성 |
| [src/main/java/com/tacz/guns/item/AttachmentItem.java](src/main/java/com/tacz/guns/item/AttachmentItem.java) | 숨김 부착물 제외 |
| [src/main/java/com/tacz/guns/util/AllowAttachmentTagMatcher.java](src/main/java/com/tacz/guns/util/AllowAttachmentTagMatcher.java) | 허용 태그 재귀 검색 |
| [src/main/java/com/tacz/guns/api/item/gun/AbstractGunItem.java](src/main/java/com/tacz/guns/api/item/gun/AbstractGunItem.java) | 부착물 ID·슬롯 종류 판정 |
| [src/main/java/com/tacz/guns/api/item/nbt/GunItemDataAccessor.java](src/main/java/com/tacz/guns/api/item/nbt/GunItemDataAccessor.java) | 부착물 저장·읽기·내장 부품 처리 |
| [src/main/java/com/tacz/guns/util/AttachmentDataUtils.java](src/main/java/com/tacz/guns/util/AttachmentDataUtils.java) | 총기별 전용 데이터 우선 적용 |
| [src/main/java/com/tacz/guns/network/message/ClientMessageRefitGun.java](src/main/java/com/tacz/guns/network/message/ClientMessageRefitGun.java) | 개조 잠금·부품 교체·탄약 배출 |
| [src/main/java/com/tacz/guns/item/AmmoBoxItem.java](src/main/java/com/tacz/guns/item/AmmoBoxItem.java) | 탄약상자 종류·보관 방식 |
| [src/main/java/com/tacz/guns/config/sync/SyncConfig.java](src/main/java/com/tacz/guns/config/sync/SyncConfig.java) | 탄약상자 기본 배수 |
| [src/main/resources/assets/tacz/custom/tacz_default_gun/assets/tacz/lang/en_us.json](src/main/resources/assets/tacz/custom/tacz_default_gun/assets/tacz/lang/en_us.json) | 총기 영문 표시 이름의 기준 |
| [src/main/resources/assets/tacz/custom/tacz_default_gun/assets/tacz/lang/ko_kr.json](src/main/resources/assets/tacz/custom/tacz_default_gun/assets/tacz/lang/ko_kr.json) | 영문 총기 이름·한국어 설명·탄약·부품 한글 이름 |
| [src/main/resources/assets/tacz/lang/ko_kr.json](src/main/resources/assets/tacz/lang/ko_kr.json) | 기본 장비 한글 이름·조작 안내 |

총기 54종, 탄약 24종, 부착물 99종의 정의를 읽고, 허용 태그를 재귀적으로 확장하여 **1,641개의 총기–부품 조합**을 대조했습니다. 이 조합 수에는 숨김 부품도 포함되며, 슬롯 불일치 20개와 아이템 정의 누락 4개는 정상 호환표에서 제외했습니다. 총기별 목록과 부품별 역방향 목록은 같은 판정 결과를 사용했습니다.

이 작업의 검증은 문서 항목 누락·상호 참조·원본 파일 경로·호환 집계에 대한 정적 검사입니다. 빌드, 클라이언트/서버 기동, 모든 부착물 조합의 실제 개조·렌더링·발사 시험은 수행하지 않았습니다. 기본 팩 파일이나 서버 설정이 바뀌면 해당 내용도 다시 대조해야 합니다.
