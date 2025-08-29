# NSRS文档转换工具 - Word转换脚本
# PowerShell版本

Write-Host "========================================" -ForegroundColor Green
Write-Host "NSRS文档转换工具 - Word转换" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 设置变量
$InputFile = "..\NSRS系统性能测试方案.md"
$OutputDir = "output"
$Timestamp = Get-Date -Format "yyyy-MM-ddTHH-mm-ss"

# 创建输出目录
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# 检查Pandoc是否安装
Write-Host "[信息] 检查Pandoc安装状态..." -ForegroundColor Yellow
try {
    $pandocVersion = pandoc --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[成功] Pandoc已安装" -ForegroundColor Green
    } else {
        throw "Pandoc not found"
    }
} catch {
    Write-Host "[错误] 未找到Pandoc！" -ForegroundColor Red
    Write-Host "[提示] 请安装Pandoc：" -ForegroundColor Yellow
    Write-Host "  方法1: winget install --id JohnMacFarlane.Pandoc" -ForegroundColor Cyan
    Write-Host "  方法2: 访问 https://pandoc.org/installing.html" -ForegroundColor Cyan
    Write-Host "  方法3: choco install pandoc" -ForegroundColor Cyan
    Read-Host "按回车键退出"
    exit 1
}

# 检查输入文件
if (!(Test-Path $InputFile)) {
    Write-Host "[错误] 找不到输入文件: $InputFile" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "[信息] 输入文件: $InputFile" -ForegroundColor Cyan
Write-Host "[信息] 输出目录: $OutputDir" -ForegroundColor Cyan
Write-Host ""

# 基础转换
$OutputBasic = "$OutputDir\NSRS系统性能测试方案_$Timestamp.docx"
Write-Host "[执行] 基础Word转换..." -ForegroundColor Yellow
try {
    pandoc $InputFile -o $OutputBasic --from markdown --to docx
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[成功] 基础版本已生成: $OutputBasic" -ForegroundColor Green
    } else {
        throw "Conversion failed"
    }
} catch {
    Write-Host "[错误] 基础转换失败！" -ForegroundColor Red
    Read-Host "按回车键退出"
    exit 1
}

# 样式转换（如果有模板）
$TemplateFile = "template.docx"
if (Test-Path $TemplateFile) {
    $OutputStyled = "$OutputDir\NSRS系统性能测试方案_样式版_$Timestamp.docx"
    Write-Host "[执行] 样式Word转换..." -ForegroundColor Yellow
    try {
        pandoc $InputFile -o $OutputStyled --from markdown --to docx --reference-doc=$TemplateFile
        if ($LASTEXITCODE -eq 0) {
            Write-Host "[成功] 样式版本已生成: $OutputStyled" -ForegroundColor Green
        } else {
            Write-Host "[警告] 样式转换失败，但基础版本已生成" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "[警告] 样式转换失败，但基础版本已生成" -ForegroundColor Yellow
    }
} else {
    Write-Host "[信息] 未找到模板文件，跳过样式转换" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "转换完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "[位置] $(Get-Location)\$OutputDir" -ForegroundColor Cyan

# 显示生成的文件
$generatedFiles = Get-ChildItem -Path $OutputDir -Filter "*.docx" | Sort-Object LastWriteTime -Descending
if ($generatedFiles.Count -gt 0) {
    Write-Host "[生成的文件]" -ForegroundColor Yellow
    foreach ($file in $generatedFiles) {
        Write-Host "  - $($file.Name)" -ForegroundColor White
    }
}

Write-Host ""
Write-Host "[提示] 按任意键打开输出目录..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Start-Process explorer.exe -ArgumentList (Resolve-Path $OutputDir).Path