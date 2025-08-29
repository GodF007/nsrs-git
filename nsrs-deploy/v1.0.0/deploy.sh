#!/bin/bash

# NSRS Kubernetes 部署脚本
# 使用方法: ./deploy-k8s.sh [action]
# action: deploy|undeploy|status|logs|restart

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
NAMESPACE="nsrs"
APP_NAME="nsrs"
IMAGE_NAME="nsrs:1.0.0"
K8S_BASE_DIR="k8s/base"
K8S_DATABASE_DIR="k8s/database"
K8S_SCALING_DIR="k8s/scaling"
K8S_MONITORING_DIR="k8s/monitoring"
DOCKER_DIR="docker"

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

# 检查依赖
check_dependencies() {
    log_info "检查依赖..."
    
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl 未安装或不在 PATH 中"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        log_error "docker 未安装或不在 PATH 中"
        exit 1
    fi
    
    # 检查 kubectl 连接
    if ! kubectl cluster-info &> /dev/null; then
        log_error "无法连接到 Kubernetes 集群"
        exit 1
    fi
    
    log_success "依赖检查通过"
}

# 构建 Docker 镜像
build_image() {
    log_info "构建 Docker 镜像..."
    
    cd ..
    
    # 检查 Dockerfile 是否存在
    if [ ! -f "nsrs-deploy/$DOCKER_DIR/Dockerfile" ]; then
        log_error "Dockerfile 不存在: nsrs-deploy/$DOCKER_DIR/Dockerfile"
        exit 1
    fi
    
    # 构建镜像
    docker build -f nsrs-deploy/$DOCKER_DIR/Dockerfile -t $IMAGE_NAME .
    
    if [ $? -eq 0 ]; then
        log_success "Docker 镜像构建成功: $IMAGE_NAME"
    else
        log_error "Docker 镜像构建失败"
        exit 1
    fi
    
    cd nsrs-deploy
}

# 部署到 Kubernetes
deploy() {
    log_info "开始部署 NSRS 到 Kubernetes..."
    
    # 检查 k8s 目录
    if [ ! -d "$K8S_BASE_DIR" ]; then
        log_error "K8s 配置目录不存在: $K8S_BASE_DIR"
        exit 1
    fi
    
    # 创建命名空间
    log_info "创建命名空间..."
    kubectl apply -f $K8S_BASE_DIR/namespace.yaml
    
    # 部署 MySQL
    log_info "部署 MySQL 数据库..."
    kubectl apply -f $K8S_DATABASE_DIR/
    
    # 等待 MySQL 就绪
    log_info "等待 MySQL 启动..."
    kubectl wait --for=condition=ready pod -l app=mysql -n $NAMESPACE --timeout=300s
    
    if [ $? -eq 0 ]; then
        log_success "MySQL 启动成功"
    else
        log_warning "MySQL 启动超时，继续部署应用..."
    fi
    
    # 部署应用配置
    log_info "部署应用配置..."
    kubectl apply -f $K8S_BASE_DIR/configmap.yaml
    kubectl apply -f $K8S_BASE_DIR/secret.yaml
    
    # 部署应用
    log_info "部署 NSRS 应用..."
    kubectl apply -f $K8S_BASE_DIR/deployment.yaml
    kubectl apply -f $K8S_BASE_DIR/service.yaml
    kubectl apply -f $K8S_BASE_DIR/ingress.yaml
    
    # 等待应用就绪
    log_info "等待应用启动..."
    kubectl wait --for=condition=ready pod -l app=$APP_NAME -n $NAMESPACE --timeout=300s
    
    if [ $? -eq 0 ]; then
        log_success "NSRS 应用部署成功"
        show_status
    else
        log_error "应用启动超时"
        show_logs
        exit 1
    fi
}

# 卸载部署
undeploy() {
    log_info "开始卸载 NSRS..."
    
    # 删除应用资源
    kubectl delete -f $K8S_BASE_DIR/ingress.yaml --ignore-not-found=true
    kubectl delete -f $K8S_BASE_DIR/service.yaml --ignore-not-found=true
    kubectl delete -f $K8S_BASE_DIR/deployment.yaml --ignore-not-found=true
    kubectl delete -f $K8S_BASE_DIR/configmap.yaml --ignore-not-found=true
    kubectl delete -f $K8S_BASE_DIR/secret.yaml --ignore-not-found=true
    
    # 删除 MySQL（注意：这会删除数据）
    read -p "是否删除 MySQL 数据库？这将删除所有数据 (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        kubectl delete -f $K8S_DATABASE_DIR/ --ignore-not-found=true
        log_warning "MySQL 数据库已删除"
    fi
    
    # 删除命名空间
    read -p "是否删除命名空间 $NAMESPACE？(y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        kubectl delete namespace $NAMESPACE --ignore-not-found=true
        log_success "命名空间 $NAMESPACE 已删除"
    fi
    
    log_success "NSRS 卸载完成"
}

# 显示状态
show_status() {
    log_info "NSRS 部署状态:"
    
    echo "\n=== Pods ==="
    kubectl get pods -n $NAMESPACE -o wide
    
    echo "\n=== Services ==="
    kubectl get svc -n $NAMESPACE
    
    echo "\n=== Ingress ==="
    kubectl get ingress -n $NAMESPACE
    
    echo "\n=== PVC ==="
    kubectl get pvc -n $NAMESPACE
    
    # 获取访问地址
    echo "\n=== 访问地址 ==="
    
    # NodePort 访问
    NODEPORT=$(kubectl get svc nsrs-nodeport -n $NAMESPACE -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null)
    if [ ! -z "$NODEPORT" ]; then
        NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="ExternalIP")].address}')
        if [ -z "$NODE_IP" ]; then
            NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
        fi
        echo "NodePort: http://$NODE_IP:$NODEPORT"
    fi
    
    # LoadBalancer 访问
    LB_IP=$(kubectl get svc nsrs-loadbalancer -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
    if [ ! -z "$LB_IP" ]; then
        echo "LoadBalancer: http://$LB_IP"
    fi
    
    # Ingress 访问
    INGRESS_IP=$(kubectl get ingress nsrs-ingress -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
    if [ ! -z "$INGRESS_IP" ]; then
        echo "Ingress: http://$INGRESS_IP/nsrs"
    fi
}

# 显示日志
show_logs() {
    log_info "显示应用日志:"
    kubectl logs -f deployment/nsrs-app -n $NAMESPACE --tail=100
}

# 重启应用
restart() {
    log_info "重启 NSRS 应用..."
    kubectl rollout restart deployment/nsrs-app -n $NAMESPACE
    kubectl rollout status deployment/nsrs-app -n $NAMESPACE
    log_success "应用重启完成"
}

# 主函数
main() {
    case "${1:-deploy}" in
        "build")
            check_dependencies
            build_image
            ;;
        "deploy")
            check_dependencies
            build_image
            deploy
            ;;
        "undeploy")
            check_dependencies
            undeploy
            ;;
        "status")
            check_dependencies
            show_status
            ;;
        "logs")
            check_dependencies
            show_logs
            ;;
        "restart")
            check_dependencies
            restart
            ;;
        "help")
            echo "使用方法: $0 [action]"
            echo "Actions:"
            echo "  build    - 仅构建 Docker 镜像"
            echo "  deploy   - 构建镜像并部署到 K8s（默认）"
            echo "  undeploy - 卸载部署"
            echo "  status   - 显示部署状态"
            echo "  logs     - 显示应用日志"
            echo "  restart  - 重启应用"
            echo "  help     - 显示帮助信息"
            ;;
        *)
            log_error "未知操作: $1"
            echo "使用 '$0 help' 查看帮助信息"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"