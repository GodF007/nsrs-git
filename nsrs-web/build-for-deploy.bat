@echo off
setlocal enabledelayedexpansion

REM NSRS-Web Local Build Script
REM Generate WAR file for manual deployment

echo ========================================
echo        NSRS-Web Local Build Tool
echo ========================================
echo.

REM Check if running in correct directory
if not exist "package.json" (
    echo Error: Please run this script in the project root directory!
    echo Current directory: %CD%
    pause
    exit /b 1
)

set APP_NAME=nsrs-web

REM Check required tools
npm --version >nul 2>&1
if errorlevel 1 (
    echo Error: npm command not found, please install Node.js first!
    pause
    exit /b 1
)

jar >nul 2>&1
if errorlevel 1 (
    echo Error: jar command not found, please install JDK first!
    pause
    exit /b 1
)

echo [1/3] Cleaning old files...
if exist "dist" (
    echo Removing dist directory...
    rmdir /s /q "dist" 2>nul
)
if exist "%APP_NAME%.war" (
    echo Removing old WAR file...
    del "%APP_NAME%.war" 2>nul
)
if exist "war-temp" (
    echo Removing temp directory...
    rmdir /s /q "war-temp" 2>nul
)
echo Cleanup completed

echo.
echo [2/3] Building project...
echo Building with Vite...
npx vite build
if errorlevel 1 (
    echo.
    echo X Build failed!
    echo Please check code for errors
    pause
    exit /b 1
)
echo Build completed successfully

echo.
echo [3/3] Creating WAR package...

REM Check build results
if not exist "dist" (
    echo X Build directory dist does not exist!
    pause
    exit /b 1
)

if not exist "dist\index.html" (
    echo X Build result incomplete, missing index.html!
    pause
    exit /b 1
)

REM Create WAR directory structure
echo Creating temp directory...
mkdir war-temp 2>nul
echo Copying build files...
xcopy "dist\*" "war-temp\" /E /I /Q /Y
if errorlevel 1 (
    echo X File copy failed!
    pause
    exit /b 1
)

REM Create WEB-INF directory and web.xml
echo Creating WEB-INF structure...
mkdir "war-temp\WEB-INF" 2>nul

echo Creating web.xml...
echo ^<?xml version="1.0" encoding="UTF-8"?^> > "war-temp\WEB-INF\web.xml"
echo ^<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" >> "war-temp\WEB-INF\web.xml"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" >> "war-temp\WEB-INF\web.xml"
echo          xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee" >> "war-temp\WEB-INF\web.xml"
echo          http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd" >> "war-temp\WEB-INF\web.xml"
echo          version="4.0"^> >> "war-temp\WEB-INF\web.xml"
echo. >> "war-temp\WEB-INF\web.xml"
echo     ^<display-name^>NSRS-Web^</display-name^> >> "war-temp\WEB-INF\web.xml"
echo. >> "war-temp\WEB-INF\web.xml"
echo     ^<error-page^> >> "war-temp\WEB-INF\web.xml"
echo         ^<error-code^>404^</error-code^> >> "war-temp\WEB-INF\web.xml"
echo         ^<location^>/index.html^</location^> >> "war-temp\WEB-INF\web.xml"
echo     ^</error-page^> >> "war-temp\WEB-INF\web.xml"
echo. >> "war-temp\WEB-INF\web.xml"
echo ^</web-app^> >> "war-temp\WEB-INF\web.xml"

REM Package as WAR file
echo Creating WAR file...
cd war-temp
jar -cf "..\%APP_NAME%.war" *
if errorlevel 1 (
    cd ..
    echo X WAR file creation failed!
    echo Please check if jar command is properly installed
    pause
    exit /b 1
)
cd ..
rmdir /s /q war-temp 2>nul

REM Verify WAR file
if not exist "%APP_NAME%.war" (
    echo X WAR file creation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo           Package completed!
echo ========================================
echo WAR file: %CD%\%APP_NAME%.war
for %%A in ("%APP_NAME%.war") do (
    set /a size_mb=%%~zA/1024/1024
    echo File size: %%~zA bytes (about !size_mb! MB)
)
echo.
echo Manual deployment steps:
echo 1. Copy %APP_NAME%.war to your Tomcat server
echo 2. Stop Tomcat service
echo 3. Delete webapps/%APP_NAME% directory (if exists)
echo 4. Put %APP_NAME%.war into webapps directory
echo 5. Start Tomcat service
echo 6. Access: http://server-ip:8080/%APP_NAME%/
echo ========================================

echo.
echo Press any key to exit...
pause >nul