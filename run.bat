@echo off
setlocal enabledelayedexpansion

SET SCRIPT_DIR=%~dp0
SET LIB=%SCRIPT_DIR%lib
SET OUT=%SCRIPT_DIR%out

if not exist "%OUT%" (
    echo Dossier 'out' manquant. Veuillez d'abord executer compile.bat
    pause
    exit /b 1
)

set "CP_STR=%OUT%"
for /R "%LIB%" %%F in (*.jar) do (
    set "CP_STR=!CP_STR!;%%F"
)

echo === Lancement de l'application... ===

java --module-path "%LIB%" --add-modules javafx.controls,javafx.fxml -cp "!CP_STR!" main.MainApp

if ERRORLEVEL 1 (
    echo.
    echo L'application s'est arretee avec une erreur.
    echo Verifiez que MySQL (XAMPP) est demarre et que les JARs sont corrects.
    pause
)
