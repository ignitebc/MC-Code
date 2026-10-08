아이템 아이콘 재제작 — 2026-10-08

범위
- 비트코인 획득 확률 쿠폰 2종
- 랜덤 상자 I~IV 및 보상 열쇠 I~IV, 총 8종
- 강화 조각·강화 원석 2종
- 직업 선택권·사망 시 아이템 보존권·땅 구입 문서·강화 파괴 방지권 4종
- 견습공·숙련공·장인·명장의 축복 주문서 4종
- 일반·희귀·전설 펫 상자 3종
총 23개 디자인. 쿠폰 2개는 JobsPlus 상태 효과 PNG에도 동일하게 적용한다.

제작 기준
최종 아이템 리소스 23종은 64×64px RGBA PNG다. 앞으로 아이템 텍스처의 기본값도 64×64px다.
직업·일반스킬·하이퍼스킬·상태 효과 등 UI 전용 아이콘은 256×256px를 기본값으로 유지한다.
같은 쿠폰 그림을 사용하는 JobsPlus 상태 효과 PNG 2종은 UI 전용이므로 256×256px를 유지한다.
image_gen에서 투명 배경으로 각각 제작한 뒤 비율을 유지해 고품질 Bicubic 보간으로 축소했다.
배경의 투명도와 본체의 불투명도를 유지하며 알파를 이진화하지 않는다.
고해상도 생성 원본은 Codex generated_images에 보존하고 게임 리소스에는 용도별 최종 해상도로 넣는다.

디자인 구분
상자·열쇠: I 구리/갈색, II 청록/은색, III 보라/은색, IV 진홍/금색.
강화 재료: 깨진 조각 묶음과 온전한 육각 원석.
보호권: 사망 보존권은 초록 방패와 배낭, 강화 파괴 방지권은 파랑 방패와 검.
축복 주문서: +3% 초록/구리, +5% 파랑/은색, +7% 보라/금색, +10% 진홍/금색.
펫 상자: 일반 흰색/목재, 희귀 파랑/은색, 전설 진홍/금색. 공통 발바닥 문양.
기존 아이템 ID nomal_petbox의 철자는 리소스 연결을 유지하기 위해 그대로 사용한다.

파일
gallery.html: 최종 64px 아이템의 확대 보기와 16/32/64px, 밝고 어두운 배경 미리보기.
preview.png: 전체 디자인 한 장 미리보기.
prompts.json: 각 이미지 제작 프롬프트 및 후속 편집 프롬프트. 프롬프트는 당시 생성 기록이며 최종 해상도는 finalSize와 statusEffectSize를 따른다.
assets.json: 적용 경로, 생성 원본 파일명, 해상도, 파일 크기, SHA-256. additionalAssets에는 상태 효과 파일의 별도 규격을 기록한다.
실제 아이템 PNG: AdvancedNetherite-26.2/Common/src/main/resources/assets/advancednetherite/textures/item/
상태 효과 PNG: JobsPlusRemastered-26.2/common/src/main/resources/assets/jobsplus/textures/mob_effect/

확인 범위
PNG 해상도·알파 채널, 기존 모델의 텍스처 참조와 용도별 해시를 정적으로 확인한다.
Java, 모델, 아이템 ID, 효과 수치는 변경하지 않는다.
소스 커밋 시점에는 빌드·배포 JAR 교체 전이며 게임 실행은 미실행이다.

사용자 확인
Java 25 환경에서 AdvancedNetherite-26.2는 .\gradlew.bat :Fabric:build,
JobsPlusRemastered-26.2는 .\gradlew.bat :fabric:build 로 각각 빌드한다.
게임에서 /give @s advancednetherite:<assets.json의 name> 로 지급해 외형을 확인한다.
상자와 열쇠의 단계별 색상, 주문서 구분, 손에 들었을 때 외형, 쿠폰 상태 효과 표시를 확인한다.
