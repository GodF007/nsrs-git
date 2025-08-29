# NSRS Kubernetes 快速开始指南

本指南将帮助您快速将 NSRS 系统部署到 Kubernetes 集群中。

## 🚀 快速部署

### 前提条件

1. **Kubernetes 集群**
   - Kubernetes 版本 1.19+
   - 至少 2 个节点，每个节点 2GB+ 内存
   - 已配置 kubectl 并能连接到集群

2. **Docker**
   - Docker 版本 20.10+
   - 能够构建和推送镜像

3. **工具检查**
   ```bash
   # 检查 kubectl
   kubectl version --client
   kubectl cluster-info
   
   # 检查 Docker
   docker --version
   docker info
   ```

### 一键部署

#### Windows 用户

```cmd
# 进入部署目录
cd d:\Code\NSRS\nsrs-deploy

# 执行部署脚本
deploy-k8s.bat deploy
```

#### Linux/Mac 用户

```bash
# 进入部署目录
cd /path/to/NSRS/nsrs-deploy

# 给脚本执行权限
chmod +x deploy-k8s.sh

# 执行部署脚本
./deploy-k8s.sh deploy
```

### 手动部署步骤

如果您想了解详细的部署过程，可以按以下步骤手动执行：

#### 1. 构建 Docker 镜像

```bash
# 在项目根目录执行
cd d:\Code\NSRS
docker build -f nsrs-deploy/k8s/Dockerfile -t nsrs:1.0.0 .
```

#### 2. 部署到 Kubernetes

```bash
# 进入 k8s 配置目录
cd nsrs-deploy/k8s

# 创建命名空间
kubectl apply -f namespace.yaml

# 部署 MySQL 数据库
kubectl apply -f mysql/

# 等待 MySQL 启动
kubectl wait --for=condition=ready pod -l app=mysql -n nsrs --timeout=300s

# 部署应用配置
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml

# 部署应用
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml

# 等待应用启动
kubectl wait --for=condition=ready pod -l app=nsrs -n nsrs --timeout=300s
```

## 📊 验证部署

### 检查部署状态

```bash
# 查看所有资源
kubectl get all -n nsrs

# 查看 Pod 状态
kubectl get pods -n nsrs -o wide

# 查看服务状态
kubectl get svc -n nsrs
```

### 访问应用

部署成功后，您可以通过以下方式访问应用：

#### 1. NodePort 访问（推荐用于测试）

```bash
# 获取 NodePort
kubectl get svc nsrs-nodeport -n nsrs

# 访问地址
http://节点IP:30080
```

#### 2. 端口转发访问

```bash
# 创建端口转发
kubectl port-forward svc/nsrs-service 8080:8080 -n nsrs

# 访问地址
http://localhost:8080
```

#### 3. Ingress 访问（生产环境推荐）

需要先安装 Ingress Controller（如 nginx-ingress）：

```bash
# 安装 nginx-ingress
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# 获取 Ingress IP
kubectl get ingress -n nsrs

# 访问地址
http://Ingress-IP/nsrs
```

### 健康检查

```bash
# 检查应用健康状态
curl http://节点IP:30080/actuator/health

# 查看应用信息
curl http://节点IP:30080/actuator/info
```

## 🔧 常用操作

### 查看日志

```bash
# 查看应用日志
kubectl logs -f deployment/nsrs-app -n nsrs

# 查看 MySQL 日志
kubectl logs -f deployment/mysql -n nsrs

# 查看特定 Pod 日志
kubectl logs <pod-name> -n nsrs
```

### 扩缩容

```bash
# 扩容到 3 个副本
kubectl scale deployment nsrs-app --replicas=3 -n nsrs

# 查看扩容状态
kubectl get pods -n nsrs -w
```

### 更新应用

```bash
# 构建新镜像
docker build -f nsrs-deploy/k8s/Dockerfile -t nsrs:1.0.1 .

# 更新部署
kubectl set image deployment/nsrs-app nsrs-app=nsrs:1.0.1 -n nsrs

# 查看更新状态
kubectl rollout status deployment/nsrs-app -n nsrs
```

### 重启应用

```bash
# 重启应用
kubectl rollout restart deployment/nsrs-app -n nsrs

# 使用脚本重启
deploy-k8s.bat restart
```

## 🛠️ 配置说明

### 环境变量配置

主要配置在 `configmap.yaml` 和 `secret.yaml` 中：

- **数据库配置**：`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- **应用配置**：`SERVER_PORT`, `SPRING_PROFILES_ACTIVE`
- **JPA 配置**：`SPRING_JPA_HIBERNATE_DDL_AUTO`, `SPRING_JPA_SHOW_SQL`

### 资源配置

默认资源配置：

- **NSRS 应用**：
  - CPU: 500m (请求) / 1000m (限制)
  - 内存: 1Gi (请求) / 2Gi (限制)

- **MySQL**：
  - CPU: 250m (请求) / 500m (限制)
  - 内存: 512Mi (请求) / 1Gi (限制)
  - 存储: 10Gi

### 网络配置

- **ClusterIP**：集群内部访问
- **NodePort**：通过节点端口访问（30080）
- **LoadBalancer**：云环境负载均衡器
- **Ingress**：HTTP/HTTPS 路由

## 🚨 故障排查

### 常见问题

#### 1. Pod 启动失败

```bash
# 查看 Pod 详情
kubectl describe pod <pod-name> -n nsrs

# 查看 Pod 日志
kubectl logs <pod-name> -n nsrs

# 查看事件
kubectl get events -n nsrs --sort-by='.lastTimestamp'
```

#### 2. 数据库连接失败

```bash
# 检查 MySQL 状态
kubectl get pods -l app=mysql -n nsrs

# 测试数据库连接
kubectl exec -it deployment/mysql -n nsrs -- mysql -u nsrs -p nsrs

# 检查网络连接
kubectl exec -it deployment/nsrs-app -n nsrs -- nc -zv mysql-service 3306
```

#### 3. 服务无法访问

```bash
# 检查服务状态
kubectl get svc -n nsrs

# 检查端点
kubectl get endpoints -n nsrs

# 测试服务连接
kubectl exec -it deployment/nsrs-app -n nsrs -- curl http://nsrs-service:8080/actuator/health
```

#### 4. 镜像拉取失败

```bash
# 检查镜像是否存在
docker images | grep nsrs

# 如果使用私有仓库，检查 Secret
kubectl get secret nsrs-registry-secret -n nsrs -o yaml
```

### 日志级别调整

如果需要更详细的日志，可以修改 `configmap.yaml` 中的日志配置：

```yaml
LOGGING_LEVEL_ROOT: "DEBUG"
LOGGING_LEVEL_COM_NSRS: "TRACE"
```

然后重新应用配置：

```bash
kubectl apply -f configmap.yaml
kubectl rollout restart deployment/nsrs-app -n nsrs
```

## 🗑️ 清理部署

### 完全卸载

```bash
# 使用脚本卸载
deploy-k8s.bat undeploy

# 或手动卸载
kubectl delete namespace nsrs
```

### 保留数据卸载

```bash
# 只删除应用，保留数据库
kubectl delete -f deployment.yaml
kubectl delete -f service.yaml
kubectl delete -f ingress.yaml
```

## 📚 进阶配置

### 启用 HTTPS

1. 安装 cert-manager
2. 配置域名和证书
3. 修改 `ingress.yaml` 中的 TLS 配置

### 监控和告警

1. 安装 Prometheus 和 Grafana
2. 配置监控指标收集
3. 设置告警规则

### 高可用部署

1. 使用多副本部署
2. 配置 Pod 反亲和性
3. 使用外部数据库（如云数据库）

### 性能优化

1. 调整 JVM 参数
2. 优化数据库连接池
3. 配置 HPA（水平自动扩缩容）

---

如果您在部署过程中遇到任何问题，请查看日志或联系技术支持。