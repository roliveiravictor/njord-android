@echo off
setlocal
set APP_HOME=%~dp0
set GRADLE_VERSION=8.10.2
set GRADLE_BIN=%APP_HOME%\.gradle\gradle-%GRADLE_VERSION%\bin\gradle.bat
if not exist "%GRADLE_BIN%" (
  echo Please run gradlew from macOS/Linux first or install Gradle %GRADLE_VERSION%.
  exit /b 1
)
"%GRADLE_BIN%" %*
