@echo off
set "MAVEN_PATH=%~dp0maven-local\apache-maven-3.9.6\bin\mvn.cmd"
cls
echo ==========================================
echo    RAILPANTRY DEVELOPMENT RUNNER
echo ==========================================
echo.
echo [1/2] Cleaning and Rebuilding Project...
echo       (Applying latest updates and features)
echo.
call "%MAVEN_PATH%" clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed. Please check the logs above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] Launching RailPantry App...
echo       (Loading the latest version)
echo.
call "%MAVEN_PATH%" javafx:run

echo.
echo Application closed.
pause
