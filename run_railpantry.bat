@echo off
set "MAVEN_PATH=%~dp0maven-local\apache-maven-3.9.6\bin\mvn.cmd"
echo ==========================================
echo    RAILPANTRY DEVELOPMENT RUNNER
echo ==========================================
echo.
echo [1/2] Compiling project...
call "%MAVEN_PATH%" compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Please check the logs above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] Launching RailPantry App...
call "%MAVEN_PATH%" javafx:run

echo.
echo Application closed.
pause
