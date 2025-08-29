@echo off
setlocal enabledelayedexpansion

REM NSRS 健康检查脚本 (Windows 版本)
REM 用于检查 NSRS 应用和数据库的健康状态

REM 配置
set NAMESPACE=nsrs
set APP_NAME=nsrs-app
set DB_NAME=mysql

REM 颜色定义 (Windows 10+ 支持 ANSI 颜色)
set RED=[31m
set GREEN=[32m
set YELLOW=[33m
set BLUE=[34m
set NC=[0m

REM 日志函数
:log_info
echo %BLUE%[INFO]%NC% %~1
goto :eof

:log_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:log_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:log_error
echo %RED%[ERROR]%NC% %~1
goto :eof

REM 检查 kubectl 是否可用
:check_kubectl
call :log_info "检查 kubectl 连接..."
kubectl cluster-info >nul 2>&1
if %errorlevel% neq 0 (
    call :log_error "无法连接到 Kubernetes 集群"
    exit /b 1
)
call :log_success "kubectl 连接正常"
goto :eof

REM 检查命名空间
:check_namespace
call :log_info "检查命名空间 %NAMESPACE%..."
kubectl get namespace %NAMESPACE% >nul 2>&1
if %errorlevel% neq 0 (
    call :log_error "命名空间 %NAMESPACE% 不存在"
    exit /b 1
)
call :log_success "命名空间 %NAMESPACE% 存在"
goto :eof

REM 检查 Pod 状态
:check_pods
call :log_info "检查 Pod 状态..."

REM 检查应用 Pod
for /f "skip=1 tokens=1,2,3" %%a in ('kubectl get pods -n %NAMESPACE% -l app=%APP_NAME% --no-headers 2^>nul') do (
    set POD_NAME=%%a
    set POD_READY=%%b
    set POD_STATUS=%%c
    
    if "!POD_STATUS!"=="Running" (
        call :log_success "应用 Pod !POD_NAME! 状态正常 (!POD_STATUS!, !POD_READY!)"
    ) else (
        call :log_error "应用 Pod !POD_NAME! 状态异常 (!POD_STATUS!, !POD_READY!)"
    )
)

REM 检查数据库 Pod
for /f "skip=1 tokens=1,2,3" %%a in ('kubectl get pods -n %NAMESPACE% -l app=%DB_NAME% --no-headers 2^>nul') do (
    set POD_NAME=%%a
    set POD_READY=%%b
    set POD_STATUS=%%c
    
    if "!POD_STATUS!"=="Running" (
        call :log_success "数据库 Pod !POD_NAME! 状态正常 (!POD_STATUS!, !POD_READY!)"
    ) else (
        call :log_error "数据库 Pod !POD_NAME! 状态异常 (!POD_STATUS!, !POD_READY!)"
    )
)
goto :eof

REM 检查服务状态
:check_services
call :log_info "检查服务状态..."

for /f "skip=1 tokens=1,2,3,4,5" %%a in ('kubectl get svc -n %NAMESPACE% --no-headers 2^>nul') do (
    set SVC_NAME=%%a
    set SVC_TYPE=%%b
    set SVC_CLUSTER_IP=%%c
    set SVC_EXTERNAL_IP=%%d
    set SVC_PORTS=%%e
    
    call :log_success "服务 !SVC_NAME! 正常 (类型: !SVC_TYPE!, 集群IP: !SVC_CLUSTER_IP!, 端口: !SVC_PORTS!)"
)
goto :eof

REM 检查应用健康端点
:check_app_health
call :log_info "检查应用健康端点..."

REM 获取应用 Pod
for /f "tokens=1" %%a in ('kubectl get pods -n %NAMESPACE% -l app=%APP_NAME% --no-headers 2^>nul ^| findstr /r "^[^ ]*"') do (
    set APP_POD=%%a
    goto :found_app_pod
)
call :log_error "未找到应用 Pod"
goto :eof

:found_app_pod
call :log_info "检查 Pod %APP_POD% 的健康端点..."

REM 检查健康端点
kubectl exec -n %NAMESPACE% %APP_POD% -- curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    call :log_success "应用健康检查通过"
) else (
    call :log_error "无法访问应用健康端点"
)

REM 检查信息端点
kubectl exec -n %NAMESPACE% %APP_POD% -- curl -s http://localhost:8080/actuator/info >nul 2>&1
if %errorlevel% equ 0 (
    call :log_success "应用信息端点可访问"
) else (
    call :log_warning "应用信息端点不可访问"
)
goto :eof

REM 检查数据库连接
:check_database
call :log_info "检查数据库连接..."

REM 获取数据库 Pod
for /f "tokens=1" %%a in ('kubectl get pods -n %NAMESPACE% -l app=%DB_NAME% --no-headers 2^>nul ^| findstr /r "^[^ ]*"') do (
    set DB_POD=%%a
    goto :found_db_pod
)
call :log_error "未找到数据库 Pod"
goto :eof

:found_db_pod
call :log_info "检查 Pod %DB_POD% 的数据库连接..."

REM 检查 MySQL 连接
kubectl exec -n %NAMESPACE% %DB_POD% -- mysql -u root -proot123 -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    call :log_success "数据库连接正常"
) else (
    call :log_error "数据库连接失败"
)
goto :eof

REM 检查资源使用情况
:check_resources
call :log_info "检查资源使用情况..."

REM 检查节点资源
kubectl top nodes >nul 2>&1
if %errorlevel% equ 0 (
    call :log_info "节点资源使用情况:"
    kubectl top nodes
    echo.
) else (
    call :log_warning "无法获取节点资源使用情况 (可能需要安装 metrics-server)"
)

REM 检查 Pod 资源
kubectl top pods -n %NAMESPACE% >nul 2>&1
if %errorlevel% equ 0 (
    call :log_info "Pod 资源使用情况:"
    kubectl top pods -n %NAMESPACE%
    echo.
) else (
    call :log_warning "无法获取 Pod 资源使用情况"
)
goto :eof

REM 检查存储
:check_storage
call :log_info "检查存储状态..."

for /f "skip=1 tokens=1,2,3,4" %%a in ('kubectl get pvc -n %NAMESPACE% --no-headers 2^>nul') do (
    set PVC_NAME=%%a
    set PVC_STATUS=%%b
    set PVC_VOLUME=%%c
    set PVC_CAPACITY=%%d
    
    if "!PVC_STATUS!"=="Bound" (
        call :log_success "PVC !PVC_NAME! 状态正常 (状态: !PVC_STATUS!, 容量: !PVC_CAPACITY!)"
    ) else (
        call :log_error "PVC !PVC_NAME! 状态异常 (状态: !PVC_STATUS!)"
    )
)
goto :eof

REM 主函数
:main
echo ==========================================
echo          NSRS 健康检查报告
echo ==========================================
echo 检查时间: %date% %time%
echo 命名空间: %NAMESPACE%
echo ==========================================
echo.

call :check_kubectl
echo.

call :check_namespace
echo.

call :check_pods
echo.

call :check_services
echo.

call :check_app_health
echo.

call :check_database
echo.

call :check_resources
echo.

call :check_storage
echo.

echo ==========================================
echo          健康检查完成
echo ==========================================

goto :eof

REM 执行主函数
call :main