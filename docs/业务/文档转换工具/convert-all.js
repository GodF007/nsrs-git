#!/usr/bin/env node

/**
 * NSRS系统文档转换工具 - Node.js版本
 * 支持Markdown转Word、PDF等多种格式
 */

const fs = require('fs');
const path = require('path');
const { execSync, spawn } = require('child_process');

// 配置
const CONFIG = {
    inputFile: '../NSRS系统性能测试方案.md',
    outputDir: 'output',
    timestamp: new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)
};

// 颜色输出
const colors = {
    reset: '\x1b[0m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m'
};

function log(message, color = 'reset') {
    console.log(`${colors[color]}${message}${colors.reset}`);
}

function logSuccess(message) {
    log(`✓ ${message}`, 'green');
}

function logError(message) {
    log(`✗ ${message}`, 'red');
}

function logInfo(message) {
    log(`ℹ ${message}`, 'blue');
}

function logWarning(message) {
    log(`⚠ ${message}`, 'yellow');
}

// 检查命令是否存在
function commandExists(command) {
    try {
        execSync(`${command} --version`, { stdio: 'ignore' });
        return true;
    } catch {
        return false;
    }
}

// 检查文件是否存在
function fileExists(filePath) {
    return fs.existsSync(filePath);
}

// 创建输出目录
function ensureOutputDir() {
    if (!fs.existsSync(CONFIG.outputDir)) {
        fs.mkdirSync(CONFIG.outputDir, { recursive: true });
        logInfo(`创建输出目录: ${CONFIG.outputDir}`);
    }
}

// 执行命令
function executeCommand(command, description) {
    try {
        logInfo(`执行: ${description}`);
        execSync(command, { stdio: 'inherit' });
        logSuccess(`完成: ${description}`);
        return true;
    } catch (error) {
        logError(`失败: ${description}`);
        logError(`错误: ${error.message}`);
        return false;
    }
}

// Pandoc转换
function convertWithPandoc() {
    log('\n=== 使用Pandoc转换 ===', 'cyan');
    
    if (!commandExists('pandoc')) {
        logError('未检测到Pandoc，请先安装');
        logInfo('安装方法:');
        logInfo('  Windows: choco install pandoc 或 scoop install pandoc');
        logInfo('  官网: https://pandoc.org/installing.html');
        return false;
    }

    const inputFile = CONFIG.inputFile;
    const timestamp = CONFIG.timestamp;
    
    // Word转换
    const wordOutput = path.join(CONFIG.outputDir, `NSRS系统性能测试方案_${timestamp}.docx`);
    const wordCommand = `pandoc "${inputFile}" -o "${wordOutput}"`;
    executeCommand(wordCommand, 'Markdown转Word');

    // PDF转换（检查LaTeX引擎）
    let pdfEngine = 'pdflatex';
    if (commandExists('xelatex')) {
        pdfEngine = 'xelatex';
        logInfo('检测到XeLaTeX引擎，将使用中文优化');
    } else if (commandExists('pdflatex')) {
        logWarning('使用PDFLaTeX引擎，中文支持可能有限');
    } else {
        logError('未检测到LaTeX引擎，跳过PDF转换');
        logInfo('请安装MiKTeX或TeX Live');
        return true;
    }

    const pdfOutput = path.join(CONFIG.outputDir, `NSRS系统性能测试方案_${timestamp}.pdf`);
    let pdfCommand = `pandoc "${inputFile}" -o "${pdfOutput}" --pdf-engine=${pdfEngine}`;
    
    if (pdfEngine === 'xelatex') {
        pdfCommand += ' -V mainfont="SimSun" -V CJKmainfont="SimSun"';
    }
    
    executeCommand(pdfCommand, 'Markdown转PDF');

    // 高级PDF（带目录和章节编号）
    const pdfAdvancedOutput = path.join(CONFIG.outputDir, `NSRS系统性能测试方案_高级版_${timestamp}.pdf`);
    let pdfAdvancedCommand = `pandoc "${inputFile}" -o "${pdfAdvancedOutput}" --pdf-engine=${pdfEngine} --toc --toc-depth=3 --number-sections`;
    
    if (pdfEngine === 'xelatex') {
        pdfAdvancedCommand += ' -V mainfont="SimSun" -V CJKmainfont="SimSun"';
    }
    
    executeCommand(pdfAdvancedCommand, 'Markdown转PDF（高级版）');

    return true;
}

// Node.js工具转换
function convertWithNodeTools() {
    log('\n=== 使用Node.js工具转换 ===', 'cyan');
    
    // 检查并安装依赖
    const tools = [
        { name: 'markdown-pdf', package: 'markdown-pdf' },
        { name: 'md-to-pdf', package: 'md-to-pdf' }
    ];

    for (const tool of tools) {
        if (!commandExists(tool.name)) {
            logWarning(`${tool.name} 未安装，尝试安装...`);
            try {
                execSync(`npm install -g ${tool.package}`, { stdio: 'inherit' });
                logSuccess(`${tool.name} 安装成功`);
            } catch {
                logError(`${tool.name} 安装失败`);
                continue;
            }
        }

        // 使用工具转换
        const outputFile = path.join(CONFIG.outputDir, `NSRS系统性能测试方案_${tool.name}_${CONFIG.timestamp}.pdf`);
        const command = `${tool.name} "${CONFIG.inputFile}" -o "${outputFile}"`;
        executeCommand(command, `使用${tool.name}转换PDF`);
    }
}

// HTML转换
function convertToHtml() {
    log('\n=== 转换为HTML ===', 'cyan');
    
    const htmlOutput = path.join(CONFIG.outputDir, `NSRS系统性能测试方案_${CONFIG.timestamp}.html`);
    const htmlCommand = `pandoc "${CONFIG.inputFile}" -o "${htmlOutput}" --standalone --css=style.css`;
    
    executeCommand(htmlCommand, 'Markdown转HTML');
    
    // 创建简单的CSS样式文件
    const cssContent = `
body {
    font-family: 'Microsoft YaHei', 'SimSun', sans-serif;
    line-height: 1.6;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    color: #333;
}

h1, h2, h3, h4, h5, h6 {
    color: #2c3e50;
    margin-top: 2em;
    margin-bottom: 1em;
}

h1 {
    border-bottom: 3px solid #3498db;
    padding-bottom: 10px;
}

h2 {
    border-bottom: 2px solid #ecf0f1;
    padding-bottom: 5px;
}

table {
    border-collapse: collapse;
    width: 100%;
    margin: 1em 0;
}

th, td {
    border: 1px solid #ddd;
    padding: 8px 12px;
    text-align: left;
}

th {
    background-color: #f8f9fa;
    font-weight: bold;
}

code {
    background-color: #f8f9fa;
    padding: 2px 4px;
    border-radius: 3px;
    font-family: 'Consolas', 'Monaco', monospace;
}

pre {
    background-color: #f8f9fa;
    padding: 15px;
    border-radius: 5px;
    overflow-x: auto;
}

blockquote {
    border-left: 4px solid #3498db;
    margin: 1em 0;
    padding-left: 1em;
    color: #666;
}
`;
    
    const cssPath = path.join(CONFIG.outputDir, 'style.css');
    fs.writeFileSync(cssPath, cssContent);
    logInfo('创建CSS样式文件');
}

// 主函数
function main() {
    log('========================================', 'cyan');
    log('NSRS系统文档转换工具 - Node.js版本', 'cyan');
    log('========================================', 'cyan');
    
    // 检查输入文件
    if (!fileExists(CONFIG.inputFile)) {
        logError(`找不到输入文件: ${CONFIG.inputFile}`);
        process.exit(1);
    }
    
    logInfo(`输入文件: ${CONFIG.inputFile}`);
    logInfo(`输出目录: ${CONFIG.outputDir}`);
    logInfo(`时间戳: ${CONFIG.timestamp}`);
    
    // 创建输出目录
    ensureOutputDir();
    
    // 执行转换
    convertWithPandoc();
    convertToHtml();
    // convertWithNodeTools(); // 可选：启用Node.js工具转换
    
    // 显示结果
    log('\n========================================', 'cyan');
    log('转换完成！', 'green');
    log('========================================', 'cyan');
    
    if (fs.existsSync(CONFIG.outputDir)) {
        const files = fs.readdirSync(CONFIG.outputDir);
        logInfo('生成的文件:');
        files.forEach(file => {
            log(`  - ${file}`, 'green');
        });
    }
    
    logInfo(`\n输出目录: ${path.resolve(CONFIG.outputDir)}`);
}

// 运行
if (require.main === module) {
    main();
}

module.exports = {
    convertWithPandoc,
    convertWithNodeTools,
    convertToHtml
};