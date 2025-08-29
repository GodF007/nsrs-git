@echo off
REM NSRS Docker镜像构建脚本 (Windows版本)
REM 作用：自动化构建NSRS应用的Docker镜像
REM 说明：需要先编译生成NSRS.jar文件，然后构建Docker镜像

setlocal enabledelayedexpansion

echo 开始构建NSRS Docker镜像...

REM 配置变量
set "IMAGE_NAME=nsrs"
set "IMAGE_TAG=1.0.0"
set "REGISTRY=10.21.1.210:5000"
set "FULL_IMAGE_NAME=%REGISTRY%/%IMAGE_NAME%:%IMAGE_TAG%"

REM 项目根目录（假设脚本在nsrs-deploy\simple-deploy目录下）
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%..\..\" 
set "DEPLOY_DIR=%SCRIPT_DIR%"

echo 项目根目录: %PROJECT_ROOT%
echo 部署目录: %DEPLOY_DIR%
echo 镜像名称: %FULL_IMAGE_NAME%

REM 检查Docker是否可用
docker version >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker命令未找到，请确保已安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Maven是否可用
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: Maven命令未找到，请确保已安装Maven
    pause
    exit /b 1
)

echo ✓ Docker和Maven环境检查通过

REM 1. 编译项目
echo 1. 编译NSRS项目...
cd /d "%PROJECT_ROOT%"

REM 清理并编译项目
echo 执行Maven编译...
mvn clean package -DskipTests -Dmaven.test.skip=true

if errorlevel 1 (
    echo 错误: Maven编译失败
    pause
    exit /b 1
)

echo ✓ 项目编译完成

REM 2. 查找生成的JAR文件
echo 2. 查找生成的JAR文件...
set "JAR_FILE="
for /r "%PROJECT_ROOT%" %%f in (*.jar) do (
    set "filename=%%~nf"
    echo !filename! | findstr /v "original-" | findstr /v "sources" >nul
    if not errorlevel 1 (
        set "JAR_FILE=%%f"
        goto :found_jar
    )
)

:found_jar
if "%JAR_FILE%"=="" (
    echo 错误: 未找到编译生成的JAR文件
    echo 请检查Maven编译是否成功
    pause
    exit /b 1
)

echo 找到JAR文件: %JAR_FILE%

REM 3. 复制JAR文件到构建目录
echo 3. 准备构建文件...
copy "%JAR_FILE%" "%DEPLOY_DIR%NSRS.jar" >nul

if not exist "%DEPLOY_DIR%NSRS.jar" (
    echo 错误: JAR文件复制失败
    pause
    exit /b 1
)

echo ✓ JAR文件准备完成

REM 4. 构建Docker镜像
echo 4. 构建Docker镜像...
cd /d "%DEPLOY_DIR%"

REM 检查Dockerfile是否存在
if not exist "Dockerfile" (
    echo 错误: Dockerfile不存在
    pause
    exit /b 1
)

echo 开始构建镜像: %FULL_IMAGE_NAME%
docker build -t "%FULL_IMAGE_NAME%" .

if errorlevel 1 (
    echo 错误: Docker镜像构建失败
    pause
    exit /b 1
)

echo ✓ Docker镜像构建完成

REM 5. 推送镜像到仓库（可选）
set /p "push_choice=是否推送镜像到仓库? (y/N): "
if /i "%push_choice%"=="y" (
    echo 5. 推送镜像到仓库...
    docker push "%FULL_IMAGE_NAME%"
    
    if not errorlevel 1 (
        echo ✓ 镜像推送完成
    ) else (
        echo 警告: 镜像推送失败，请检查仓库连接和权限
    )
) else (
    echo 跳过镜像推送
)

REM 6. 清理临时文件
echo 6. 清理临时文件...
if exist "%DEPLOY_DIR%NSRS.jar" del "%DEPLOY_DIR%NSRS.jar"
echo ✓ 清理完成

REM 7. 显示构建结果
echo.
echo === 构建完成 ===
echo 镜像名称: %FULL_IMAGE_NAME%
echo.
echo 可用命令:
echo 查看镜像: docker images %FULL_IMAGE_NAME%
echo 运行容器: docker run -p 8080:8080 %FULL_IMAGE_NAME%
echo 部署到K8s: 更新deployment.yml中的镜像地址后执行部署脚本

echo.
echo 🎉 NSRS Docker镜像构建完成！
echo.
echo 下一步:
echo 1. 更新 k8s\deployment.yml 中的镜像地址为: %FULL_IMAGE_NAME%
echo 2. 执行部署脚本: scripts\deploy.bat

echo.
echo 按任意键退出...
pause >nul