@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Windows용 Gradle 시작 스크립트
@rem
@rem ##########################################################################

@rem Windows NT 셸에서 변수 범위를 지역으로 설정
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem 보통은 쓰지 않는다
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem APP_HOME의 "."과 ".."을 풀어 경로를 짧게 만든다.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem 기본 JVM 옵션은 여기에 추가한다. JAVA_OPTS와 GRADLE_OPTS로도 이 스크립트에 JVM 옵션을 넘길 수 있다.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem java.exe 찾기
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem 명령줄 구성

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Gradle 실행
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem Windows NT 셸의 지역 변수 범위 종료
if "%OS%"=="Windows_NT" endlocal

:omega
@exit /b %ERRORLEVEL%

:fail
rem _cmd.exe /c_ 반환 코드 대신 스크립트 자체의 반환 코드가 필요하면
rem GRADLE_EXIT_CONSOLE 변수를 설정한다!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%
