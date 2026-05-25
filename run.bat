@echo off
cd /d %~dp0
echo Running Office Seat Manager with Maven...
echo Make sure MySQL is running and config\database.properties has the correct password.
where mvn >nul 2>nul
if errorlevel 1 (
    echo.
    echo Maven was not found on this computer.
    echo Please run the project from IntelliJ Maven panel, or install Maven and add it to PATH.
    echo In IntelliJ: open Maven tool window, then run Plugins - javafx - javafx:run.
    echo.
    pause
    exit /b 1
)
call mvn javafx:run
pause
