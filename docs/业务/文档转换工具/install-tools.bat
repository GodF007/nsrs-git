@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo NSRS文档转换工具 - 环境安装脚本
echo ========================================
echo.

echo [信息] 此脚本将帮助您安装文档转换所需的工具
echo.

:: 检查管理员权限
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 建议以管理员身份运行此脚本以获得最佳体验
    echo.
)

:: 检查Chocolatey
echo [步骤1] 检查包管理器...
choco --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [信息] 检测到Chocolatey包管理器
    set "PKG_MANAGER=choco"
) else (
    scoop --version >nul 2>&1
    if !errorlevel! equ 0 (
        echo [信息] 检测到Scoop包管理器
        set "PKG_MANAGER=scoop"
    ) else (
        echo [警告] 未检测到包管理器，将提供手动安装指导
        set "PKG_MANAGER=manual"
    )
)
echo.

:: 安装Pandoc
echo [步骤2] 安装Pandoc...
pandoc --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [信息] Pandoc已安装
    pandoc --version | findstr "pandoc"
) else (
    echo [信息] 正在安装Pandoc...
    if "%PKG_MANAGER%"=="choco" (
        choco install pandoc -y
    ) else if "%PKG_MANAGER%"=="scoop" (
        scoop install pandoc
    ) else (
        echo [手动安装] 请访问 https://pandoc.org/installing.html
        echo [手动安装] 下载并安装Pandoc for Windows
        pause
    )
)
echo.

:: 安装LaTeX
echo [步骤3] 安装LaTeX引擎...
xelatex --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [信息] XeLaTeX已安装
) else (
    pdflatex --version >nul 2>&1
    if !errorlevel! equ 0 (
        echo [信息] PDFLaTeX已安装
    ) else (
        echo [信息] 正在安装MiKTeX...
        if "%PKG_MANAGER%"=="choco" (
            choco install miktex -y
        ) else if "%PKG_MANAGER%"=="scoop" (
            scoop bucket add extras
            scoop install miktex
        ) else (
            echo [手动安装] 请访问 https://miktex.org/
            echo [手动安装] 下载并安装MiKTeX
            pause
        )
    )
)
echo.

:: 检查Node.js
echo [步骤4] 检查Node.js...
node --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [信息] Node.js已安装
    node --version
) else (
    echo [信息] 正在安装Node.js...
    if "%PKG_MANAGER%"=="choco" (
        choco install nodejs -y
    ) else if "%PKG_MANAGER%"=="scoop" (
        scoop install nodejs
    ) else (
        echo [手动安装] 请访问 https://nodejs.org/
        echo [手动安装] 下载并安装Node.js LTS版本
        pause
    )
)
echo.

:: 安装Node.js转换工具（可选）
echo [步骤5] 安装Node.js转换工具（可选）...
echo [询问] 是否安装额外的Node.js转换工具？(y/n)
set /p "install_node_tools=请输入选择: "
if /i "%install_node_tools%"=="y" (
    echo [信息] 安装markdown-pdf...
    npm install -g markdown-pdf
    echo [信息] 安装md-to-pdf...
    npm install -g md-to-pdf
) else (
    echo [信息] 跳过Node.js工具安装
)
echo.

:: 验证安装
echo ========================================
echo 安装验证
echo ========================================
echo.

echo [验证] Pandoc:
pandoc --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Pandoc 安装成功
    pandoc --version | findstr "pandoc"
) else (
    echo ✗ Pandoc 安装失败或未找到
)
echo.

echo [验证] LaTeX引擎:
xelatex --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ XeLaTeX 可用（推荐，支持中文）
) else (
    pdflatex --version >nul 2>&1
    if !errorlevel! equ 0 (
        echo ✓ PDFLaTeX 可用
        echo ⚠ 建议安装XeLaTeX以获得更好的中文支持
    ) else (
        echo ✗ LaTeX引擎未安装
        echo ℹ PDF转换功能将不可用
    )
)
echo.

echo [验证] Node.js:
node --version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Node.js 可用
    node --version
) else (
    echo ✗ Node.js 未安装
)
echo.

:: 完成
echo ========================================
echo 安装完成！
echo ========================================
echo.
echo [下一步] 您现在可以使用以下方式转换文档：
echo.
echo 1. 双击运行 convert-to-word.bat（转换为Word）
echo 2. 双击运行 convert-to-pdf.bat（转换为PDF）
echo 3. 运行 node convert-all.js（使用Node.js脚本）
echo 4. 查看 快速使用指南.md 了解更多用法
echo.
echo 按任意键退出...
pause >nul