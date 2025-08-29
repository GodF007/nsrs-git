#!/bin/bash

# NSRS 健康检查脚本
# 用于检查 NSRS 应用和数据库的健康状态

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
NAMESPACE="nsrs"
APP_NAME="nsrs-app"
DB_NAME="mysql"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 kubectl 是否可用
check_kubectl() {
    log_info "检查 kubectl 连接..."
    if ! kubectl cluster-info &>/dev/null; then
        log_error "无法连接到 Kubernetes 集群"
        exit 1
    fi
    log_success "kubectl 连接正常"
}

# 检查命名空间
check_namespace() {
    log_info "检查命名空间 $NAMESPACE..."
    if ! kubectl get namespace $NAMESPACE &>/dev/null; then
        log_error "命名空间 $NAMESPACE 不存在"
        exit 1
    fi
    log_success "命名空间 $NAMESPACE 存在"
}

# 检查 Pod 状态
check_pods() {
    log_info "检查 Pod 状态..."
    
    # 检查应用 Pod
    APP_PODS=$(kubectl get pods -n $NAMESPACE -l app=$APP_NAME --no-headers 2>/dev/null || echo "")
    if [ -z "$APP_PODS" ]; then
        log_error "未找到应用 Pod"
        return 1
    fi
    
    echo "$APP_PODS" | while read line; do
        POD_NAME=$(echo $line | awk '{print $1}')
        POD_STATUS=$(echo $line | awk '{print $3}')
        POD_READY=$(echo $line | awk '{print $2}')
        
        if [ "$POD_STATUS" = "Running" ] && [[ "$POD_READY" == *"/"* ]]; then
            READY_COUNT=$(echo $POD_READY | cut -d'/' -f1)
            TOTAL_COUNT=$(echo $POD_READY | cut -d'/' -f2)
            if [ "$READY_COUNT" = "$TOTAL_COUNT" ]; then
                log_success "应用 Pod $POD_NAME 状态正常 ($POD_STATUS, $POD_READY)"
            else
                log_warning "应用 Pod $POD_NAME 未完全就绪 ($POD_STATUS, $POD_READY)"
            fi
        else
            log_error "应用 Pod $POD_NAME 状态异常 ($POD_STATUS, $POD_READY)"
        fi
    done
    
    # 检查数据库 Pod
    DB_PODS=$(kubectl get pods -n $NAMESPACE -l app=$DB_NAME --no-headers 2>/dev/null || echo "")
    if [ -z "$DB_PODS" ]; then
        log_error "未找到数据库 Pod"
        return 1
    fi
    
    echo "$DB_PODS" | while read line; do
        POD_NAME=$(echo $line | awk '{print $1}')
        POD_STATUS=$(echo $line | awk '{print $3}')
        POD_READY=$(echo $line | awk '{print $2}')
        
        if [ "$POD_STATUS" = "Running" ] && [[ "$POD_READY" == *"/"* ]]; then
            READY_COUNT=$(echo $POD_READY | cut -d'/' -f1)
            TOTAL_COUNT=$(echo $POD_READY | cut -d'/' -f2)
            if [ "$READY_COUNT" = "$TOTAL_COUNT" ]; then
                log_success "数据库 Pod $POD_NAME 状态正常 ($POD_STATUS, $POD_READY)"
            else
                log_warning "数据库 Pod $POD_NAME 未完全就绪 ($POD_STATUS, $POD_READY)"
            fi
        else
            log_error "数据库 Pod $POD_NAME 状态异常 ($POD_STATUS, $POD_READY)"
        fi
    done
}

# 检查服务状态
check_services() {
    log_info "检查服务状态..."
    
    SERVICES=$(kubectl get svc -n $NAMESPACE --no-headers 2>/dev/null || echo "")
    if [ -z "$SERVICES" ]; then
        log_error "未找到任何服务"
        return 1
    fi
    
    echo "$SERVICES" | while read line; do
        SVC_NAME=$(echo $line | awk '{print $1}')
        SVC_TYPE=$(echo $line | awk '{print $2}')
        SVC_CLUSTER_IP=$(echo $line | awk '{print $3}')
        SVC_EXTERNAL_IP=$(echo $line | awk '{print $4}')
        SVC_PORTS=$(echo $line | awk '{print $5}')
        
        log_success "服务 $SVC_NAME 正常 (类型: $SVC_TYPE, 集群IP: $SVC_CLUSTER_IP, 端口: $SVC_PORTS)"
    done
}

# 检查应用健康端点
check_app_health() {
    log_info "检查应用健康端点..."
    
    # 获取应用 Pod
    APP_POD=$(kubectl get pods -n $NAMESPACE -l app=$APP_NAME --no-headers | head -1 | awk '{print $1}' 2>/dev/null || echo "")
    if [ -z "$APP_POD" ]; then
        log_error "未找到应用 Pod"
        return 1
    fi
    
    # 检查健康端点
    log_info "检查 Pod $APP_POD 的健康端点..."
    
    # 检查 actuator/health
    if kubectl exec -n $NAMESPACE $APP_POD -- curl -s http://localhost:8080/actuator/health &>/dev/null; then
        HEALTH_STATUS=$(kubectl exec -n $NAMESPACE $APP_POD -- curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4 2>/dev/null || echo "unknown")
        if [ "$HEALTH_STATUS" = "UP" ]; then
            log_success "应用健康检查通过 (状态: $HEALTH_STATUS)"
        else
            log_warning "应用健康检查异常 (状态: $HEALTH_STATUS)"
        fi
    else
        log_error "无法访问应用健康端点"
    fi
    
    # 检查 actuator/info
    if kubectl exec -n $NAMESPACE $APP_POD -- curl -s http://localhost:8080/actuator/info &>/dev/null; then
        log_success "应用信息端点可访问"
    else
        log_warning "应用信息端点不可访问"
    fi
}

# 检查数据库连接
check_database() {
    log_info "检查数据库连接..."
    
    # 获取数据库 Pod
    DB_POD=$(kubectl get pods -n $NAMESPACE -l app=$DB_NAME --no-headers | head -1 | awk '{print $1}' 2>/dev/null || echo "")
    if [ -z "$DB_POD" ]; then
        log_error "未找到数据库 Pod"
        return 1
    fi
    
    # 检查 MySQL 连接
    log_info "检查 Pod $DB_POD 的数据库连接..."
    
    if kubectl exec -n $NAMESPACE $DB_POD -- mysql -u root -proot123 -e "SELECT 1" &>/dev/null; then
        log_success "数据库连接正常"
        
        # 检查数据库状态
        DB_STATUS=$(kubectl exec -n $NAMESPACE $DB_POD -- mysql -u root -proot123 -e "SHOW STATUS LIKE 'Threads_connected'" 2>/dev/null | tail -1 | awk '{print $2}' || echo "unknown")
        log_info "当前数据库连接数: $DB_STATUS"
        
        # 检查数据库大小
        DB_SIZE=$(kubectl exec -n $NAMESPACE $DB_POD -- mysql -u root -proot123 -e "SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 1) AS 'DB Size in MB' FROM information_schema.tables WHERE table_schema='nsrs'" 2>/dev/null | tail -1 || echo "unknown")
        log_info "数据库大小: ${DB_SIZE} MB"
    else
        log_error "数据库连接失败"
    fi
}

# 检查资源使用情况
check_resources() {
    log_info "检查资源使用情况..."
    
    # 检查节点资源
    if command -v kubectl &> /dev/null && kubectl top nodes &>/dev/null; then
        log_info "节点资源使用情况:"
        kubectl top nodes
        echo
    else
        log_warning "无法获取节点资源使用情况 (可能需要安装 metrics-server)"
    fi
    
    # 检查 Pod 资源
    if kubectl top pods -n $NAMESPACE &>/dev/null; then
        log_info "Pod 资源使用情况:"
        kubectl top pods -n $NAMESPACE
        echo
    else
        log_warning "无法获取 Pod 资源使用情况"
    fi
}

# 检查存储
check_storage() {
    log_info "检查存储状态..."
    
    PVC_STATUS=$(kubectl get pvc -n $NAMESPACE --no-headers 2>/dev/null || echo "")
    if [ -z "$PVC_STATUS" ]; then
        log_warning "未找到 PVC"
        return 0
    fi
    
    echo "$PVC_STATUS" | while read line; do
        PVC_NAME=$(echo $line | awk '{print $1}')
        PVC_STATUS=$(echo $line | awk '{print $2}')
        PVC_VOLUME=$(echo $line | awk '{print $3}')
        PVC_CAPACITY=$(echo $line | awk '{print $4}')
        
        if [ "$PVC_STATUS" = "Bound" ]; then
            log_success "PVC $PVC_NAME 状态正常 (状态: $PVC_STATUS, 容量: $PVC_CAPACITY)"
        else
            log_error "PVC $PVC_NAME 状态异常 (状态: $PVC_STATUS)"
        fi
    done
}

# 主函数
main() {
    echo "==========================================="
    echo "         NSRS 健康检查报告"
    echo "==========================================="
    echo "检查时间: $(date)"
    echo "命名空间: $NAMESPACE"
    echo "==========================================="
    echo
    
    check_kubectl
    echo
    
    check_namespace
    echo
    
    check_pods
    echo
    
    check_services
    echo
    
    check_app_health
    echo
    
    check_database
    echo
    
    check_resources
    echo
    
    check_storage
    echo
    
    echo "==========================================="
    echo "         健康检查完成"
    echo "==========================================="
}

# 执行主函数
main