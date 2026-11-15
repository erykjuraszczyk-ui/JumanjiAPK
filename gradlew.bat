@echo off
setlocal
set GRADLE_VERSION=9.6.0
set GRADLE_HOME=%USERPROFILE%\.gradle\jumanji-gradle\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%GRADLE_HOME%\bin\gradle.bat

if not exist "%GRADLE_BIN%" (
  set DIST=%USERPROFILE%\.gradle\jumanji-gradle
  if not exist "%DIST%" mkdir "%DIST%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST%\gradle.zip'; Expand-Archive -Path '%DIST%\gradle.zip' -DestinationPath '%DIST%' -Force; Remove-Item '%DIST%\gradle.zip'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_BIN%" %*
