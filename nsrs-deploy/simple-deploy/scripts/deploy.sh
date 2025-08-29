#!/bin/bash

# NSRS系统Kubernetes部署脚本
# 作用：自动化部署NSRS应用到Kubernetes集群
# 说明：此脚本会按顺序创建命名空间、ConfigMap、Deployment和Service等资源

set -e  # 遇到错误立即退出

echo "开始部署NSRS系统到Kubernetes集群..."

# 设置部署目录
DEPLOY_DIR="$(cd "$(dirname "$0")/.." && pwd)"
K8S_DIR="$DEPLOY_DIR/k8s"

echo "部署目录: $DEPLOY_DIR"
echo "Kubernetes配置目录: $K8S_DIR"

# 检查kubectl是否可用
if ! command -v kubectl &> /dev/null; then
    echo "错误: kubectl命令未找到，请确保已安装并配置kubectl"
    exit 1
fi

# 检查集群连接
echo "检查Kubernetes集群连接..."
if ! kubectl cluster-info &> /dev/null; then
    echo "错误: 无法连接到Kubernetes集群，请检查kubeconfig配置"
    exit 1
fi

echo "✓ Kubernetes集群连接正常"

# 1. 创建命名空间
echo "1. 创建命名空间 'nsrs'..."
kubectl apply -f "$K8S_DIR/namespace.yml"
echo "✓ 命名空间创建完成"

# 2. 创建ConfigMap配置
echo "2. 创建应用配置 (ConfigMap)..."
kubectl apply -f "$K8S_DIR/configmap.yml"
echo "✓ 应用配置创建完成"

# 3. 部署应用
echo "3. 部署NSRS应用..."
kubectl apply -f "$K8S_DIR/deployment.yml"
echo "✓ 应用部署完成"

# 4. 等待部署完成
echo "4. 等待应用启动..."
kubectl wait --for=condition=available --timeout=300s deployment/nsrs -n nsrs
echo "✓ 应用启动完成"

# 5. 显示部署状态
echo "\n=== 部署状态 ==="
echo "命名空间:"
kubectl get namespace nsrs

echo "\nPods状态:"
kubectl get pods -n nsrs

echo "\nServices状态:"
kubectl get services -n nsrs

echo "\nDeployment状态:"
kubectl get deployment -n nsrs

# 6. 显示访问信息
echo "\n=== 访问信息 ==="
echo "外部访问方式 (NodePort):"
echo "获取节点IP: kubectl get nodes -o wide"
echo "访问地址: http://<节点IP>:30088/nsrs"
echo "API文档地址: http://<节点IP>:30088/nsrs/doc.html"
echo "\n本地端口转发命令 (可选):"
echo "kubectl port-forward -n nsrs service/nsrs-service 8088:8088"
echo "本地访问地址: http://localhost:8088/nsrs"

echo "\n🎉 NSRS系统部署完成！"
echo "\n注意事项:"
echo "1. 外部访问已启用，通过NodePort 30088端口访问"
echo "2. 生产环境建议配置Ingress或LoadBalancer服务"
echo "3. 监控应用日志: kubectl logs -f deployment/nsrs -n nsrs"
echo "4. 健康检查端点: http://<节点IP>:30088/nsrs/actuator/health"