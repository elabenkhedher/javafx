@echo off
setlocal enabledelayedexpansion

SET SCRIPT_DIR=%~dp0
SET SRC=%SCRIPT_DIR%src
SET LIB=%SCRIPT_DIR%lib
SET OUT=%SCRIPT_DIR%out

echo === Compilation en cours... ===

IF EXIST "%OUT%" RMDIR /S /Q "%OUT%"
MKDIR "%OUT%"

set "CP_STR="
for /R "%LIB%" %%F in (*.jar) do (
    if not defined CP_STR (
        set "CP_STR=%%F"
    ) else (
        set "CP_STR=!CP_STR!;%%F"
    )
)

if exist "%TEMP%\sources.txt" del "%TEMP%\sources.txt"
for /R "%SRC%" %%F in (*.java) do (
    set "FILE=%%F"
    set "FILE=!FILE:\=/!"
    echo "!FILE!" >> "%TEMP%\sources.txt"
)

javac --module-path "%LIB%" --add-modules javafx.controls,javafx.fxml -cp "!CP_STR!" -d "%OUT%" @"%TEMP%\sources.txt"

IF ERRORLEVEL 1 (
    echo ECHEC de la compilation.
    exit /b 1
)

XCOPY /E /Y "%SCRIPT_DIR%resources\view\*" "%OUT%\view\" 2>nul
XCOPY /E /Y "%SCRIPT_DIR%resources\css\*"  "%OUT%\css\" 2>nul
XCOPY /E /Y "%SCRIPT_DIR%src\ressources\view\*" "%OUT%\view\" 2>nul

echo === Compilation reussie ! Lancez run.bat pour demarrer. ===
