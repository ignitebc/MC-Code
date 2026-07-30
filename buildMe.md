# 빌드 가이드

이 워크스페이스의 모든 Fabric 모드는 `Build_File`의 수집 스크립트 하나로만 빌드합니다.
각 모듈 폴더에서 gradlew를 직접 실행하지 않습니다.

## 목차

- [빌드 환경](#빌드-환경)
- [빌드 실행](#빌드-실행)
- [빌드 대상 모듈](#빌드-대상-모듈)
- [일부 모듈만 빌드하기](#일부-모듈만-빌드하기)
- [빌드 결과](#빌드-결과)
- [빌드 오류 확인](#빌드-오류-확인)

## 빌드 환경

| 항목 | 버전 |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 |
| Fabric Loader | 0.19.3 이상 |
| Python | 3.9 이상 |

스크립트는 설치된 Java 25를 자동으로 찾습니다.
자동 탐색이 불가능한 환경에서는 `JAVA_25_HOME`에 JDK 25 설치 경로를 지정합니다.

최초 빌드 시 Fabric, Architectury, CurseMaven 의존성을 내려받으므로 인터넷 연결이 필요합니다.

## 빌드 실행

저장소 루트에서 다음 명령 하나만 실행합니다.

```bash
python Build_File/collect_fabric_jars.py
```

스크립트가 등록된 모든 모듈을 순서대로 빌드하고, 배포용 JAR을 `Build_File/build_files/`에 모읍니다.

## 빌드 대상 모듈

빌드 순서는 의존 관계를 따릅니다. 라이브러리(UI Lib, YAML Config, ArcLib)가 먼저 빌드되고,
이를 참조하는 Jobs+가 나중에 빌드되도록 스크립트의 `MODULES` 목록이 이미 정렬되어 있습니다.

| 순서 | 모듈 | 비고 |
| --- | --- | --- |
| 1 | UILib-26.2 | 라이브러리 |
| 2 | YamlConfig-26.2 | 라이브러리 |
| 3 | ArcLib-26.2 | 라이브러리 (액션, 조건, 보상 타입 제공) |
| 4 | ItemRestrictions-26.2 | |
| 5 | JobsPlusRemastered-26.2 | 위 라이브러리에 의존 |
| 6 | AdvancedNetherite-26.2 | 데이터 생성 태스크 제외 후 빌드 |
| 7 | illagerinvasion-26.2.0-mc26.2-fabric | |
| 8 | caramelChat-26.2 | |
| 9 | FallingTree-minecraft-26.2 | 루트 buildJar 병합 산출물 수집 |

> [!WARNING]
> ArcLib에 신규 조건이나 보상을 추가한 뒤 Jobs+만 빌드하면, 데이터팩이 참조하는 타입을 찾지 못해 로드에 실패합니다.
> 라이브러리를 수정했다면 전체 빌드를 실행하십시오.

## 일부 모듈만 빌드하기

`Build_File/collect_fabric_jars.py`의 `MODULES` 목록에서 필요 없는 항목을 주석 처리하면
남은 모듈만 빌드됩니다.

> [!NOTE]
> 스크립트 시작 시 `Build_File/build_files/` 폴더가 초기화되므로,
> 일부만 빌드하면 결과 폴더에는 그 모듈들의 JAR만 남습니다.

## 빌드 결과

수집된 배포용 JAR은 `Build_File/build_files/`에 저장됩니다.

수집 스크립트는 프로젝트에서 직접 빌드하는 JAR만 모읍니다. Fabric API, Architectury API,
Puzzles Lib, Forge Config API Port 같은 외부 필수 모드는 별도로 준비해야 합니다.
전체 목록은 `Build_File/FABRIC_DEPENDENCIES.md`를 확인하십시오.

## 빌드 오류 확인

### Java 버전 오류

Java 25가 설치되어 있는지 확인합니다.

```bash
java -version
```

스크립트가 Java 25를 찾지 못하면 `JAVA_25_HOME` 환경 변수에 JDK 25 경로를 지정합니다.

### 의존성 다운로드 오류

다음 저장소에 접근할 수 없는 환경에서는 최초 빌드가 실패할 수 있습니다.

- Fabric Maven
- Architectury Maven
- CurseMaven
- Gradle Plugin Portal

### Gradle 메모리 부족

빌드 환경에 최소 4GB 이상의 여유 메모리를 확보하는 것을 권장합니다.

### 데이터팩 로드 오류

게임 실행 시 조건이나 보상 타입을 찾지 못한다는 로그가 나오면 라이브러리 JAR이 최신이 아닐 때입니다.
전체 빌드를 다시 실행하십시오.
