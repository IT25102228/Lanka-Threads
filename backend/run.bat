@echo off
cd /d "%~dp0"
setlocal enabledelayedexpansion
title Lanka Threads - Garment ERP Launcher
color 0B

cls
echo ===============================================================================
echo                 LANKA THREADS - GARMENT ERP SYSTEM
echo ===============================================================================
echo.
echo   [1/4] Checking Java Runtime Environment...

:: Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo   [X] ERROR: Java 17 or higher was NOT found on this system!
    echo.
    echo   Please install Java 17+ using one of these options:
    echo     1. Run in PowerShell (Admin):
    echo        winget install EclipseAdoptium.Temurin.17.JDK
    echo     2. Download installer: https://adoptium.net/temurin/releases/?version=17
    echo.
    echo   Press any key to exit...
    pause >nul
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)
echo   [OK] Java detected: !JAVAVER!
echo.
echo   [2/4] Checking Database Service (MySQL / MariaDB)...

:: Test port 3306
powershell -NoProfile -Command "(New-Object System.Net.Sockets.TcpClient).Connect('127.0.0.1', 3306)" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\xampp\mysql\bin\mysqld.exe" (
        echo   [*] MySQL not active. Auto-starting XAMPP MySQL...
        start "" "C:\xampp\mysql\bin\mysqld.exe" --defaults-file="C:\xampp\mysql\bin\my.ini" --standalone
        timeout /t 3 /nobreak >nul
        echo   [OK] MySQL service started.
    ) else (
        echo   [!] Warning: MySQL port 3306 is not responding.
        echo   Please ensure XAMPP MySQL is started if using MySQL database.
    )
) else (
    echo   [OK] Database connected on port 3306.
)

echo.
echo   [3/4] Preparing Application Launcher...

:: Auto-open browser in 6 seconds in the background
start "" cmd /c "timeout /t 6 /nobreak >nul & start http://localhost:8080"

echo   [4/4] Starting Lanka Threads Server on http://localhost:8080...
echo.
echo ===============================================================================
echo                             DEFAULT LOGIN ACCOUNTS
echo ===============================================================================
echo   * Factory Manager:       manager    / pass
echo   * Production Supervisor: supervisor / pass
echo   * Inventory Officer:     inventory  / pass
echo   * Sales Manager:         sales      / pass
echo   * Supplier Coordination: supplier   / pass
echo   * Customer Portal:       customer   / pass
echo ===============================================================================
echo.
echo   [!] Note: Browser will automatically open http://localhost:8080 shortly.
echo   [!] To stop the server at any time, press CTRL + C.
echo.

if exist "target\system-0.0.1-SNAPSHOT.jar" (
    echo [*] Running pre-built package (target\system-0.0.1-SNAPSHOT.jar)...
    echo.
    java -jar target\system-0.0.1-SNAPSHOT.jar
) else (
    echo [*] Running via Maven Wrapper (mvnw.cmd spring-boot:run)...
    echo.
    call mvnw.cmd spring-boot:run
)

if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo ===============================================================================
    echo   [X] Server stopped unexpectedly or failed to start.
    echo ===============================================================================
    echo   Common fixes:
    echo     1. Is Port 8080 already in use? Close other running instances.
    echo     2. If building first time, ensure an active internet connection.
    echo.
    echo   Press any key to exit...
    pause >nul
)
