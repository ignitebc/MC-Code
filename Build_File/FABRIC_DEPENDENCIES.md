# Fabric 26.2 배포 의존성

`collect_fabric_jars.py`는 이 작업 공간에서 직접 빌드하는 7개 Fabric 모드 JAR만
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

Illager Invasion JAR에는 MultiLoader Data Extensions가 포함되므로 별도로 설치하지
않습니다.

## 배포 전 확인

- `Build_File/build_files`에는 수집된 `.jar` 파일만 있는지 확인합니다.
- `mods` 폴더에 1.21.9 또는 1.21.10용 JAR이 없는지 확인합니다.
- 같은 모드 ID의 JAR이 두 개 이상 들어 있지 않은지 확인합니다.
- 서버와 클라이언트에 필요한 공통 의존성을 모두 설치했는지 확인합니다.
- 전용 서버와 클라이언트를 각각 기동하여 의존성 및 Mixin 적용 오류가 없는지 확인합니다.
