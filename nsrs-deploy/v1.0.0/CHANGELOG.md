# NSRS Kubernetes 部署配置变更记录

## [v1.0.0] - 2025-01-27

### 🎉 首次发布

这是 NSRS Kubernetes 部署配置的首个正式版本，提供了完整的容器化部署方案。

### ✨ 新增特性

#### 核心部署配置
- **Docker 镜像构建**: 多阶段构建优化，支持 OpenJDK 8
- **Kubernetes 部署**: 完整的 K8s 资源配置
- **命名空间隔离**: 独立的 `nsrs` 命名空间
- **配置管理**: ConfigMap 和 Secret 分离管理

#### 数据库支持
- **MySQL 8.0**: 完整的数据库部署配置
- **持久化存储**: 10GB PVC 存储卷
- **初始化脚本**: 自动创建数据库和用户
- **配置优化**: 针对容器环境的 MySQL 配置

#### 服务访问
- **ClusterIP**: 集群内部服务发现
- **NodePort**: 外部访问支持 (端口 30080)
- **LoadBalancer**: 云环境负载均衡器支持
- **Ingress**: HTTP/HTTPS 路由配置
- **Headless Service**: 服务发现支持

#### 健康检查
- **存活探针**: `/actuator/health` 端点检查
- **就绪探针**: 应用启动状态检查
- **启动探针**: 初始化阶段健康检查

#### 自动扩缩容
- **HPA**: 基于 CPU/内存的水平扩缩容
- **VPA**: 垂直资源自动调整 (可选)
- **PDB**: Pod 中断预算保证高可用

#### 监控告警
- **Prometheus 集成**: ServiceMonitor 配置
- **告警规则**: 应用、数据库、资源监控告警
- **Grafana 仪表板**: 预配置的监控面板
- **指标暴露**: Spring Boot Actuator 集成

#### 安全配置
- **网络策略**: 入站出站流量控制
- **Pod 安全上下文**: 非 root 用户运行
- **Secret 管理**: 敏感信息加密存储
- **RBAC**: 基于角色的访问控制 (预留)

#### 部署工具
- **自动化脚本**: Windows (.bat) 和 Linux (.sh) 部署脚本
- **一键部署**: 从构建到部署的完整自动化
- **状态检查**: 部署状态和健康状态查看
- **日志查看**: 应用和数据库日志查看

### 🔧 技术规格

#### 应用配置
- **基础镜像**: openjdk:8-jre-slim
- **应用端口**: 8080
- **JVM 参数**: G1GC, 容器感知
- **资源限制**: 1Gi-2Gi 内存, 500m-1000m CPU

#### 数据库配置
- **MySQL 版本**: 8.0
- **字符集**: utf8mb4
- **时区**: Asia/Shanghai
- **连接池**: HikariCP 优化配置

#### 网络配置
- **服务端口**: 8080 (HTTP)
- **NodePort**: 30080
- **数据库端口**: 3306
- **健康检查**: /actuator/health

### 📋 支持的功能

#### 部署操作
- [x] 一键部署 (`deploy`)
- [x] 状态查看 (`status`)
- [x] 日志查看 (`logs`)
- [x] 应用重启 (`restart`)
- [x] 完全卸载 (`undeploy`)
- [x] 镜像构建 (`build`)

#### 扩缩容
- [x] 手动扩缩容
- [x] 自动水平扩缩容 (HPA)
- [x] 垂直扩缩容建议 (VPA)
- [x] Pod 中断预算 (PDB)

#### 监控
- [x] Prometheus 指标收集
- [x] 应用性能监控
- [x] 数据库连接池监控
- [x] JVM 内存和 GC 监控
- [x] HTTP 请求监控

### 🚀 部署要求

#### 环境要求
- Kubernetes 1.19+
- Docker 20.10+
- kubectl 客户端
- 至少 2 个工作节点

#### 资源要求
- **最小**: 2 CPU, 4GB 内存, 20GB 存储
- **推荐**: 4 CPU, 8GB 内存, 50GB 存储

### 📝 配置文件清单

#### 基础配置
- `k8s/base/namespace.yaml` - 命名空间定义
- `k8s/base/configmap.yaml` - 应用配置
- `k8s/base/secret.yaml` - 敏感信息
- `k8s/base/deployment.yaml` - 应用部署
- `k8s/base/service.yaml` - 服务定义
- `k8s/base/ingress.yaml` - 入口配置

#### 数据库配置
- `k8s/database/mysql-configmap.yaml` - MySQL 配置
- `k8s/database/mysql-deployment.yaml` - MySQL 部署
- `k8s/database/mysql-service.yaml` - MySQL 服务
- `k8s/database/mysql-pvc.yaml` - 持久化存储

#### 扩展配置
- `k8s/scaling/hpa.yaml` - 自动扩缩容
- `k8s/monitoring/monitoring.yaml` - 监控配置

#### 构建配置
- `docker/Dockerfile` - 镜像构建文件

#### 部署脚本
- `deploy.bat` - Windows 部署脚本
- `deploy.sh` - Linux/Mac 部署脚本

### 🔄 升级路径

由于这是首个版本，暂无升级路径。后续版本将在此处提供详细的升级指南。

### 🐛 已知问题

目前暂无已知问题。如发现问题，请及时反馈。

### 📚 文档

- `README.md` - 版本概述和快速开始
- `DEPLOYMENT-GUIDE.md` - 详细部署指南
- `CHANGELOG.md` - 本文件，版本变更记录

---

## 版本规划

### [v1.1.0] - 计划中
- [ ] Helm Chart 支持
- [ ] 多环境配置模板
- [ ] 备份恢复方案
- [ ] 性能优化配置

### [v1.2.0] - 计划中
- [ ] Istio 服务网格支持
- [ ] 分布式追踪集成
- [ ] 高可用数据库方案
- [ ] CI/CD 流水线集成

---

**维护者**: NSRS 开发团队  
**最后更新**: 2025-01-27