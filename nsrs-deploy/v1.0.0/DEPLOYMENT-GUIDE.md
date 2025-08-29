# NSRS Kubernetes 详细部署指南 v1.0.0

本指南提供 NSRS 系统在 Kubernetes 环境中的完整部署说明。

## 📋 目录

1. [环境准备](#环境准备)
2. [配置说明](#配置说明)
3. [部署步骤](#部署步骤)
4. [验证部署](#验证部署)
5. [运维管理](#运维管理)
6. [故障排查](#故障排查)
7. [性能调优](#性能调优)
8. [安全配置](#安全配置)
9. [备份恢复](#备份恢复)

## 🔧 环境准备

### 系统要求

#### Kubernetes 集群
- **版本**: 1.19 或更高
- **节点数**: 最少 2 个工作节点
- **网络插件**: CNI 兼容 (Flannel, Calico, Weave 等)
- **存储**: 支持 PVC 的存储类

#### 资源要求

**最小配置**:
- CPU: 2 核心
- 内存: 4GB
- 存储: 20GB

**推荐配置**:
- CPU: 4 核心
- 内存: 8GB
- 存储: 50GB

**生产环境**:
- CPU: 8 核心
- 内存: 16GB
- 存储: 100GB

### 工具安装

#### kubectl 安装

**Windows**:
```powershell
# 使用 Chocolatey
choco install kubernetes-cli

# 或下载二进制文件
curl -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"
```

**Linux**:
```bash
# Ubuntu/Debian
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# CentOS/RHEL
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo mv kubectl /usr/local/bin/
sudo chmod +x /usr/local/bin/kubectl
```

**macOS**:
```bash
# 使用 Homebrew
brew install kubectl

# 或下载二进制文件
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/amd64/kubectl"
```

#### Docker 安装

**Windows**:
- 下载并安装 Docker Desktop
- 启用 Kubernetes 支持 (可选)

**Linux**:
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install docker.io
sudo systemctl enable docker
sudo systemctl start docker

# CentOS/RHEL
sudo yum install docker
sudo systemctl enable docker
sudo systemctl start docker
```

### 集群连接验证

```bash
# 检查集群连接
kubectl cluster-info

# 查看节点状态
kubectl get nodes

# 检查存储类
kubectl get storageclass
```

## ⚙️ 配置说明

### 环境变量配置

#### 应用配置 (configmap.yaml)

```yaml
# 数据库配置
DB_HOST: "mysql-service"          # 数据库主机
DB_PORT: "3306"                   # 数据库端口
DB_NAME: "nsrs"                   # 数据库名称

# 应用配置
SERVER_PORT: "8080"               # 应用端口
SPRING_PROFILES_ACTIVE: "k8s"     # Spring 配置文件

# JPA 配置
SPRING_JPA_HIBERNATE_DDL_AUTO: "update"  # 数据库表自动更新
SPRING_JPA_SHOW_SQL: "false"             # 是否显示 SQL

# 连接池配置
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "20"  # 最大连接数
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE: "5"        # 最小空闲连接
```

#### 敏感信息配置 (secret.yaml)

```yaml
# 数据库认证 (需要 base64 编码)
DB_USERNAME: bnNycw==      # nsrs
DB_PASSWORD: bnNyczEyMw==  # nsrs123
MYSQL_ROOT_PASSWORD: cm9vdDEyMw==  # root123
```

**生成 base64 编码**:
```bash
# Linux/Mac
echo -n "your-password" | base64

# Windows PowerShell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("your-password"))
```

### 资源配置

#### 应用资源限制

```yaml
resources:
  requests:
    memory: "1Gi"     # 内存请求
    cpu: "500m"       # CPU 请求
  limits:
    memory: "2Gi"     # 内存限制
    cpu: "1000m"      # CPU 限制
```

#### MySQL 资源限制

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

### 存储配置

#### 持久化存储卷

```yaml
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  # 指定存储类 (可选)
  # storageClassName: fast-ssd
```

## 🚀 部署步骤

### 方式一: 自动化部署 (推荐)

#### Windows 环境

```cmd
# 进入部署目录
cd d:\Code\NSRS\nsrs-deploy\v1.0.0

# 执行部署
deploy.bat

# 或指定具体操作
deploy.bat build    # 仅构建镜像
deploy.bat deploy   # 完整部署
deploy.bat status   # 查看状态
deploy.bat logs     # 查看日志
deploy.bat restart  # 重启应用
deploy.bat undeploy # 卸载部署
```

#### Linux/Mac 环境

```bash
# 进入部署目录
cd /path/to/NSRS/nsrs-deploy/v1.0.0

# 给脚本执行权限
chmod +x deploy.sh

# 执行部署
./deploy.sh

# 或指定具体操作
./deploy.sh build    # 仅构建镜像
./deploy.sh deploy   # 完整部署
./deploy.sh status   # 查看状态
./deploy.sh logs     # 查看日志
./deploy.sh restart  # 重启应用
./deploy.sh undeploy # 卸载部署
```

### 方式二: 手动部署

#### 1. 构建 Docker 镜像

```bash
# 进入项目根目录
cd d:\Code\NSRS

# 构建镜像
docker build -f nsrs-deploy/v1.0.0/docker/Dockerfile -t nsrs:1.0.0 .

# 验证镜像
docker images | grep nsrs
```

#### 2. 创建命名空间

```bash
kubectl apply -f k8s/base/namespace.yaml
```

#### 3. 部署数据库

```bash
# 部署 MySQL 相关资源
kubectl apply -f k8s/database/

# 等待 MySQL 启动
kubectl wait --for=condition=ready pod -l app=mysql -n nsrs --timeout=300s

# 验证 MySQL 状态
kubectl get pods -l app=mysql -n nsrs
```

#### 4. 部署应用配置

```bash
# 部署配置文件
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secret.yaml
```

#### 5. 部署应用

```bash
# 部署应用
kubectl apply -f k8s/base/deployment.yaml
kubectl apply -f k8s/base/service.yaml
kubectl apply -f k8s/base/ingress.yaml

# 等待应用启动
kubectl wait --for=condition=ready pod -l app=nsrs -n nsrs --timeout=300s
```

#### 6. 部署扩展功能 (可选)

```bash
# 部署自动扩缩容
kubectl apply -f k8s/scaling/hpa.yaml

# 部署监控配置
kubectl apply -f k8s/monitoring/monitoring.yaml
```

## ✅ 验证部署

### 检查资源状态

```bash
# 查看所有资源
kubectl get all -n nsrs

# 查看 Pod 详情
kubectl get pods -n nsrs -o wide

# 查看服务状态
kubectl get svc -n nsrs

# 查看 Ingress 状态
kubectl get ingress -n nsrs

# 查看持久化存储
kubectl get pvc -n nsrs
```

### 健康检查

```bash
# 获取 NodePort
NODEPORT=$(kubectl get svc nsrs-nodeport -n nsrs -o jsonpath='{.spec.ports[0].nodePort}')
NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')

# 检查应用健康状态
curl http://$NODE_IP:$NODEPORT/actuator/health

# 检查应用信息
curl http://$NODE_IP:$NODEPORT/actuator/info

# 检查 Prometheus 指标
curl http://$NODE_IP:$NODEPORT/actuator/prometheus
```

### 功能测试

```bash
# 端口转发到本地
kubectl port-forward svc/nsrs-service 8080:8080 -n nsrs &

# 测试 API 端点 (根据实际 API 调整)
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/health
```

## 🔧 运维管理

### 日志管理

#### 查看应用日志

```bash
# 查看实时日志
kubectl logs -f deployment/nsrs-app -n nsrs

# 查看最近 100 行日志
kubectl logs deployment/nsrs-app -n nsrs --tail=100

# 查看特定 Pod 日志
kubectl logs <pod-name> -n nsrs

# 查看上一个容器的日志
kubectl logs <pod-name> -n nsrs --previous
```

#### 查看数据库日志

```bash
# 查看 MySQL 日志
kubectl logs deployment/mysql -n nsrs

# 进入 MySQL 容器
kubectl exec -it deployment/mysql -n nsrs -- bash

# 在容器内查看错误日志
tail -f /var/log/mysql/error.log
```

### 扩缩容管理

#### 手动扩缩容

```bash
# 扩容到 3 个副本
kubectl scale deployment nsrs-app --replicas=3 -n nsrs

# 查看扩容状态
kubectl get pods -n nsrs -w

# 缩容到 1 个副本
kubectl scale deployment nsrs-app --replicas=1 -n nsrs
```

#### 自动扩缩容状态

```bash
# 查看 HPA 状态
kubectl get hpa -n nsrs

# 查看 HPA 详情
kubectl describe hpa nsrs-hpa -n nsrs

# 查看 VPA 状态 (如果启用)
kubectl get vpa -n nsrs
```

### 配置更新

#### 更新 ConfigMap

```bash
# 编辑 ConfigMap
kubectl edit configmap nsrs-config -n nsrs

# 或重新应用配置文件
kubectl apply -f k8s/base/configmap.yaml

# 重启应用以应用新配置
kubectl rollout restart deployment/nsrs-app -n nsrs
```

#### 更新 Secret

```bash
# 编辑 Secret
kubectl edit secret nsrs-secret -n nsrs

# 重启应用
kubectl rollout restart deployment/nsrs-app -n nsrs
```

### 应用更新

#### 滚动更新

```bash
# 构建新版本镜像
docker build -f docker/Dockerfile -t nsrs:1.0.1 .

# 更新部署镜像
kubectl set image deployment/nsrs-app nsrs-app=nsrs:1.0.1 -n nsrs

# 查看更新状态
kubectl rollout status deployment/nsrs-app -n nsrs

# 查看更新历史
kubectl rollout history deployment/nsrs-app -n nsrs
```

#### 回滚部署

```bash
# 回滚到上一个版本
kubectl rollout undo deployment/nsrs-app -n nsrs

# 回滚到指定版本
kubectl rollout undo deployment/nsrs-app --to-revision=2 -n nsrs
```

## 🐛 故障排查

### 常见问题诊断

#### Pod 启动失败

```bash
# 查看 Pod 状态
kubectl get pods -n nsrs

# 查看 Pod 详情
kubectl describe pod <pod-name> -n nsrs

# 查看 Pod 日志
kubectl logs <pod-name> -n nsrs

# 查看事件
kubectl get events -n nsrs --sort-by='.lastTimestamp'
```

**常见原因**:
- 镜像拉取失败
- 资源不足
- 配置错误
- 存储挂载失败

#### 数据库连接失败

```bash
# 检查 MySQL Pod 状态
kubectl get pods -l app=mysql -n nsrs

# 检查 MySQL 服务
kubectl get svc mysql-service -n nsrs

# 测试数据库连接
kubectl exec -it deployment/nsrs-app -n nsrs -- nc -zv mysql-service 3306

# 进入 MySQL 容器测试
kubectl exec -it deployment/mysql -n nsrs -- mysql -u nsrs -p nsrs
```

#### 服务无法访问

```bash
# 检查服务状态
kubectl get svc -n nsrs

# 检查端点
kubectl get endpoints -n nsrs

# 检查 Ingress 状态
kubectl get ingress -n nsrs
kubectl describe ingress nsrs-ingress -n nsrs

# 测试服务连通性
kubectl exec -it deployment/nsrs-app -n nsrs -- curl http://nsrs-service:8080/actuator/health
```

#### 资源不足

```bash
# 查看节点资源使用情况
kubectl top nodes

# 查看 Pod 资源使用情况
kubectl top pods -n nsrs

# 查看资源配额 (如果设置)
kubectl get resourcequota -n nsrs

# 查看限制范围 (如果设置)
kubectl get limitrange -n nsrs
```

### 性能问题诊断

#### JVM 性能分析

```bash
# 进入应用容器
kubectl exec -it deployment/nsrs-app -n nsrs -- bash

# 查看 JVM 进程
jps -v

# 查看 GC 情况
jstat -gc <pid>

# 查看内存使用
jmap -histo <pid>

# 生成堆转储 (谨慎使用)
jmap -dump:format=b,file=/tmp/heap.hprof <pid>
```

#### 数据库性能分析

```bash
# 进入 MySQL 容器
kubectl exec -it deployment/mysql -n nsrs -- mysql -u root -p

# 查看进程列表
SHOW PROCESSLIST;

# 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

# 查看连接数
SHOW STATUS LIKE 'Connections';
SHOW STATUS LIKE 'Threads_connected';

# 查看缓存命中率
SHOW STATUS LIKE 'Qcache%';
```

## ⚡ 性能调优

### JVM 调优

#### 内存配置

```yaml
# 在 deployment.yaml 中调整 JAVA_OPTS
env:
- name: JAVA_OPTS
  value: "-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

#### GC 调优

```yaml
# G1GC 配置
JAVA_OPTS: "-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m"

# ParallelGC 配置
JAVA_OPTS: "-XX:+UseParallelGC -XX:ParallelGCThreads=4"

# ZGC 配置 (JDK 11+)
JAVA_OPTS: "-XX:+UseZGC -XX:+UnlockExperimentalVMOptions"
```

### 数据库调优

#### MySQL 配置优化

```yaml
# 在 mysql-configmap.yaml 中调整
my.cnf: |
  [mysqld]
  # 连接配置
  max_connections = 500
  max_connect_errors = 10000
  
  # 缓冲区配置
  innodb_buffer_pool_size = 1G
  innodb_log_file_size = 256M
  innodb_log_buffer_size = 32M
  
  # 查询缓存
  query_cache_type = 1
  query_cache_size = 128M
  
  # 临时表
  tmp_table_size = 64M
  max_heap_table_size = 64M
```

#### 连接池优化

```yaml
# 在 configmap.yaml 中调整 HikariCP 配置
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "50"
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE: "10"
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT: "20000"
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT: "300000"
SPRING_DATASOURCE_HIKARI_MAX_LIFETIME: "1200000"
SPRING_DATASOURCE_HIKARI_LEAK_DETECTION_THRESHOLD: "60000"
```

### 网络优化

#### 服务网格 (可选)

```bash
# 安装 Istio
curl -L https://istio.io/downloadIstio | sh -
istioctl install --set values.defaultRevision=default

# 启用自动注入
kubectl label namespace nsrs istio-injection=enabled

# 重新部署应用
kubectl rollout restart deployment/nsrs-app -n nsrs
```

## 🔒 安全配置

### RBAC 配置

```yaml
# rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: nsrs-sa
  namespace: nsrs
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: nsrs-role
  namespace: nsrs
rules:
- apiGroups: [""]
  resources: ["pods", "services"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: nsrs-rolebinding
  namespace: nsrs
subjects:
- kind: ServiceAccount
  name: nsrs-sa
  namespace: nsrs
roleRef:
  kind: Role
  name: nsrs-role
  apiGroup: rbac.authorization.k8s.io
```

### Pod 安全策略

```yaml
# 在 deployment.yaml 中添加安全上下文
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  runAsGroup: 1000
  fsGroup: 1000
  capabilities:
    drop:
    - ALL
  readOnlyRootFilesystem: true
  allowPrivilegeEscalation: false
```

### 网络策略

```yaml
# 已包含在 monitoring.yaml 中
# 可根据需要调整网络访问规则
```

## 💾 备份恢复

### 数据库备份

#### 手动备份

```bash
# 创建备份
kubectl exec deployment/mysql -n nsrs -- mysqldump -u root -p$MYSQL_ROOT_PASSWORD nsrs > nsrs-backup-$(date +%Y%m%d).sql

# 恢复备份
kubectl exec -i deployment/mysql -n nsrs -- mysql -u root -p$MYSQL_ROOT_PASSWORD nsrs < nsrs-backup-20250127.sql
```

#### 自动备份 (CronJob)

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: mysql-backup
  namespace: nsrs
spec:
  schedule: "0 2 * * *"  # 每天凌晨 2 点
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: mysql-backup
            image: mysql:8.0
            command:
            - /bin/bash
            - -c
            - |
              mysqldump -h mysql-service -u root -p$MYSQL_ROOT_PASSWORD nsrs > /backup/nsrs-$(date +%Y%m%d-%H%M%S).sql
            env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: nsrs-secret
                  key: MYSQL_ROOT_PASSWORD
            volumeMounts:
            - name: backup-storage
              mountPath: /backup
          volumes:
          - name: backup-storage
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

### 配置备份

```bash
# 备份所有配置
kubectl get all,configmap,secret,pvc,ingress -n nsrs -o yaml > nsrs-config-backup-$(date +%Y%m%d).yaml

# 恢复配置
kubectl apply -f nsrs-config-backup-20250127.yaml
```

---

## 📞 技术支持

如果在部署过程中遇到问题，请提供以下信息：

1. **环境信息**:
   - Kubernetes 版本
   - 节点操作系统
   - 存储类型
   - 网络插件

2. **错误信息**:
   - Pod 状态和日志
   - 事件信息
   - 错误截图

3. **配置信息**:
   - 修改过的配置文件
   - 资源配置
   - 环境变量

**联系方式**: 请通过项目 Issue 或内部技术支持渠道联系。

---

**文档版本**: v1.0.0  
**最后更新**: 2025-01-27  
**维护者**: NSRS 开发团队