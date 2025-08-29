# NSRS系统 Kubernetes 简化部署配置

本目录包含NSRS系统在Kubernetes环境中的简化部署配置文件，专门为NSRS应用本身设计，不包含数据库等外部依赖服务的部署配置。

## 📁 目录结构

```
simple-deploy/
├── Dockerfile              # NSRS应用容器镜像构建文件
├── README.md               # 本说明文档
├── k8s/                    # Kubernetes配置文件目录
│   ├── namespace.yml       # 命名空间配置
│   ├── configmap.yml       # 应用配置文件
│   └── deployment.yml      # 应用部署和服务配置
└── scripts/                # 部署脚本目录
    ├── deploy.sh           # Linux/macOS部署脚本
    └── deploy.bat          # Windows部署脚本
```

## 🔧 配置文件说明

### 1. Dockerfile
**作用**: 构建NSRS应用的Docker镜像  
**说明**: 
- 基于OpenJDK 8镜像
- 配置时区为Asia/Shanghai
- 设置JVM参数优化内存使用
- 暴露8080端口供HTTP访问
- 使用外部配置文件application-sharding.yml

### 2. namespace.yml
**作用**: 创建独立的Kubernetes命名空间  
**说明**: 
- 命名空间名称: `nsrs`
- 提供资源隔离和管理
- 便于权限控制和资源配额管理

### 3. configmap.yml
**作用**: 存储NSRS应用的运行时配置  
**说明**: 
- 基于原始的`application-sharding.yml`配置适配
- 包含数据库分片配置(ShardingSphere)
- 包含Redis集群配置
- 包含MyBatis-Plus、日志、API文档等配置
- **重要**: 需要根据实际环境修改数据库和Redis连接信息

### 4. deployment.yml
**作用**: 定义NSRS应用的部署规格和服务配置  
**说明**: 
- **Deployment部分**:
  - 副本数: 1个(可根据需要调整)
  - 资源限制: 内存1Gi，CPU 500m
  - 健康检查: 配置存活探针和就绪探针
  - 配置文件挂载: 将ConfigMap挂载为配置文件
- **Service部分**:
  - 类型: ClusterIP(集群内部访问)
  - 端口: 8080
  - 支持负载均衡

## 🚀 部署步骤

### 前置条件
1. 已安装并配置kubectl
2. 有可访问的Kubernetes集群
3. 已构建NSRS应用的Docker镜像
4. 数据库和Redis服务已部署并可访问

### 方法一: 使用自动化脚本

**Linux/macOS:**
```bash
cd simple-deploy/scripts
chmod +x deploy.sh
./deploy.sh
```

**Windows:**
```cmd
cd simple-deploy\scripts
deploy.bat
```

### 方法二: 手动部署

```bash
# 1. 创建命名空间
kubectl apply -f k8s/namespace.yml

# 2. 创建配置
kubectl apply -f k8s/configmap.yml

# 3. 部署应用
kubectl apply -f k8s/deployment.yml

# 4. 检查部署状态
kubectl get pods -n nsrs
kubectl get services -n nsrs
```

## 🔍 访问应用

### 集群内部访问
应用部署后可通过以下地址在集群内部访问:
- 服务地址: `http://nsrs-service.nsrs.svc.cluster.local:8080`
- 应用首页: `http://nsrs-service.nsrs.svc.cluster.local:8080/nsrs`

### 本地访问(端口转发)
```bash
# 设置端口转发
kubectl port-forward -n nsrs service/nsrs-service 8080:8080

# 访问地址
# 应用首页: http://localhost:8080/nsrs
# API文档: http://localhost:8080/nsrs/doc.html
```

### 外部访问
如需从集群外部访问，可以:
1. 修改Service类型为NodePort或LoadBalancer
2. 配置Ingress控制器
3. 使用kubectl proxy

## ⚙️ 配置自定义

### 修改数据库连接
编辑`k8s/configmap.yml`文件中的数据源配置:
```yaml
ds0:
  url: jdbc:mysql://YOUR_DB_HOST:PORT/DATABASE_NAME
  username: YOUR_USERNAME
  password: YOUR_PASSWORD
```

### 修改Redis连接
编辑`k8s/configmap.yml`文件中的Redis配置:
```yaml
redis:
  cluster:
    nodes: YOUR_REDIS_NODES
```

### 调整资源配置
编辑`k8s/deployment.yml`文件中的resources部分:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "2Gi"    # 根据需要调整
    cpu: "1000m"     # 根据需要调整
```

### 调整副本数
编辑`k8s/deployment.yml`文件:
```yaml
spec:
  replicas: 3  # 修改为所需副本数
```

## 🔧 故障排查

### 查看Pod状态
```bash
kubectl get pods -n nsrs
kubectl describe pod <pod-name> -n nsrs
```

### 查看应用日志
```bash
kubectl logs -f deployment/nsrs -n nsrs
```

### 查看配置是否正确
```bash
kubectl get configmap nsrs-config -n nsrs -o yaml
```

### 检查服务连通性
```bash
kubectl get services -n nsrs
kubectl describe service nsrs-service -n nsrs
```

## 📝 注意事项

1. **配置文件**: 部署前必须根据实际环境修改ConfigMap中的数据库和Redis连接信息
2. **镜像地址**: 需要根据实际构建的镜像修改Deployment中的镜像地址
3. **资源限制**: 根据实际负载调整CPU和内存限制
4. **安全性**: 生产环境建议使用Secret存储敏感信息如数据库密码
5. **持久化**: 如需持久化存储，请配置PersistentVolume
6. **监控**: 建议配置Prometheus等监控工具
7. **备份**: 定期备份配置文件和数据

## 🆘 技术支持

如遇到部署问题，请检查:
1. Kubernetes集群状态
2. 镜像是否可正常拉取
3. 配置文件语法是否正确
4. 网络连接是否正常
5. 资源配额是否充足