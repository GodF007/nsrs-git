# NSRS系统 Kubernetes 快速部署指南

## 🚀 5分钟快速部署

### 步骤1: 准备工作
```bash
# 检查kubectl连接
kubectl cluster-info

# 检查当前上下文
kubectl config current-context
```

### 步骤2: 修改配置
**重要**: 部署前必须修改以下配置文件中的连接信息

编辑 `k8s/configmap.yml`:
```yaml
# 修改数据库连接信息
ds0:
  url: jdbc:mysql://YOUR_DB_HOST:3324/pbs  # 替换为实际数据库地址
  username: YOUR_DB_USER                    # 替换为实际用户名
  password: YOUR_DB_PASSWORD                # 替换为实际密码

ds1:
  url: jdbc:mysql://YOUR_DB_HOST:3324/settledb_dev
  username: YOUR_DB_USER
  password: YOUR_DB_PASSWORD

# 修改Redis连接信息
redis:
  cluster:
    nodes: YOUR_REDIS_HOST1:7001,YOUR_REDIS_HOST2:7002  # 替换为实际Redis集群地址
```

编辑 `k8s/deployment.yml`:
```yaml
# 修改镜像地址
image: YOUR_REGISTRY/nsrs:1.0.0  # 替换为实际镜像地址
```

### 步骤3: 一键部署

**Windows用户:**
```cmd
cd simple-deploy\scripts
deploy.bat
```

**Linux/macOS用户:**
```bash
cd simple-deploy/scripts
chmod +x deploy.sh
./deploy.sh
```

### 步骤4: 验证部署
```bash
# 检查Pod状态
kubectl get pods -n nsrs

# 检查服务状态
kubectl get services -n nsrs

# 查看应用日志
kubectl logs -f deployment/nsrs -n nsrs
```

### 步骤5: 访问应用

**方式1: 外部访问 (推荐)**
```bash
# 获取Kubernetes节点IP
kubectl get nodes -o wide

# 通过NodePort访问 (端口30088)
# 应用首页: http://<节点IP>:30088/nsrs
# API文档: http://<节点IP>:30088/nsrs/doc.html
# 健康检查: http://<节点IP>:30088/nsrs/actuator/health
```

**方式2: 本地端口转发 (可选)**
```bash
# 设置端口转发
kubectl port-forward -n nsrs service/nsrs-service 8088:8088

# 在浏览器中访问
# 应用首页: http://localhost:8088/nsrs
# API文档: http://localhost:8088/nsrs/doc.html
```

## 🔧 常见问题

### Q: Pod一直处于Pending状态
A: 检查资源配额和节点资源是否充足
```bash
kubectl describe pod <pod-name> -n nsrs
kubectl top nodes
```

### Q: Pod启动失败
A: 查看详细错误信息
```bash
kubectl logs <pod-name> -n nsrs
kubectl describe pod <pod-name> -n nsrs
```

### Q: 无法连接数据库
A: 检查数据库连接配置和网络连通性
```bash
# 进入Pod测试连接
kubectl exec -it <pod-name> -n nsrs -- /bin/sh
# 在Pod内测试网络连通性
telnet YOUR_DB_HOST 3324
```

### Q: 健康检查失败
A: 检查应用启动状态和健康检查端点
```bash
# 查看健康检查端点
kubectl port-forward -n nsrs <pod-name> 8080:8080
curl http://localhost:8080/nsrs/actuator/health
```

## 🗑️ 清理部署
```bash
# 删除所有资源
kubectl delete namespace nsrs

# 或者逐个删除
kubectl delete -f k8s/deployment.yml
kubectl delete -f k8s/configmap.yml
kubectl delete -f k8s/namespace.yml
```

## 📋 部署检查清单

- [ ] Kubernetes集群可访问
- [ ] kubectl已配置
- [ ] 数据库服务可访问
- [ ] Redis服务可访问
- [ ] Docker镜像已构建并推送
- [ ] 配置文件中的连接信息已更新
- [ ] 资源配额充足
- [ ] 网络策略允许访问

---

💡 **提示**: 如需详细配置说明，请参考 [README.md](./README.md) 文件