@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo NSRS系统文档转换工具 - PDF格式转换
echo ========================================
echo.

:: 检查pandoc是否安装
pandoc --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Pandoc，请先安装Pandoc
    echo.
    echo 安装方法：
    echo 1. 使用Chocolatey: choco install pandoc
    echo 2. 使用Scoop: scoop install pandoc
    echo 3. 官网下载: https://pandoc.org/installing.html
    echo.
    pause
    exit /b 1
)

echo [信息] Pandoc已安装，版本信息：
pandoc --version | findstr "pandoc"
echo.

:: 检查LaTeX引擎
echo [检查] 检测LaTeX引擎...
xelatex --version >nul 2>&1
if %errorlevel% equ 0 (
    set "PDF_ENGINE=xelatex"
    echo [信息] 检测到XeLaTeX引擎（推荐，支持中文）
) else (
    pdflatex --version >nul 2>&1
    if !errorlevel! equ 0 (
        set "PDF_ENGINE=pdflatex"
        echo [信息] 检测到PDFLaTeX引擎
        echo [警告] 建议安装XeLaTeX以获得更好的中文支持
    ) else (
        echo [错误] 未检测到LaTeX引擎，无法生成PDF
        echo.
        echo 请安装LaTeX发行版：
        echo 1. MiKTeX: https://miktex.org/
        echo 2. TeX Live: https://www.tug.org/texlive/
        echo.
        pause
        exit /b 1
    )
)
echo.

:: 设置输入文件
set "INPUT_FILE=..\NSRS系统性能测试方案.md"
set "OUTPUT_DIR=output"
set "TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"

:: 创建输出目录
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

:: 检查输入文件是否存在
if not exist "%INPUT_FILE%" (
    echo [错误] 找不到输入文件: %INPUT_FILE%
    echo 请确保文件路径正确
    pause
    exit /b 1
)

echo [信息] 输入文件: %INPUT_FILE%
echo [信息] 输出目录: %OUTPUT_DIR%
echo [信息] PDF引擎: %PDF_ENGINE%
echo.

:: 基础PDF转换
echo [步骤1] 执行基础PDF转换...
set "OUTPUT_BASIC=%OUTPUT_DIR%\NSRS系统性能测试方案_基础版_%TIMESTAMP%.pdf"
pandoc "%INPUT_FILE%" -o "%OUTPUT_BASIC%" --pdf-engine=%PDF_ENGINE%
if %errorlevel% equ 0 (
    echo [成功] 基础版本已生成: %OUTPUT_BASIC%
) else (
    echo [错误] 基础转换失败
    echo [提示] 可能是LaTeX包缺失，请检查错误信息
)
echo.

:: 中文优化PDF转换（仅在XeLaTeX可用时）
if "%PDF_ENGINE%"=="xelatex" (
    echo [步骤2] 执行中文优化PDF转换...
    set "OUTPUT_CHINESE=%OUTPUT_DIR%\NSRS系统性能测试方案_中文优化_%TIMESTAMP%.pdf"
    pandoc "%INPUT_FILE%" -o "!OUTPUT_CHINESE!" --pdf-engine=xelatex -V mainfont="SimSun" -V CJKmainfont="SimSun"
    if !errorlevel! equ 0 (
        echo [成功] 中文优化版本已生成: !OUTPUT_CHINESE!
    ) else (
        echo [错误] 中文优化转换失败
        echo [提示] 可能是字体问题，尝试使用其他中文字体
    )
) else (
    echo [跳过] 中文优化转换（需要XeLaTeX引擎）
)
echo.

:: 高级PDF转换（带更多选项）
echo [步骤3] 执行高级PDF转换...
set "OUTPUT_ADVANCED=%OUTPUT_DIR%\NSRS系统性能测试方案_高级版_%TIMESTAMP%.pdf"
set "PANDOC_ARGS=--pdf-engine=%PDF_ENGINE% --toc --toc-depth=3 --number-sections"
if "%PDF_ENGINE%"=="xelatex" (
    set "PANDOC_ARGS=!PANDOC_ARGS! -V mainfont=SimSun -V CJKmainfont=SimSun"
)
pandoc "%INPUT_FILE%" -o "%OUTPUT_ADVANCED%" %PANDOC_ARGS%
if %errorlevel% equ 0 (
    echo [成功] 高级版本已生成: %OUTPUT_ADVANCED%
    echo [特性] 包含目录、章节编号、页码等
) else (
    echo [错误] 高级转换失败
)
echo.

:: 转换完成
echo ========================================
echo 转换完成！
echo ========================================
echo.
echo 输出文件位置: %OUTPUT_DIR%
dir "%OUTPUT_DIR%\*.pdf" /b
echo.
echo 按任意键打开输出目录...
pause >nul
start "" "%OUTPUT_DIR%"