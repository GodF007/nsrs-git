# NSRS号卡资源管理系统

号卡资源管理系统（Network SIM Resource System，简称NSRS）是一个专业的网络SIM卡资源管理系统，用于管理网络通信中使用的SIM卡资源、号码资源和IMSI资源。系统采用微服务架构设计，支持数据分片和高并发处理。

## 🏗️ 项目架构

### 技术栈
- **后端**: Spring Boot 2.x + MyBatis-Plus + ShardingSphere
- **前端**: React 19 + TypeScript + Ant Design + Vite
- **数据库**: MySQL 8.0 (支持分库分表)
- **缓存**: Redis (支持集群)
- **容器化**: Docker + Kubernetes
- **API文档**: Knife4j (Swagger)

### 系统特性
- 🔄 **数据分片**: 基于ShardingSphere实现IMSI资源表分片存储
- 🚀 **高性能**: 支持批量操作和异步处理
- 🌐 **国际化**: 支持中英文切换
- 🔒 **分布式锁**: 基于Redis的分布式锁机制
- 📊 **监控运维**: 集成Actuator健康检查和指标监控
- 🐳 **容器化部署**: 完整的Kubernetes部署方案

## 📦 模块结构

```
nsrs-parent/
├── nsrs-boot/          # 启动模块
├── nsrs-common/        # 公共模块
├── nsrs-framework/     # 框架模块
├── nsrs-binding/       # 号码IMSI绑定模块
├── nsrs-msisdn/        # 号码资源管理模块
├── nsrs-simcard/       # SIM卡资源管理模块
├── nsrs-web/           # 前端界面模块
└── nsrs-deploy/        # 部署配置模块
```

## 🚀 模块功能详解

### nsrs-boot (启动模块)
**功能**: Spring Boot应用启动入口
- 应用主类和启动配置
- 基础配置文件 (application.yml)
- Knife4j API文档配置
- 服务端口: 8088
- 上下文路径: /nsrs

### nsrs-common (公共模块)
**功能**: 提供通用工具类和基础组件

#### 核心组件
- **枚举类**: 
  - `NumberStatusEnum`: 号码状态枚举
  - `OperationTypeEnum`: 操作类型枚举
  - `ApprovalStatusEnum`: 审批状态枚举
  - `BatchOperationTypeEnum`: 批量操作类型枚举
  - `ResultStatusEnum`: 结果状态枚举

- **工具类**:
  - `SequenceService`: 序列号生成服务
  - `ShardingBatchUpdateUtils`: 分片批量更新工具
  - `EnumUtils`: 枚举工具类
  - `MessageUtils`: 消息工具类

- **异常处理**:
  - `BaseException`: 基础异常类
  - `BusinessException`: 业务异常类
  - `AuthorizationException`: 授权异常类

- **统一响应**:
  - `CommonResult`: 通用响应结果
  - `PageResult`: 分页结果封装
  - `R`: 响应结果工具类

### nsrs-framework (框架模块)
**功能**: 提供基础框架配置和基础设施支持

#### 配置组件
- **数据库配置**: 
  - `DruidConfig`: 数据源配置
  - `MybatisPlusConfig`: MyBatis-Plus配置
  - `CustomShardingConfigLoader`: 自定义分片配置加载器
  - `TransactionConfig`: 事务配置

- **缓存配置**:
  - `RedisConfig`: Redis配置

- **Web配置**:
  - `WebConfig`: Web基础配置
  - `WebMvcConfig`: MVC配置
  - `CorsConfig`: 跨域配置
  - `SwaggerConfig`: API文档配置

- **国际化配置**:
  - `I18nConfig`: 国际化配置

#### 基础设施组件
- **分布式锁**:
  - `DistributedLock`: 分布式锁接口
  - `RedisDistributedLock`: Redis分布式锁实现
  - `@DistributedLockable`: 分布式锁注解
  - `DistributedLockAspect`: 分布式锁切面

- **分片算法**:
  - `IccidHashShardingAlgorithm`: ICCID哈希分片算法
  - `MsisdnPrefixShardingAlgorithm`: MSISDN前缀分片算法

- **异常处理**:
  - `GlobalExceptionHandler`: 全局异常处理器

### nsrs-binding (号码IMSI绑定模块)
**功能**: 管理号码与IMSI的绑定关系

#### 核心功能
- **号码IMSI绑定管理**:
  - 号码与IMSI的一对一绑定
  - 绑定关系的创建、查询、更新、删除
  - 绑定状态管理

- **批量绑定任务**:
  - 批量绑定任务的创建和管理
  - 任务执行状态跟踪
  - 任务结果统计

- **批量绑定详情**:
  - 批量操作的详细记录
  - 成功/失败记录跟踪
  - 错误信息记录

#### 数据表
- `number_imsi_binding`: 号码IMSI绑定关系表
- `batch_binding_task`: 批量绑定任务表
- `batch_binding_detail`: 批量绑定详情表

#### API接口
- 号码IMSI绑定CRUD操作
- 批量绑定任务管理
- 绑定关系查询和统计

### nsrs-msisdn (号码资源管理模块)
**功能**: 管理电话号码资源的全生命周期

#### 核心功能
- **号码资源管理**:
  - 号码资源的CRUD操作
  - 号码状态管理 (空闲、已用、预留、激活、冻结、阻塞、释放)
  - 批量导入和导出
  - 跨表查询支持

- **号码段管理**:
  - 号码段的创建和配置
  - 号码段容量统计
  - 号码段状态管理

- **号码级别管理**:
  - 号码级别定义和管理
  - 级别权限控制

- **号码模式管理**:
  - 号码格式模式定义
  - 正则表达式验证
  - 模式匹配规则

- **号码审批管理**:
  - 号码申请流程
  - 审批工作流
  - 审批状态跟踪

- **HLR/交换机管理**:
  - HLR设备管理
  - 交换机配置
  - 设备状态监控

- **操作日志**:
  - 号码操作记录
  - 操作审计跟踪
  - 日志查询和统计

#### 数据表
- `number_resource`: 号码资源表
- `number_segment`: 号码段表
- `number_level`: 号码级别表
- `number_pattern`: 号码模式表
- `number_approval`: 号码审批表
- `number_operation_log`: 号码操作日志表
- `hlr_switch`: HLR/交换机表

### nsrs-simcard (SIM卡资源管理模块)
**功能**: 管理SIM卡和IMSI资源

#### SIM卡管理
- SIM卡CRUD操作
- SIM卡批量导入
- SIM卡状态管理
- SIM卡分配与回收

#### IMSI资源管理
- **IMSI组管理**: 
  - 创建IMSI组，设置IMSI范围
  - 支持不同类型的IMSI
  - IMSI组配置和管理

- **IMSI资源生成**: 
  - 根据IMSI组配置按顺序生成IMSI资源
  - 批量生成和导入
  - 资源预分配

- **IMSI资源查询**: 
  - 按IMSI号码、组、供应商、状态等条件查询
  - 支持模糊查询和精确查询
  - 分页查询支持

- **IMSI资源状态管理**: 
  - 状态变更和批量操作
  - 状态流转控制
  - 状态统计报表

#### 数据表分区
- IMSI资源表按尾缀分表，共分为10个表（imsi_resource_0 至 imsi_resource_9）
- 根据IMSI号码最后一位确定数据存储的表
- 支持跨表查询和统计

#### API接口
- `GET /simcard/imsi/group/page`: 分页查询IMSI组列表
- `GET /simcard/imsi/group/{groupId}`: 获取IMSI组详情
- `POST /simcard/imsi/group`: 添加IMSI组
- `PUT /simcard/imsi/group`: 修改IMSI组
- `DELETE /simcard/imsi/group/{groupId}`: 删除IMSI组
- `GET /simcard/imsi/group/list`: 获取所有IMSI组

### nsrs-web (前端界面模块)
**功能**: 提供Web用户界面

#### 技术栈
- **框架**: React 19 + TypeScript
- **UI组件**: Ant Design 5.x
- **路由**: TanStack Router
- **状态管理**: Zustand
- **构建工具**: Vite 7.x
- **样式**: Tailwind CSS
- **图表**: Ant Design Charts
- **国际化**: i18next
- **HTTP客户端**: Axios
- **Mock数据**: MockJS

#### 功能模块
- **SIM卡管理**: SIM卡资源的增删改查界面
- **IMSI管理**: IMSI资源管理界面
- **号码管理**: 号码资源管理界面
- **绑定管理**: 号码IMSI绑定管理界面
- **卡选管理**: 卡片选择管理界面

#### 开发配置
- 开发服务器端口: 3002
- 支持热重载和模块热替换
- 集成ESLint和Prettier代码规范
- 支持SVG图标组件化
- Mock数据支持

### nsrs-deploy (部署配置模块)
**功能**: 提供完整的部署解决方案

#### 部署方式
- **简化部署** (`simple-deploy/`): 适用于开发和测试环境
- **版本化部署** (`v1.0.0/`): 适用于生产环境

#### 容器化配置
- **Dockerfile**: 基于OpenJDK 8的应用镜像
- **Kubernetes配置**:
  - Namespace: 资源隔离
  - ConfigMap: 应用配置管理
  - Deployment: 应用部署规格
  - Service: 服务发现和负载均衡
  - Ingress: 外部访问入口

#### 部署脚本
- **集成构建脚本**: `build-integrated.sh/bat`
- **部署脚本**: `deploy.sh/bat`
- **自动化部署**: 支持一键部署

## 🔧 系统配置

### 数据库分片配置
```yaml
spring:
  shardingsphere:
    datasource:
      names: ds0
    sharding:
      tables:
        imsi_resource:
          actual-data-nodes: ds0.imsi_resource_$->{0..9}
          table-strategy:
            inline:
              sharding-column: imsi
              algorithm-expression: imsi_resource_$->{imsi.longValue() % 10}
```

### Redis配置
```yaml
spring:
  redis:
    cluster:
      nodes:
        - redis-cluster-0.redis-cluster:6379
        - redis-cluster-1.redis-cluster:6379
        - redis-cluster-2.redis-cluster:6379
```

## 🚀 快速开始

### 环境要求
- JDK 8+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 本地开发

1. **克隆项目**
```bash
git clone <repository-url>
cd NSRS
```

2. **后端启动**
```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run -pl nsrs-boot
```

3. **前端启动**
```bash
cd nsrs-web
npm install
npm run dev
```

4. **访问应用**
- 后端API: http://localhost:8088/nsrs
- API文档: http://localhost:8088/nsrs/doc.html
- 前端界面: http://localhost:3002

### 生产部署

1. **构建应用**
```bash
# 集成构建（前端+后端）
./nsrs-deploy/build-integrated.sh
```

2. **Kubernetes部署**
```bash
cd nsrs-deploy/simple-deploy/scripts
./deploy.sh
```

3. **访问应用**
- 外部访问: http://\<节点IP\>:30088/nsrs
- API文档: http://\<节点IP\>:30088/nsrs/doc.html

## 📊 系统监控

### 健康检查
- 端点: `/nsrs/actuator/health`
- 支持存活探针和就绪探针
- 自动故障恢复

### 日志配置
- 业务日志级别: DEBUG
- 框架日志级别: WARN
- 支持日志聚合和分析

## 🔒 安全特性

- 内置权限系统
- 操作审计日志
- 数据访问控制
- 分布式锁防并发

## 📝 开发规范

- 代码规范: 遵循阿里巴巴Java开发手册
- 提交规范: 使用Conventional Commits
- API设计: RESTful风格
- 数据库设计: 遵循三范式

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交变更
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

Copyright © 2025 号卡资源管理系统

---

**号卡资源管理系统** - 专业的网络SIM卡资源管理解决方案

