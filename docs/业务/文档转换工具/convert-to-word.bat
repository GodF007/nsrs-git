@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ========================================
echo NSRS Document Converter - Word Export
echo ========================================
echo.

REM Check if pandoc is installed
pandoc --version > nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Pandoc not found, please install Pandoc first
    echo.
    echo Installation methods:
    echo 1. Using Chocolatey: choco install pandoc
    echo 2. Using Scoop: scoop install pandoc
    echo 3. Official download: https://pandoc.org/installing.html
    echo.
    pause
    exit /b 1
)

echo [INFO] Pandoc installed, version info:
pandoc --version | findstr "pandoc"
echo.

REM Set input file
set "INPUT_FILE=..\NSRS系统性能测试方案.md"
set "OUTPUT_DIR=output"
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "TIMESTAMP=%dt:~0,4%-%dt:~4,2%-%dt:~6,2%T%dt:~8,2%-%dt:~10,2%-%dt:~12,2%"

REM Create output directory
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Check if input file exists
if not exist "%INPUT_FILE%" (
    echo [ERROR] Input file not found: %INPUT_FILE%
    echo Please ensure the file path is correct
    pause
    exit /b 1
)

echo [INFO] Input file: %INPUT_FILE%
echo [INFO] Output directory: %OUTPUT_DIR%
echo.

REM Basic conversion
echo [STEP 1] Executing basic Word conversion...
set "OUTPUT_BASIC=%OUTPUT_DIR%\NSRS_Performance_Test_Plan_%TIMESTAMP%.docx"
pandoc "%INPUT_FILE%" -o "%OUTPUT_BASIC%" --from markdown --to docx
if %errorlevel% equ 0 (
    echo [SUCCESS] Basic version generated: %OUTPUT_BASIC%
) else (
    echo [ERROR] Basic conversion failed
)
echo.

REM Check if template file exists
set "TEMPLATE_FILE=template.docx"
if exist "%TEMPLATE_FILE%" (
    echo [STEP 2] Executing advanced conversion with template...
    set "OUTPUT_STYLED=%OUTPUT_DIR%\NSRS_Performance_Test_Plan_Styled_%TIMESTAMP%.docx"
    pandoc "%INPUT_FILE%" -o "!OUTPUT_STYLED!" --from markdown --to docx --reference-doc="%TEMPLATE_FILE%"
    if !errorlevel! equ 0 (
        echo [SUCCESS] Styled version generated: !OUTPUT_STYLED!
    ) else (
        echo [ERROR] Styled conversion failed
    )
) else (
    echo [INFO] Template file %TEMPLATE_FILE% not found, skipping styled conversion
    echo [TIP] You can create a Word template file to customize styles
)
echo.

REM Conversion complete
echo ========================================
echo Conversion Complete!
echo ========================================
echo.
echo Output file location: %OUTPUT_DIR%
dir "%OUTPUT_DIR%\*.docx" /b
echo.
echo Press any key to open output directory...
pause >nul
start "" "%OUTPUT_DIR%"