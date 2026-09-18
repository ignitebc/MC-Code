# Fabric 26.2 배포 의존성

`collect_fabric_jars.py`는 이 작업 공간에서 직접 빌드하는 11개 Fabric 모드 JAR만
`Build_File/build_files`에 수집합니다. 이 문서는 배포 산출물이 아니므로 JAR 폴더에
복사하지 않고 `Build_File`에 별도로 보관합니다.

## 별도로 설치할 공통 의존성

서버와 클라이언트의 `mods` 폴더에 다음 26.2 호환 파일을 설치해야 합니다.

- Minecraft 26.2
- Java 25 이상
- Fabric Loader 0.19.3 이상
- Fabric API 0.154.2+26.2 이상
- Architectury API 21.0.6 이상
- Puzzles Lib 26.2.0 이상
- Forge Config API Port의 Minecraft 26.2용 Fabric 버전

## 수집되는 모드 간 의존성

- Yaml Config는 UI Lib을 사용합니다.
- Arc Lib은 UI Lib과 Yaml Config를 사용합니다.
- Item Restrictions는 Arc Lib과 Yaml Config를 사용합니다.
- Jobs+는 UI Lib, Yaml Config, Arc Lib, Item Restrictions, Advanced Netherite를 사용합니다.
- Illager Invasion은 Puzzles Lib과 Forge Config API Port를 사용합니다.
- caramelChat은 클라이언트 전용이며 별도의 필수 모드 의존성이 없습니다.
- FallingTree는 Fabric API만 사용하며 서버와 클라이언트 모두에 설치합니다.
- TACZ Refabricated는 Fabric API와 Forge Config API Port 26.2.1 이상을 사용하며 서버와 클라이언트 모두에 설치합니다.
- Server Utilities는 Fabric API를 사용합니다. 전용 서버에 설치하며 원격 접속 클라이언트에는 필수가 아닙니다. 싱글플레이·LAN 호스트에서 같은 규칙을 사용할 때는 호스트 클라이언트에도 설치합니다.

## Server Utilities와 함께 교체할 파일

- 새 Jobs+·Advanced Netherite·Server Utilities를 같은 배포 단위로 교체합니다. 기존 두 모드에 있던 전투·사망·크리퍼·배고픔 규칙이 Server Utilities로 이동했습니다.
- 예전 Jobs+ 또는 Advanced Netherite의 규칙 클래스가 남아 있으면 Server Utilities가 기동을 중단하여 중복 적용을 막습니다. 이전 빌드의 클래스가 `build/classes`에 남아 있으면 해당 모드를 `clean` 후 다시 빌드합니다.
- 모듈 제거 전 수면 규칙 복원과 기존 JAR 복원은 [운영 안내](../ServerUtilities-26.2/README.md)를 따릅니다.
- caramelChat은 서버 설치 대상에서 제외합니다. 나머지 모드의 기존 필수 클라이언트 의존성은 유지합니다.

## 수집 실패 처리

빌드·검증·임시 복사가 모두 끝난 후 수집 폴더를 교체합니다. 빌드나 복사가 실패하면 이전 성공 산출물을 보존합니다. 교체 후 복원마저 실패하면 `.jar-collection-*/previous` 경로를 출력하고 보존하므로 해당 폴더에서 복구합니다.

Illager Invasion JAR에는 MultiLoader Data Extensions가 포함되므로 별도로 설치하지
않습니다.

## 배포 전 확인

- `Build_File/build_files`에는 수집된 `.jar` 파일만 있는지 확인합니다.
- `mods` 폴더에 1.21.9 또는 1.21.10용 JAR이 없는지 확인합니다.
- 같은 모드 ID의 JAR이 두 개 이상 들어 있지 않은지 확인합니다.
- 서버와 클라이언트에 필요한 공통 의존성을 모두 설치했는지 확인합니다.
- 전용 서버와 클라이언트를 각각 기동하여 의존성 및 Mixin 적용 오류가 없는지 확인합니다.
