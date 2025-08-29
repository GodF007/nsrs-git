# NSRS Kubernetes 部署配置

本目录包含 NSRS 系统在 Kubernetes 环境中的部署配置，采用版本化管理方式。

## 📁 目录结构

```
nsrs-deploy/
├── README.md                    # 本文件
├── build-integrated.bat         # Windows 集成构建脚本
├── build-integrated.sh          # Linux/Mac 集成构建脚本
├── k8s/                         # 空目录 (历史遗留)
└── v1.0.0/                      # 版本 1.0.0 部署配置
    ├── README.md                # 版本说明
    ├── CHANGELOG.md             # 变更日志
    ├── DEPLOYMENT-GUIDE.md      # 详细部署指南
    ├── QUICK-START.md           # 快速开始指南
    ├── deploy.sh                # Linux/Mac 部署脚本
    ├── deploy.bat               # Windows 部署脚本
    ├── docker/                  # Docker 配置
    │   └── Dockerfile           # 应用镜像构建文件
    ├── k8s/                     # Kubernetes 配置文件
    │   ├── base/                # 基础配置
    │   │   ├── namespace.yaml   # 命名空间
    │   │   ├── configmap.yaml   # 配置映射
    │   │   ├── secret.yaml      # 密钥
    │   │   ├── deployment.yaml  # 应用部署
    │   │   ├── service.yaml     # 服务
    │   │   └── ingress.yaml     # 入口
    │   ├── database/            # 数据库配置
    │   │   ├── mysql-configmap.yaml
    │   │   ├── mysql-pvc.yaml
    │   │   ├── mysql-deployment.yaml
    │   │   └── mysql-service.yaml
    │   ├── scaling/             # 扩缩容配置
    │   │   └── hpa.yaml         # 水平自动扩缩容
    │   └── monitoring/          # 监控配置
    │       └── monitoring.yaml # 监控和告警
    └── scripts/                 # 辅助脚本
        ├── check-health.sh      # 健康检查脚本 (Linux/Mac)
        ├── check-health.bat     # 健康检查脚本 (Windows)
        ├── backup.sh            # 备份脚本
        └── cleanup.sh           # 清理脚本
```

## 🚀 快速开始

### 使用最新版本 (v1.0.0)

#### Windows 环境
```cmd
cd v1.0.0
deploy.bat
```

#### Linux/Mac 环境
```bash
cd v1.0.0
chmod +x deploy.sh
./deploy.sh
```

### 查看详细文档

- **快速开始**: [v1.0.0/QUICK-START.md](v1.0.0/QUICK-START.md)
- **详细部署指南**: [v1.0.0/DEPLOYMENT-GUIDE.md](v1.0.0/DEPLOYMENT-GUIDE.md)
- **版本说明**: [v1.0.0/README.md](v1.0.0/README.md)
- **变更日志**: [v1.0.0/CHANGELOG.md](v1.0.0/CHANGELOG.md)

## 📋 版本管理

### 当前版本

| 版本 | 状态 | 发布日期 | 说明 |
|------|------|----------|------|
| v1.0.0 | ✅ 稳定 | 2025-01-27 | 初始版本，包含完整的 K8s 部署配置 |

### 版本选择指南

- **生产环境**: 使用最新的稳定版本 (v1.0.0)
- **测试环境**: 可以使用最新版本进行测试
- **开发环境**: 建议使用最新版本

## 🔧 环境要求

### 基础要求
- **Kubernetes**: 1.19 或更高版本
- **kubectl**: 已配置并能连接到集群
- **Docker**: 用于构建镜像
- **存储**: 支持 PVC 的存储类

### 资源要求
- **最小**: 2 CPU, 4GB 内存, 20GB 存储
- **推荐**: 4 CPU, 8GB 内存, 50GB 存储
- **生产**: 8 CPU, 16GB 内存, 100GB 存储

## 🛠️ 常用操作

### 部署应用
```bash
# 进入版本目录
cd v1.0.0

# 执行部署
./deploy.sh deploy
```

### 查看状态
```bash
# 查看部署状态
./deploy.sh status

# 健康检查
./scripts/check-health.sh
```

### 查看日志
```bash
# 查看应用日志
./deploy.sh logs

# 或直接使用 kubectl
kubectl logs -f deployment/nsrs-app -n nsrs
```

### 备份数据
```bash
# 完整备份
./scripts/backup.sh

# 仅备份数据库
./scripts/backup.sh --db-only
```

### 清理部署
```bash
# 卸载应用
./deploy.sh undeploy

# 完全清理
./scripts/cleanup.sh
```

## 🔍 故障排查

### 常见问题

1. **Pod 启动失败**
   ```bash
   kubectl get pods -n nsrs
   kubectl describe pod <pod-name> -n nsrs
   kubectl logs <pod-name> -n nsrs
   ```

2. **服务无法访问**
   ```bash
   kubectl get svc -n nsrs
   kubectl get ingress -n nsrs
   ```

3. **数据库连接失败**
   ```bash
   kubectl exec -it deployment/mysql -n nsrs -- mysql -u root -p
   ```

### 获取帮助

- 查看详细部署指南: [v1.0.0/DEPLOYMENT-GUIDE.md](v1.0.0/DEPLOYMENT-GUIDE.md)
- 运行健康检查: `./scripts/check-health.sh`
- 查看应用日志: `kubectl logs -f deployment/nsrs-app -n nsrs`

## 📈 升级指南

### 版本升级步骤

1. **备份当前数据**
   ```bash
   cd v1.0.0
   ./scripts/backup.sh
   ```

2. **下载新版本配置**
   ```bash
   # 新版本发布后，将会有新的版本目录
   # 例如: v1.1.0/
   ```

3. **执行升级**
   ```bash
   cd v1.1.0  # 新版本目录
   ./deploy.sh deploy
   ```

4. **验证升级**
   ```bash
   ./scripts/check-health.sh
   ```

## 🤝 贡献指南

### 添加新版本

1. 创建新的版本目录 (例如: `v1.1.0/`)
2. 复制并修改配置文件
3. 更新 `README.md` 和 `CHANGELOG.md`
4. 测试部署流程
5. 更新本文档的版本表格

### 配置文件规范

- 使用语义化版本号 (Semantic Versioning)
- 保持向后兼容性
- 提供详细的变更说明
- 包含完整的测试用例

## 📞 技术支持

如果在使用过程中遇到问题，请提供以下信息：

1. **环境信息**:
   - Kubernetes 版本
   - 操作系统
   - 使用的配置版本

2. **错误信息**:
   - 错误日志
   - Pod 状态
   - 事件信息

3. **重现步骤**:
   - 操作步骤
   - 预期结果
   - 实际结果

---

**维护者**: NSRS 开发团队  
**最后更新**: 2025-01-27  
**当前版本**: v1.0.0