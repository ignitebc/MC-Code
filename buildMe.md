# Jobs+ Fabric 빌드 가이드

이 문서는 Jobs+ 프로젝트의 Fabric 모듈을 빌드하는 방법을 설명합니다.

## 목차

- [빌드 환경](#빌드-환경)
- [사전 준비](#사전-준비)
- [빌드 순서](#빌드-순서)
- [Fabric 모듈 빌드](#fabric-모듈-빌드)
- [클린 빌드](#클린-빌드)
- [빌드 결과](#빌드-결과)
- [프로젝트 빌드 구조](#프로젝트-빌드-구조)
- [빌드 오류 확인](#빌드-오류-확인)

## 빌드 환경

| 항목 | 버전 |
| --- | --- |
| Minecraft | 26.2 |
| Java | 21 |
| Fabric Loader | 0.17.0 |
| Fabric API | 0.154.2+26.2 |
| Gradle 메모리 | 최대 4GB |

> [!NOTE]
> `settings.gradle`에서 `common`과 `fabric` 모듈만 활성화되어 있습니다.
> NeoForge 모듈은 프로젝트에 존재하지만 현재 빌드 대상에서는 제외되어 있습니다.

## 사전 준비

Java 21이 설치되어 있어야 합니다.

```bash
java -version
```

출력 결과에서 Java 버전이 21인지 확인합니다.

최초 빌드 시 Fabric, Architectury, CurseMaven 의존성을 내려받으므로 인터넷 연결이 필요합니다.

## 빌드 순서

라이브러리 모드를 함께 수정한 경우에는 **라이브러리를 먼저 빌드**해야 합니다.

| 순서 | 모듈 | 이유 |
| --- | --- | --- |
| 1 | ArcLib | 액션, 조건, 보상 타입을 제공 |
| 2 | UI Lib / YAML Config / Item Restrictions | Jobs+가 참조하는 라이브러리 |
| 3 | Jobs+ | 위 라이브러리에 의존 |

> [!WARNING]
> ArcLib에 신규 조건이나 보상을 추가한 뒤 Jobs+만 빌드하면, 데이터팩이 참조하는 타입을 찾지 못해 로드에 실패합니다.

Jobs+만 수정한 경우에는 이 순서를 지키지 않아도 됩니다.

## Fabric 모듈 빌드

Gradle 모듈 이름은 소문자 `fabric`입니다.

**Windows**

```bat
gradlew.bat :fabric:build
```

PowerShell에서도 동일하게 실행할 수 있습니다.

```powershell
./gradlew.bat :fabric:build
```

**Linux / macOS**

실행 권한이 없는 경우 먼저 권한을 부여합니다.

```bash
chmod +x gradlew
```

이후 Fabric 모듈을 빌드합니다.

```bash
./gradlew :fabric:build
```

## 클린 빌드

이전 빌드 결과를 제거한 후 다시 빌드하려면 다음 명령을 사용합니다.

**Windows**

```bat
gradlew.bat :fabric:clean :fabric:build
```

**Linux / macOS**

```bash
./gradlew :fabric:clean :fabric:build
```

## 빌드 결과

빌드가 정상적으로 완료되면 결과 파일은 `fabric/build/libs/`에 생성됩니다.

| 파일 | 용도 |
| --- | --- |
| `jobsplus-1.6.4-26.2-fabric.jar` | **배포용.** 서버와 클라이언트에 설치 |
| `*-fabric-sources.jar` | 소스 첨부용. 설치하지 않음 |
| `*-fabric-dev-shadow.jar` | 개발 환경 전용. 설치하지 않음 |

실제 모드 설치에는 파일명 끝이 `-fabric.jar`인 JAR 파일을 사용합니다.

여러 모드를 한 번에 빌드하고 결과 JAR을 모으려면 다음 스크립트를 사용할 수 있습니다.

```bash
python Build_File/collect_fabric_jars.py
```

수집된 JAR은 `Build_File/build_files/`에 저장됩니다.

## 프로젝트 빌드 구조

Fabric 모듈을 빌드하면 `common` 모듈도 자동으로 함께 컴파일되고 Fabric 배포 JAR에 포함됩니다.

```text
common
  └─ 공통 게임 로직, 데이터, 네트워크 및 기능

fabric
  └─ Fabric 초기화 코드 및 공통 모듈 패키징
```

따라서 일반적인 Fabric 빌드에서는 다음 명령 하나만 실행하면 됩니다.

```bash
./gradlew :fabric:build
```

## 빌드 오류 확인

### Java 버전 오류

Java 21이 아닌 환경에서는 컴파일이 실패할 수 있습니다.

```bash
java -version
```

Gradle이 사용하는 Java 경로도 함께 확인할 수 있습니다.

```bash
./gradlew --version
```

### 의존성 다운로드 오류

다음 저장소에 접근할 수 없는 환경에서는 최초 빌드가 실패할 수 있습니다.

- Fabric Maven
- Architectury Maven
- CurseMaven
- Gradle Plugin Portal

폐쇄망에서 빌드하려면 필요한 Gradle 배포 파일과 Maven 의존성을 사전에 캐시에 준비해야 합니다.

### Gradle 메모리 부족

현재 프로젝트는 다음 설정을 사용합니다.

```properties
org.gradle.jvmargs=-Xmx4096M
```

빌드 환경에 최소 4GB 이상의 여유 메모리를 확보하는 것을 권장합니다.

### 데이터팩 로드 오류

게임 실행 시 조건이나 보상 타입을 찾지 못한다는 로그가 나오면 [빌드 순서](#빌드-순서)를 확인하십시오. 라이브러리 JAR이 최신이 아닐 때 발생합니다.
