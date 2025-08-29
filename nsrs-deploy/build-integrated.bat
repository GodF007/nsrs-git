@echo off
setlocal enabledelayedexpansion

echo ========================================
echo        NSRS Integrated Build Tool
echo ========================================
echo.

REM Get project root directory
set PROJECT_ROOT=%~dp0..
cd /d "%PROJECT_ROOT%"

REM Check if running in correct directory
if not exist "pom.xml" (
    echo Error: Project root pom.xml not found!
    exit /b 1
)

if not exist "nsrs-web\package.json" (
    echo Error: nsrs-web module not found!
    exit /b 1
)

echo [1/2] Building frontend project in nsrs-web directory...
cd nsrs-web
echo Current directory: %CD%
if exist "node_modules" (
    echo Dependencies already exist, skipping npm install...
) else (
    echo Installing dependencies...
    npm install
    REM Check if node_modules exists after installation
    if not exist "node_modules" (
        echo Frontend dependency installation failed - node_modules not found!
        cd ..
        exit /b 1
    )
    echo Dependencies installed successfully
)
echo Building frontend with npx vite build...
npx vite build
if errorlevel 1 (
    echo Frontend build failed!
    cd ..
    exit /b 1
)
cd ..
echo Frontend build completed successfully

REM Verify frontend build
if not exist "nsrs-web\dist" (
    echo Frontend build directory dist does not exist!
    exit /b 1
)

echo.
echo [2/2] Building integrated jar package with Maven...
echo Current directory: %CD%
mvn clean package -DskipTests
if errorlevel 1 (
    echo Backend build failed!
    exit /b 1
)
echo Backend build completed successfully

REM Verify jar file
set JAR_FILE=nsrs-boot\target\nsrs-boot-1.0.0-SNAPSHOT.jar
if not exist "%JAR_FILE%" (
    echo JAR file creation failed!
    exit /b 1
)

for %%A in ("%JAR_FILE%") do (
    set /a size_mb=%%~zA/1024/1024
    set jar_size=%%~zA
)

echo.
echo ========================================
echo        Build Completed Successfully!
echo ========================================
echo JAR file: %CD%\%JAR_FILE%
echo File size: !jar_size! bytes (about !size_mb! MB)
echo.
echo To run the application:
echo java -jar %JAR_FILE% --spring.profiles.active=sharding
echo.
echo Access URL: http://localhost:8088
echo ========================================

echo.
echo Build script completed successfully!