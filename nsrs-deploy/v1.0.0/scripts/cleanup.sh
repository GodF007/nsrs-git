#!/bin/bash

# NSRS 清理脚本
# 用于完全卸载 NSRS 部署和相关资源

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
NAMESPACE="nsrs"
FORCE_DELETE=false
KEEP_NAMESPACE=false
KEEP_PVC=false
DRY_RUN=false

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

# 显示帮助信息
show_help() {
    echo "NSRS 清理脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help              显示此帮助信息"
    echo "  -n, --namespace NS      指定命名空间 (默认: nsrs)"
    echo "  -f, --force             强制删除，不询问确认"
    echo "  --keep-namespace        保留命名空间"
    echo "  --keep-pvc              保留持久化存储卷"
    echo "  --dry-run               仅显示将要删除的资源，不实际执行"
    echo "  --app-only              仅删除应用，保留数据库"
    echo "  --db-only               仅删除数据库，保留应用"
    echo ""
    echo "示例:"
    echo "  $0                      # 完全清理 (交互式)"
    echo "  $0 --force              # 强制完全清理"
    echo "  $0 --keep-pvc           # 清理但保留数据"
    echo "  $0 --dry-run            # 预览清理操作"
    echo "  $0 --app-only           # 仅清理应用"
}

# 检查 kubectl 连接
check_kubectl() {
    if ! kubectl cluster-info &>/dev/null; then
        log_error "无法连接到 Kubernetes 集群"
        exit 1
    fi
}

# 检查命名空间是否存在
check_namespace() {
    if ! kubectl get namespace $NAMESPACE &>/dev/null; then
        log_warning "命名空间 $NAMESPACE 不存在，无需清理"
        exit 0
    fi
}

# 显示将要删除的资源
show_resources() {
    log_info "将要删除的资源 (命名空间: $NAMESPACE):"
    echo "----------------------------------------"
    
    # 显示 Deployments
    DEPLOYMENTS=$(kubectl get deployments -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$DEPLOYMENTS" ]; then
        echo "Deployments:"
        echo "$DEPLOYMENTS" | sed 's/^/  - /'
        echo
    fi
    
    # 显示 Services
    SERVICES=$(kubectl get services -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$SERVICES" ]; then
        echo "Services:"
        echo "$SERVICES" | sed 's/^/  - /'
        echo
    fi
    
    # 显示 ConfigMaps
    CONFIGMAPS=$(kubectl get configmaps -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$CONFIGMAPS" ]; then
        echo "ConfigMaps:"
        echo "$CONFIGMAPS" | sed 's/^/  - /'
        echo
    fi
    
    # 显示 Secrets
    SECRETS=$(kubectl get secrets -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$SECRETS" ]; then
        echo "Secrets:"
        echo "$SECRETS" | sed 's/^/  - /'
        echo
    fi
    
    # 显示 PVCs
    PVCS=$(kubectl get pvc -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$PVCS" ]; then
        echo "PersistentVolumeClaims:"
        echo "$PVCS" | sed 's/^/  - /'
        if [ "$KEEP_PVC" = true ]; then
            echo "  (将被保留)"
        fi
        echo
    fi
    
    # 显示 Ingress
    INGRESSES=$(kubectl get ingress -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$INGRESSES" ]; then
        echo "Ingresses:"
        echo "$INGRESSES" | sed 's/^/  - /'
        echo
    fi
    
    # 显示 HPA
    HPAS=$(kubectl get hpa -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$HPAS" ]; then
        echo "HorizontalPodAutoscalers:"
        echo "$HPAS" | sed 's/^/  - /'
        echo
    fi
    
    # 显示其他资源
    OTHER_RESOURCES=$(kubectl api-resources --verbs=list --namespaced -o name | grep -E "servicemonitor|networkpolicy|poddisruptionbudget" || echo "")
    if [ -n "$OTHER_RESOURCES" ]; then
        echo "其他资源:"
        echo "$OTHER_RESOURCES" | while read resource; do
            ITEMS=$(kubectl get $resource -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
            if [ -n "$ITEMS" ]; then
                echo "  $resource:"
                echo "$ITEMS" | sed 's/^/    - /'
            fi
        done
        echo
    fi
    
    echo "----------------------------------------"
    
    if [ "$KEEP_NAMESPACE" = false ]; then
        echo "命名空间: $NAMESPACE (将被删除)"
    else
        echo "命名空间: $NAMESPACE (将被保留)"
    fi
}

# 确认删除操作
confirm_deletion() {
    if [ "$FORCE_DELETE" = true ]; then
        return 0
    fi
    
    echo
    echo -n "确认要删除上述资源吗？[y/N]: "
    read -r CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        log_info "清理操作已取消"
        exit 0
    fi
}

# 删除应用资源
delete_app_resources() {
    log_info "删除应用资源..."
    
    # 删除 Deployments
    DEPLOYMENTS=$(kubectl get deployments -n $NAMESPACE -l app=nsrs-app --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$DEPLOYMENTS" ]; then
        echo "$DEPLOYMENTS" | while read deployment; do
            if [ -n "$deployment" ]; then
                log_info "删除 Deployment: $deployment"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete deployment $deployment -n $NAMESPACE --grace-period=30 || true
                fi
            fi
        done
    fi
    
    # 删除应用相关的 Services
    SERVICES=$(kubectl get services -n $NAMESPACE -l app=nsrs-app --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$SERVICES" ]; then
        echo "$SERVICES" | while read service; do
            if [ -n "$service" ]; then
                log_info "删除 Service: $service"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete service $service -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 删除数据库资源
delete_db_resources() {
    log_info "删除数据库资源..."
    
    # 删除 MySQL Deployment
    DEPLOYMENTS=$(kubectl get deployments -n $NAMESPACE -l app=mysql --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$DEPLOYMENTS" ]; then
        echo "$DEPLOYMENTS" | while read deployment; do
            if [ -n "$deployment" ]; then
                log_info "删除 MySQL Deployment: $deployment"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete deployment $deployment -n $NAMESPACE --grace-period=30 || true
                fi
            fi
        done
    fi
    
    # 删除 MySQL Services
    SERVICES=$(kubectl get services -n $NAMESPACE -l app=mysql --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$SERVICES" ]; then
        echo "$SERVICES" | while read service; do
            if [ -n "$service" ]; then
                log_info "删除 MySQL Service: $service"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete service $service -n $NAMESPACE || true
                fi
            fi
        done
    fi
    
    # 删除 MySQL ConfigMaps
    CONFIGMAPS=$(kubectl get configmaps -n $NAMESPACE -l app=mysql --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$CONFIGMAPS" ]; then
        echo "$CONFIGMAPS" | while read configmap; do
            if [ -n "$configmap" ]; then
                log_info "删除 MySQL ConfigMap: $configmap"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete configmap $configmap -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 删除配置资源
delete_config_resources() {
    log_info "删除配置资源..."
    
    # 删除 ConfigMaps (排除 MySQL)
    CONFIGMAPS=$(kubectl get configmaps -n $NAMESPACE --no-headers 2>/dev/null | grep -v mysql | awk '{print $1}' || echo "")
    if [ -n "$CONFIGMAPS" ]; then
        echo "$CONFIGMAPS" | while read configmap; do
            if [ -n "$configmap" ] && [ "$configmap" != "kube-root-ca.crt" ]; then
                log_info "删除 ConfigMap: $configmap"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete configmap $configmap -n $NAMESPACE || true
                fi
            fi
        done
    fi
    
    # 删除 Secrets (排除默认的)
    SECRETS=$(kubectl get secrets -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$SECRETS" ]; then
        echo "$SECRETS" | while read secret; do
            if [ -n "$secret" ] && [[ "$secret" != "default-token-"* ]]; then
                log_info "删除 Secret: $secret"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete secret $secret -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 删除网络资源
delete_network_resources() {
    log_info "删除网络资源..."
    
    # 删除 Ingress
    INGRESSES=$(kubectl get ingress -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$INGRESSES" ]; then
        echo "$INGRESSES" | while read ingress; do
            if [ -n "$ingress" ]; then
                log_info "删除 Ingress: $ingress"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete ingress $ingress -n $NAMESPACE || true
                fi
            fi
        done
    fi
    
    # 删除 NetworkPolicy
    NETWORKPOLICIES=$(kubectl get networkpolicy -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$NETWORKPOLICIES" ]; then
        echo "$NETWORKPOLICIES" | while read policy; do
            if [ -n "$policy" ]; then
                log_info "删除 NetworkPolicy: $policy"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete networkpolicy $policy -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 删除扩缩容资源
delete_scaling_resources() {
    log_info "删除扩缩容资源..."
    
    # 删除 HPA
    HPAS=$(kubectl get hpa -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$HPAS" ]; then
        echo "$HPAS" | while read hpa; do
            if [ -n "$hpa" ]; then
                log_info "删除 HPA: $hpa"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete hpa $hpa -n $NAMESPACE || true
                fi
            fi
        done
    fi
    
    # 删除 VPA (如果存在)
    VPAS=$(kubectl get vpa -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$VPAS" ]; then
        echo "$VPAS" | while read vpa; do
            if [ -n "$vpa" ]; then
                log_info "删除 VPA: $vpa"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete vpa $vpa -n $NAMESPACE || true
                fi
            fi
        done
    fi
    
    # 删除 PDB
    PDBS=$(kubectl get pdb -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$PDBS" ]; then
        echo "$PDBS" | while read pdb; do
            if [ -n "$pdb" ]; then
                log_info "删除 PodDisruptionBudget: $pdb"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete pdb $pdb -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 删除监控资源
delete_monitoring_resources() {
    log_info "删除监控资源..."
    
    # 删除 ServiceMonitor
    SERVICEMONITORS=$(kubectl get servicemonitor -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$SERVICEMONITORS" ]; then
        echo "$SERVICEMONITORS" | while read sm; do
            if [ -n "$sm" ]; then
                log_info "删除 ServiceMonitor: $sm"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete servicemonitor $sm -n $NAMESPACE || true
                fi
            fi
        done
    fi
    
    # 删除 PrometheusRule
    PROMETHEUSRULES=$(kubectl get prometheusrule -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$PROMETHEUSRULES" ]; then
        echo "$PROMETHEUSRULES" | while read rule; do
            if [ -n "$rule" ]; then
                log_info "删除 PrometheusRule: $rule"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete prometheusrule $rule -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 删除存储资源
delete_storage_resources() {
    if [ "$KEEP_PVC" = true ]; then
        log_warning "跳过 PVC 删除 (--keep-pvc 选项)"
        return 0
    fi
    
    log_info "删除存储资源..."
    
    # 删除 PVCs
    PVCS=$(kubectl get pvc -n $NAMESPACE --no-headers 2>/dev/null | awk '{print $1}' || echo "")
    if [ -n "$PVCS" ]; then
        echo "$PVCS" | while read pvc; do
            if [ -n "$pvc" ]; then
                log_warning "删除 PVC: $pvc (数据将丢失!)"
                if [ "$DRY_RUN" = false ]; then
                    kubectl delete pvc $pvc -n $NAMESPACE || true
                fi
            fi
        done
    fi
}

# 等待资源删除完成
wait_for_deletion() {
    log_info "等待资源删除完成..."
    
    # 等待 Pods 删除
    local timeout=120
    local count=0
    while [ $count -lt $timeout ]; do
        PODS=$(kubectl get pods -n $NAMESPACE --no-headers 2>/dev/null | wc -l || echo "0")
        if [ "$PODS" -eq 0 ]; then
            break
        fi
        
        if [ $((count % 10)) -eq 0 ]; then
            log_info "等待 $PODS 个 Pod 删除... ($count/$timeout 秒)"
        fi
        
        sleep 1
        count=$((count + 1))
    done
    
    if [ $count -ge $timeout ]; then
        log_warning "等待超时，仍有 Pod 未删除完成"
        kubectl get pods -n $NAMESPACE 2>/dev/null || true
    else
        log_success "所有 Pod 已删除完成"
    fi
}

# 删除命名空间
delete_namespace() {
    if [ "$KEEP_NAMESPACE" = true ]; then
        log_info "保留命名空间 $NAMESPACE"
        return 0
    fi
    
    log_info "删除命名空间: $NAMESPACE"
    
    if [ "$DRY_RUN" = false ]; then
        kubectl delete namespace $NAMESPACE --grace-period=30 || true
        
        # 等待命名空间删除完成
        local timeout=60
        local count=0
        while [ $count -lt $timeout ]; do
            if ! kubectl get namespace $NAMESPACE &>/dev/null; then
                break
            fi
            
            if [ $((count % 10)) -eq 0 ]; then
                log_info "等待命名空间删除... ($count/$timeout 秒)"
            fi
            
            sleep 1
            count=$((count + 1))
        done
        
        if [ $count -ge $timeout ]; then
            log_warning "命名空间删除超时"
        else
            log_success "命名空间已删除"
        fi
    fi
}

# 清理 Docker 镜像 (可选)
cleanup_docker_images() {
    log_info "清理本地 Docker 镜像..."
    
    # 查找 NSRS 相关镜像
    NSRS_IMAGES=$(docker images | grep -E "nsrs|NSRS" | awk '{print $3}' 2>/dev/null || echo "")
    
    if [ -n "$NSRS_IMAGES" ]; then
        echo "找到以下 NSRS 相关镜像:"
        docker images | grep -E "nsrs|NSRS" || true
        echo
        
        if [ "$FORCE_DELETE" = false ]; then
            echo -n "是否删除这些镜像？[y/N]: "
            read -r CONFIRM
            if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
                log_info "跳过镜像清理"
                return 0
            fi
        fi
        
        if [ "$DRY_RUN" = false ]; then
            echo "$NSRS_IMAGES" | while read image_id; do
                if [ -n "$image_id" ]; then
                    log_info "删除镜像: $image_id"
                    docker rmi $image_id --force 2>/dev/null || true
                fi
            done
            
            # 清理悬空镜像
            docker image prune -f &>/dev/null || true
            log_success "Docker 镜像清理完成"
        fi
    else
        log_info "未找到 NSRS 相关镜像"
    fi
}

# 主函数
main() {
    local APP_ONLY=false
    local DB_ONLY=false
    local CLEANUP_DOCKER=false
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -f|--force)
                FORCE_DELETE=true
                shift
                ;;
            --keep-namespace)
                KEEP_NAMESPACE=true
                shift
                ;;
            --keep-pvc)
                KEEP_PVC=true
                shift
                ;;
            --dry-run)
                DRY_RUN=true
                shift
                ;;
            --app-only)
                APP_ONLY=true
                shift
                ;;
            --db-only)
                DB_ONLY=true
                shift
                ;;
            --cleanup-docker)
                CLEANUP_DOCKER=true
                shift
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    echo "==========================================="
    echo "         NSRS 清理工具"
    echo "==========================================="
    echo "时间: $(date)"
    echo "命名空间: $NAMESPACE"
    if [ "$DRY_RUN" = true ]; then
        echo "模式: 预览模式 (不会实际删除)"
    elif [ "$FORCE_DELETE" = true ]; then
        echo "模式: 强制删除"
    else
        echo "模式: 交互式删除"
    fi
    echo "==========================================="
    echo
    
    check_kubectl
    check_namespace
    
    # 显示将要删除的资源
    show_resources
    
    # 确认删除
    if [ "$DRY_RUN" = false ]; then
        confirm_deletion
    else
        log_info "预览模式，不会实际执行删除操作"
        exit 0
    fi
    
    echo
    log_info "开始清理操作..."
    
    # 根据选项执行不同的清理操作
    if [ "$APP_ONLY" = true ]; then
        delete_app_resources
    elif [ "$DB_ONLY" = true ]; then
        delete_db_resources
    else
        # 完整清理
        delete_scaling_resources
        delete_monitoring_resources
        delete_network_resources
        delete_app_resources
        delete_db_resources
        delete_config_resources
        
        # 等待资源删除
        wait_for_deletion
        
        # 删除存储
        delete_storage_resources
        
        # 删除命名空间
        delete_namespace
    fi
    
    # 清理 Docker 镜像
    if [ "$CLEANUP_DOCKER" = true ]; then
        echo
        cleanup_docker_images
    fi
    
    echo
    echo "==========================================="
    echo "         清理完成"
    echo "==========================================="
    
    # 显示清理后的状态
    if kubectl get namespace $NAMESPACE &>/dev/null; then
        log_info "命名空间 $NAMESPACE 中剩余的资源:"
        kubectl get all -n $NAMESPACE 2>/dev/null || log_info "无剩余资源"
    else
        log_success "命名空间 $NAMESPACE 已完全删除"
    fi
}

# 执行主函数
main "$@"