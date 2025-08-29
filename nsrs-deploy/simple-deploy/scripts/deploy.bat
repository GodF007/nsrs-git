@echo off
REM NSRS系统Kubernetes部署脚本 (Windows版本)
REM 作用：自动化部署NSRS应用到Kubernetes集群
REM 说明：此脚本会按顺序创建命名空间、ConfigMap、Deployment和Service等资源

setlocal enabledelayedexpansion

echo 开始部署NSRS系统到Kubernetes集群...

REM 设置部署目录
set "SCRIPT_DIR=%~dp0"
set "DEPLOY_DIR=%SCRIPT_DIR%.."
set "K8S_DIR=%DEPLOY_DIR%\k8s"

echo 部署目录: %DEPLOY_DIR%
echo Kubernetes配置目录: %K8S_DIR%

REM 检查kubectl是否可用
kubectl version --client >nul 2>&1
if errorlevel 1 (
    echo 错误: kubectl命令未找到，请确保已安装并配置kubectl
    pause
    exit /b 1
)

REM 检查集群连接
echo 检查Kubernetes集群连接...
kubectl cluster-info >nul 2>&1
if errorlevel 1 (
    echo 错误: 无法连接到Kubernetes集群，请检查kubeconfig配置
    pause
    exit /b 1
)

echo ✓ Kubernetes集群连接正常

REM 1. 创建命名空间
echo 1. 创建命名空间 'nsrs'...
kubectl apply -f "%K8S_DIR%\namespace.yml"
if errorlevel 1 (
    echo 错误: 创建命名空间失败
    pause
    exit /b 1
)
echo ✓ 命名空间创建完成

REM 2. 创建ConfigMap配置
echo 2. 创建应用配置 (ConfigMap)...
kubectl apply -f "%K8S_DIR%\configmap.yml"
if errorlevel 1 (
    echo 错误: 创建ConfigMap失败
    pause
    exit /b 1
)
echo ✓ 应用配置创建完成

REM 3. 部署应用
echo 3. 部署NSRS应用...
kubectl apply -f "%K8S_DIR%\deployment.yml"
if errorlevel 1 (
    echo 错误: 部署应用失败
    pause
    exit /b 1
)
echo ✓ 应用部署完成

REM 4. 等待部署完成
echo 4. 等待应用启动...
kubectl wait --for=condition=available --timeout=300s deployment/nsrs -n nsrs
if errorlevel 1 (
    echo 警告: 应用启动超时，请检查Pod状态
) else (
    echo ✓ 应用启动完成
)

REM 5. 显示部署状态
echo.
echo === 部署状态 ===
echo 命名空间:
kubectl get namespace nsrs

echo.
echo Pods状态:
kubectl get pods -n nsrs

echo.
echo Services状态:
kubectl get services -n nsrs

echo.
echo Deployment状态:
kubectl get deployment -n nsrs

REM 6. 显示访问信息
echo.
echo === 访问信息 ===
echo 外部访问方式 (NodePort):
echo 获取节点IP: kubectl get nodes -o wide
echo 访问地址: http://^<节点IP^>:30088/nsrs
echo API文档地址: http://^<节点IP^>:30088/nsrs/doc.html
echo.
echo 本地端口转发命令 (可选):
echo kubectl port-forward -n nsrs service/nsrs-service 8088:8088
echo 本地访问地址: http://localhost:8088/nsrs

echo.
echo 🎉 NSRS系统部署完成！
echo.
echo 注意事项:
echo 1. 外部访问已启用，通过NodePort 30088端口访问
echo 2. 生产环境建议配置Ingress或LoadBalancer服务
echo 3. 监控应用日志: kubectl logs -f deployment/nsrs -n nsrs
echo 4. 健康检查端点: http://^<节点IP^>:30088/nsrs/actuator/health

echo.
echo 按任意键退出...
pause >nul