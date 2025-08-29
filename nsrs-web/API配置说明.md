# API配置说明

## 概述

前端项目已支持动态API地址配置，可以根据不同环境自动适配后端服务地址，解决部署到服务器后无法访问后端API的问题。

## 配置方式

### 1. 环境变量配置（推荐）

在项目根目录创建环境变量文件：

#### 开发环境 (`.env` 或 `.env.development`)
```bash
VITE_API_BASE_URL=http://localhost:8088/nsrs
VITE_APP_ENV=development
```

#### 生产环境 (`.env.production`)
```bash
# 方式1：使用相对路径（推荐）
VITE_API_BASE_URL=/nsrs
VITE_APP_ENV=production

# 方式2：使用完整URL
# VITE_API_BASE_URL=https://your-domain.com/nsrs
```

#### 本地覆盖配置 (`.env.local`)
```bash
# 本地开发时的特殊配置，不会被提交到版本控制
VITE_API_BASE_URL=http://192.168.1.100:8088/nsrs
```

### 2. 自动适配机制

如果没有配置环境变量，系统会自动根据当前环境进行适配：

- **开发环境**：自动使用 `http://localhost:8088/nsrs`
- **生产环境**：自动使用当前域名 + `/nsrs` 路径

## 部署配置

### 1. 同域部署（推荐）

当前端和后端部署在同一个域名下时：

```bash
# .env.production
VITE_API_BASE_URL=/nsrs
```

访问示例：
- 前端：`https://your-domain.com`
- 后端API：`https://your-domain.com/nsrs`

### 2. 跨域部署

当前端和后端部署在不同域名时：

```bash
# .env.production
VITE_API_BASE_URL=https://api.your-domain.com/nsrs
```

访问示例：
- 前端：`https://web.your-domain.com`
- 后端API：`https://api.your-domain.com/nsrs`

### 3. IP地址部署

直接使用IP地址部署时：

```bash
# .env.production
VITE_API_BASE_URL=http://192.168.1.100:8088/nsrs
```

## 构建和部署

### 1. 开发环境启动

```bash
npm run dev
```

开发环境会自动使用代理配置，避免跨域问题。

### 2. 生产环境构建

```bash
# 使用生产环境配置构建
npm run build

# 或者指定特定环境
npm run build --mode production
```

### 3. 预览构建结果

```bash
npm run preview
```

## 配置文件优先级

环境变量文件的加载优先级（从高到低）：

1. `.env.local` - 本地配置（不会被提交）
2. `.env.[mode]` - 特定环境配置
3. `.env` - 通用配置

## 常见问题

### Q1: 部署后前端无法访问后端API

**原因**：使用了硬编码的localhost地址

**解决**：
1. 检查 `.env.production` 文件是否正确配置
2. 确保API地址与实际部署地址匹配
3. 检查网络连通性和防火墙设置

### Q2: 开发环境跨域问题

**解决**：项目已配置Vite代理，开发环境会自动代理API请求到后端服务。

### Q3: 如何在运行时动态修改API地址

可以通过修改 `src/utils/config.ts` 文件中的 `getApiBaseUrl` 函数来实现更复杂的逻辑。

## 技术实现

### 核心文件

- `src/utils/config.ts` - 配置工具类
- `src/utils/http.ts` - HTTP请求封装
- `src/utils/request.ts` - 请求工具
- `vite.config.ts` - Vite配置（开发代理）

### 配置函数

```typescript
// 获取API基础URL
const apiBaseUrl = getApiBaseUrl()

// 获取完整API URL
const fullUrl = getApiUrl('/msisdn/number/page')
```

## 注意事项

1. **环境变量必须以 `VITE_` 开头**才能在前端代码中访问
2. **生产环境配置文件**会在构建时被打包，不能在运行时修改
3. **敏感信息**不要放在前端环境变量中
4. **相对路径配置**更适合容器化部署
5. **记得重启开发服务器**使环境变量配置生效