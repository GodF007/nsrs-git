@echo off
setlocal enabledelayedexpansion

REM NSRS Kubernetes 部署脚本 (Windows 版本)
REM 使用方法: deploy-k8s.bat [action]
REM action: deploy|undeploy|status|logs|restart|build|help

REM 配置变量
set NAMESPACE=nsrs
set APP_NAME=nsrs
set IMAGE_NAME=nsrs:1.0.0
set K8S_BASE_DIR=k8s\base
set K8S_DATABASE_DIR=k8s\database
set K8S_SCALING_DIR=k8s\scaling
set K8S_MONITORING_DIR=k8s\monitoring
set DOCKER_DIR=docker

REM 获取操作参数
set ACTION=%1
if "%ACTION%"=="" set ACTION=deploy

REM 日志函数
echo [INFO] NSRS Kubernetes 部署脚本启动...

REM 检查依赖
:check_dependencies
echo [INFO] 检查依赖...

kubectl version --client >nul 2>&1
if errorlevel 1 (
    echo [ERROR] kubectl 未安装或不在 PATH 中
    exit /b 1
)

docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] docker 未安装或不在 PATH 中
    exit /b 1
)

kubectl cluster-info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] 无法连接到 Kubernetes 集群
    exit /b 1
)

echo [SUCCESS] 依赖检查通过
goto :action_handler

REM 构建 Docker 镜像
:build_image
echo [INFO] 构建 Docker 镜像...

cd ..

if not exist "nsrs-deploy\%DOCKER_DIR%\Dockerfile" (
    echo [ERROR] Dockerfile 不存在: nsrs-deploy\%DOCKER_DIR%\Dockerfile
    exit /b 1
)

echo [INFO] 开始构建镜像 %IMAGE_NAME%...
docker build -f nsrs-deploy\%DOCKER_DIR%\Dockerfile -t %IMAGE_NAME% .

if errorlevel 1 (
    echo [ERROR] Docker 镜像构建失败
    exit /b 1
) else (
    echo [SUCCESS] Docker 镜像构建成功: %IMAGE_NAME%
)

cd nsrs-deploy
goto :eof

REM 部署到 Kubernetes
:deploy
echo [INFO] 开始部署 NSRS 到 Kubernetes...

if not exist "%K8S_BASE_DIR%" (
    echo [ERROR] K8s 配置目录不存在: %K8S_BASE_DIR%
    exit /b 1
)

REM 创建命名空间
echo [INFO] 创建命名空间...
kubectl apply -f %K8S_BASE_DIR%\namespace.yaml

REM 部署 MySQL
echo [INFO] 部署 MySQL 数据库...
kubectl apply -f %K8S_DATABASE_DIR%\

REM 等待 MySQL 就绪
echo [INFO] 等待 MySQL 启动...
kubectl wait --for=condition=ready pod -l app=mysql -n %NAMESPACE% --timeout=300s

if errorlevel 1 (
    echo [WARNING] MySQL 启动超时，继续部署应用...
) else (
    echo [SUCCESS] MySQL 启动成功
)

REM 部署应用配置
echo [INFO] 部署应用配置...
kubectl apply -f %K8S_BASE_DIR%\configmap.yaml
kubectl apply -f %K8S_BASE_DIR%\secret.yaml

REM 部署应用
echo [INFO] 部署 NSRS 应用...
kubectl apply -f %K8S_BASE_DIR%\deployment.yaml
kubectl apply -f %K8S_BASE_DIR%\service.yaml
kubectl apply -f %K8S_BASE_DIR%\ingress.yaml

REM 等待应用就绪
echo [INFO] 等待应用启动...
kubectl wait --for=condition=ready pod -l app=%APP_NAME% -n %NAMESPACE% --timeout=300s

if errorlevel 1 (
    echo [ERROR] 应用启动超时
    call :show_logs
    exit /b 1
) else (
    echo [SUCCESS] NSRS 应用部署成功
    call :show_status
)
goto :eof

REM 卸载部署
:undeploy
echo [INFO] 开始卸载 NSRS...

REM 删除应用资源
kubectl delete -f %K8S_BASE_DIR%\ingress.yaml --ignore-not-found=true
kubectl delete -f %K8S_BASE_DIR%\service.yaml --ignore-not-found=true
kubectl delete -f %K8S_BASE_DIR%\deployment.yaml --ignore-not-found=true
kubectl delete -f %K8S_BASE_DIR%\configmap.yaml --ignore-not-found=true
kubectl delete -f %K8S_BASE_DIR%\secret.yaml --ignore-not-found=true

REM 删除 MySQL
set /p "DELETE_MYSQL=是否删除 MySQL 数据库？这将删除所有数据 (y/N): "
if /i "%DELETE_MYSQL%"=="y" (
    kubectl delete -f %K8S_DATABASE_DIR%\ --ignore-not-found=true
    echo [WARNING] MySQL 数据库已删除
)

REM 删除命名空间
set /p "DELETE_NS=是否删除命名空间 %NAMESPACE%？(y/N): "
if /i "%DELETE_NS%"=="y" (
    kubectl delete namespace %NAMESPACE% --ignore-not-found=true
    echo [SUCCESS] 命名空间 %NAMESPACE% 已删除
)

echo [SUCCESS] NSRS 卸载完成
goto :eof

REM 显示状态
:show_status
echo [INFO] NSRS 部署状态:

echo.
echo === Pods ===
kubectl get pods -n %NAMESPACE% -o wide

echo.
echo === Services ===
kubectl get svc -n %NAMESPACE%

echo.
echo === Ingress ===
kubectl get ingress -n %NAMESPACE%

echo.
echo === PVC ===
kubectl get pvc -n %NAMESPACE%

echo.
echo === 访问地址 ===

REM 获取 NodePort
for /f "tokens=*" %%i in ('kubectl get svc nsrs-nodeport -n %NAMESPACE% -o jsonpath="{.spec.ports[0].nodePort}" 2^>nul') do set NODEPORT=%%i
if not "%NODEPORT%"=="" (
    for /f "tokens=*" %%i in ('kubectl get nodes -o jsonpath="{.items[0].status.addresses[?(@.type==\"InternalIP\")].address}"') do set NODE_IP=%%i
    echo NodePort: http://!NODE_IP!:!NODEPORT!
)

REM 获取 LoadBalancer IP
for /f "tokens=*" %%i in ('kubectl get svc nsrs-loadbalancer -n %NAMESPACE% -o jsonpath="{.status.loadBalancer.ingress[0].ip}" 2^>nul') do set LB_IP=%%i
if not "%LB_IP%"=="" (
    echo LoadBalancer: http://!LB_IP!
)

REM 获取 Ingress IP
for /f "tokens=*" %%i in ('kubectl get ingress nsrs-ingress -n %NAMESPACE% -o jsonpath="{.status.loadBalancer.ingress[0].ip}" 2^>nul') do set INGRESS_IP=%%i
if not "%INGRESS_IP%"=="" (
    echo Ingress: http://!INGRESS_IP!/nsrs
)

echo.
echo [INFO] 健康检查地址: http://节点IP:30080/actuator/health
echo [INFO] 应用访问地址: http://节点IP:30080
goto :eof

REM 显示日志
:show_logs
echo [INFO] 显示应用日志:
kubectl logs -f deployment/nsrs-app -n %NAMESPACE% --tail=100
goto :eof

REM 重启应用
:restart
echo [INFO] 重启 NSRS 应用...
kubectl rollout restart deployment/nsrs-app -n %NAMESPACE%
kubectl rollout status deployment/nsrs-app -n %NAMESPACE%
echo [SUCCESS] 应用重启完成
goto :eof

REM 显示帮助
:show_help
echo 使用方法: %0 [action]
echo Actions:
echo   build    - 仅构建 Docker 镜像
echo   deploy   - 构建镜像并部署到 K8s（默认）
echo   undeploy - 卸载部署
echo   status   - 显示部署状态
echo   logs     - 显示应用日志
echo   restart  - 重启应用
echo   help     - 显示帮助信息
echo.
echo 示例:
echo   %0 deploy   - 部署应用
echo   %0 status   - 查看状态
echo   %0 logs     - 查看日志
goto :eof

REM 操作分发
:action_handler
if "%ACTION%"=="build" goto :build_image
if "%ACTION%"=="deploy" (
    call :build_image
    call :deploy
    goto :end
)
if "%ACTION%"=="undeploy" goto :undeploy
if "%ACTION%"=="status" goto :show_status
if "%ACTION%"=="logs" goto :show_logs
if "%ACTION%"=="restart" goto :restart
if "%ACTION%"=="help" goto :show_help

echo [ERROR] 未知操作: %ACTION%
echo 使用 '%0 help' 查看帮助信息
exit /b 1

:end
echo [INFO] 操作完成
pause