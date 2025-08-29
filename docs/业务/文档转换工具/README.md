# NSRS系统文档转换工具

本工具提供将Markdown文档转换为Word和PDF格式的多种方案。

## 方案一：使用Pandoc（推荐）

### 1. 安装Pandoc

#### Windows安装方式：
```bash
# 使用Chocolatey安装
choco install pandoc

# 或者使用Scoop安装
scoop install pandoc

# 或者直接下载安装包
# 访问 https://pandoc.org/installing.html
```

### 2. 转换命令

#### 转换为Word格式：
```bash
pandoc "NSRS系统性能测试方案.md" -o "NSRS系统性能测试方案.docx"
```

#### 转换为PDF格式：
```bash
# 需要先安装LaTeX引擎（如MiKTeX或TeX Live）
pandoc "NSRS系统性能测试方案.md" -o "NSRS系统性能测试方案.pdf" --pdf-engine=xelatex
```

#### 高级转换（带样式）：
```bash
# 转换为Word（带参考样式）
pandoc "NSRS系统性能测试方案.md" -o "NSRS系统性能测试方案.docx" --reference-doc=template.docx

# 转换为PDF（带中文支持）
pandoc "NSRS系统性能测试方案.md" -o "NSRS系统性能测试方案.pdf" --pdf-engine=xelatex -V mainfont="SimSun"
```

## 方案二：使用Node.js工具

### 1. 安装依赖
```bash
npm install -g markdown-pdf
npm install -g md-to-pdf
```

### 2. 转换命令

#### 转换为PDF：
```bash
# 使用markdown-pdf
markdown-pdf "NSRS系统性能测试方案.md"

# 使用md-to-pdf
md-to-pdf "NSRS系统性能测试方案.md"
```

## 方案三：在线转换工具

1. **Pandoc Try**：https://pandoc.org/try/
2. **Dillinger**：https://dillinger.io/
3. **StackEdit**：https://stackedit.io/
4. **Typora**：本地软件，支持导出多种格式

## 方案四：使用IDE插件

### VS Code插件：
1. **Markdown PDF**
2. **Markdown All in One**
3. **Pandoc Citer**

### 使用方法：
1. 在VS Code中打开Markdown文件
2. 按 `Ctrl+Shift+P` 打开命令面板
3. 输入 "Markdown PDF: Export (pdf)" 或 "Export (docx)"

## 自动化脚本

我们提供了自动化转换脚本，请查看：
- `convert-to-word.bat` - Windows批处理脚本
- `convert-to-pdf.bat` - PDF转换脚本
- `convert-all.js` - Node.js转换脚本

## 注意事项

1. **中文支持**：转换PDF时需要确保系统有中文字体
2. **图片路径**：确保Markdown中的图片路径正确
3. **表格格式**：复杂表格可能需要手动调整
4. **样式保持**：Word转换可能需要自定义模板

## 推荐工作流

1. **开发阶段**：使用Markdown编写文档
2. **评审阶段**：转换为Word格式便于批注
3. **发布阶段**：转换为PDF格式便于分发
4. **归档阶段**：保留原始Markdown文件