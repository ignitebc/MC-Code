# 상점·직업·스킬·총기도감 변경

## 적용 내용

| 요청 | 변경 후 동작 | 주요 소스 |
|---|---|---|
| 1 | 겉날개 1개 가격 500 BTC. 서버 거래 검증과 상점 화면이 같은 가격을 사용 | `shop/ShopOffers.java` |
| 2 | 모험가·채굴가·굴착가·농부·사냥꾼의 네더라이트 아이콘을 대응하는 금 아이템으로 변경 | `data/jobsplus/jobsplus/jobs/*.json` 5개 |
| 3 | 화면·안내·보상 번역·설정 설명을 `직업코인`으로 통일 | 코인·직업·스킬 컴포넌트, `JobsPlusConfig.java`, `en_us.json` |
| 5 | 같은 직업의 스킬 응답은 열린 화면에서 습득 상태·해금 조건·코인만 갱신. 확인창 복귀 시 스크롤·선택·포커스 유지 | `PowerupsScreen`, `PowerupsScreenState`, `PowerupsComponent`, `ClientboundOpenPowerupsScreenPacketHandler` |
| 6 | 일반 도감 카드 선택은 목록 순서와 스크롤을 변경하지 않음. 탭마다 스크롤을 기억 | `GunGuideComponent.java` |
| 8 | 종류별 제목 아래에 해당 총기·파츠 카드를 배치. 검색·호환 링크 이동은 대상이 화면 밖일 때만 필요한 만큼 스크롤 | `TaczCatalog.java`, `GunGuideComponent.java` |

총기 종류는 권총·기관단총·돌격소총·산탄총·저격소총·기관총·발사기·기타 총기로 구분합니다.
파츠는 조준경·소음기·총구·손잡이·개머리판·레이저·탄창/특수탄·기타 파츠로 구분합니다.
소음기는 이름 대신 TACZ 팩의 `silence` 효과 중 `use_silence_sound` 값을 사용합니다.
같은 이름의 항목은 고유 ID로 정렬을 고정합니다.

## 영향 범위와 빌드

Jobs+의 서버와 클라이언트 JAR을 함께 교체합니다. 직업 아이콘은 데이터팩의 직업 정의를 사용하므로,
외부 데이터팩이 해당 직업을 덮어쓰고 있다면 그 정의도 확인합니다.

Minecraft 26.2 / Java 25 기준입니다. 의존 모듈의 JAR이 준비되어 있다면 이 모듈 폴더에서 실행합니다.

```powershell
.\gradlew.bat :fabric:build
```

의존 모듈까지 순서대로 빌드·수집하려면 저장소 루트에서 기존 수집기를 사용합니다.

```powershell
python Build_File-26.2/collect_fabric_jars.py
```
