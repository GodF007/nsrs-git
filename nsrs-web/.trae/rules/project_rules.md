# AII-Admin-Cli 项目规范

## 1. 项目概述

本项目是一个基于 React、TypeScript 和 Vite 的管理系统前端框架，使用 Ant Design 和 Tailwind CSS 进行 UI 开发，采用 TanStack Router 进行路由管理，Zustand 进行状态管理。

## 2. 开发环境

- Node.js: v18+ (建议使用 .nvmrc 中指定的版本)
- 包管理器: pnpm
- 代码编辑器: VS Code (建议安装推荐插件)

## 3. 代码规范

### 3.0 依赖管理

- 优先使用项目已声明的依赖包（参见 `package.json`），避免重复引入功能相似的库
- 安装新依赖前需确认现有依赖无法满足需求
- 依赖版本应指定具体版本号，避免使用 `^` 或 `~` 前缀

### 3.1 TypeScript

- 使用严格模式 (`strict: true`)
- 所有组件和函数必须定义明确的类型
- 避免使用 `any` 类型，必要时使用 `unknown` 并进行类型守卫

### 3.2 ESLint

- 遵循项目根目录下 `.eslintrc.js` 配置
- 禁止使用 `console.log` (生产环境)
- React Hooks 必须遵循规则 (`react-hooks/rules-of-hooks`)
- 组件必须使用函数式组件和 TypeScript

### 3.3 代码格式化

- 使用 Prettier 进行代码格式化
- 提交代码时会自动运行格式化检查
- 规则遵循项目根目录下 `.prettierrc` 配置

## 4. 提交规范

### 4.1 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 4.2 提交类型 (type)

- `feat`: 新功能
- `update`: 功能更新
- `fixbug`: 修复 bug
- `refactor`: 代码重构
- `optimize`: 性能优化
- `style`: 样式修改
- `docs`: 文档更新
- `chore`: 构建/依赖/工具相关改动

### 4.3 提交验证

- 使用 Husky 进行提交前验证
- `pre-commit`: 自动格式化代码并添加更改
- `commit-msg`: 验证提交信息格式

## 5. 目录结构

```
/src
  /api          # API 请求
  /assets       # 静态资源
  /components   # 共享组件
  /hooks        # 自定义 Hooks
  /layouts      # 布局组件
  /locales      # 国际化
  /routes       # 路由页面
  /stores       # 状态管理
  /styles       # 全局样式
  /interface    # 接口类型定义
  /types        # 类型定义
  /utils        # 工具函数
```

## 6. 开发流程

### 6.1 安装依赖

```bash
pnpm install
```

### 6.2 开发环境

```bash
pnpm dev
```

### 6.3 构建生产版本

```bash
pnpm build
```

### 6.4 代码检查

```bash
pnpm lint
```

### 6.5 格式化代码

```bash
pnpm format
```

## 7. 组件规范

- 优先使用 `/src/components` 目录下的共享组件，避免重复开发
- 组件命名使用 PascalCase 格式，如 `AiiTable`
- 组件必须包含类型定义文件 `.types.ts`

## 8. Hooks 规范

- 优先使用 `/src/hooks` 目录下的自定义 Hooks
- Hooks 命名以 `use` 为前缀，如 `useTable`
- 每个 Hook 功能应单一，遵循单一职责原则

## 9. 样式规范

- 使用 Tailwind CSS 进行样式开发
- 遵循 tailwind.config.js 中的自定义配置
- 全局样式放在 `/src/styles/global.css`
- 组件样式优先使用 Tailwind 工具类

## 8. 路由管理

- **优先使用文件路由**，路由结构与 `/src/routes` 目录结构保持一致
- 参考文档: <mcurl name="TanStack Router 官方文档" url="https://tanstack.com/router/latest/docs/framework/react/overview"></mcurl>
- 路由生成文件 `routeTree.gen.ts` 由工具自动生成，无需手动修改
- 权限路由通过 `_authentication` 布局实现

### 8.1 文件路由命名规则

1. **页面文件**：
    - 使用 `.tsx` 扩展名
    - 文件名即为路由路径，如 `login.tsx` 对应 `/login` 路由
    - 特殊文件名：`__root.tsx` 作为根路由组件

2. **动态路由**：
    - 使用 `$` 前缀定义动态参数，如 `$userId.tsx` 对应 `/users/:userId`
    - 多参数路由：`$userId.$postId.tsx` 对应 `/users/:userId/:postId`

3. **布局路由**：
    - 使用下划线 `_` 前缀定义布局组件，如 `_authentication.tsx`
    - 布局目录：`_authentication/` 目录下的文件共享该布局

4. **嵌套路由**：
    - 通过目录结构实现，如 `/routes/dashboard/profile.tsx` 对应 `/dashboard/profile`
    - 目录内的 `index.tsx` 对应目录路由的默认页面

5. **路由分组**：
    - 使用括号 `()` 创建逻辑分组，不影响路由路径，如 `(auth)/login.tsx`

6. **私有路由**：
    - 在布局文件中实现权限控制，通过 `redirect` 函数处理未授权访问

### 8.2 路由示例结构

```
/src/routes
  __root.tsx                # 根路由
  _authentication.tsx       # 认证布局
  _authentication/
    dashboard.tsx           # 需认证的仪表盘页面
  login.tsx                 # 登录页
  users/
    index.tsx               # 用户列表页 (/users)
    $userId.tsx             # 用户详情页 (/users/:userId)
  (settings)/
    profile.tsx             # 个人资料页 (/profile)
```

## 9. 状态管理

- 使用 Zustand 进行状态管理
- 状态定义在 `/src/stores` 目录
- 全局状态和局部状态分离
- **实际项目中的状态字段定义优先于预设**，以实际开发需求为准。例如当前用户状态包含：`userId`、`userName`、`token` 和 `permissions` 字段

## 10. HTTP 请求规范

### 10.1 服务使用

- **优先使用封装的 HTTP 服务**，通过 `commonService` 实例调用 API，禁止直接使用 axios
- 导入方式：`import commonService from '@/utils/http'`
- 基础配置已包含：baseURL 为 `/`，超时时间 10000ms，自动携带 credentials

### 10.2 请求方法

- 使用封装的请求方法：`get`/`post`/`put`/`delete`/`request`
- 参数传递规范：

    ```typescript
    // GET 请求
    commonService.get<T>('/api/user', { id: 1 })

    // POST 请求
    commonService.post<T>('/api/user', { name: 'test' })
    ```

- 所有请求必须指定返回类型 `T`，确保类型安全

### 10.3 拦截器处理

- **禁止修改默认拦截器逻辑**，如需扩展创建新的服务类继承 `HttpService`
- 请求拦截器自动处理：
    - 添加 `token` 请求头（从用户状态获取）
    - 启动 NProgress 进度条
    - 取消重复请求
- 响应拦截器自动处理：
    - 关闭 NProgress 进度条
    - 移除已完成请求的取消令牌
    - 统一错误提示（通过 antd message）

### 10.4 请求取消机制

- 系统自动取消重复请求（相同 URL、方法和参数）
- 如需允许重复请求，在请求配置中添加 `ignoreCancel: true`
- 原理：使用 `AbortController` 实现，具体参见 `AxiosCanceler` 类

### 10.5 错误处理

- HTTP 状态码处理：通过 `checkStatus` 函数统一处理（401/403/404 等状态）
- 业务错误码处理：
    - 成功：`code === ResultEnum.SUCCESS` (默认 200)
    - 登录失效：`code === ResultEnum.OVERDUE` (599)，自动处理登出逻辑
    - 其他错误：自动显示 `data.msg` 错误信息

### 10.6 类型定义

- 必须使用已定义的响应类型：
    ```typescript
    // 基础响应
    interface Result {
        code: string
        msg: string
        success: boolean
    }
    // 带数据响应
    interface ResultData<T = any> extends Result {
        data?: T
    }
    // 分页响应
    interface ResPage<T> {
        datalist: T[]
        pageNum: number
        pageSize: number
        total: number
    }
    ```
- 请求参数建议使用接口定义，确保类型校验

### 10.7 特殊场景处理

- 文件上传/下载：使用 `request` 方法并指定 `responseType: 'blob'`
- Token 刷新：通过 `RefresherService` 处理，具体场景参见类实现
- 微应用环境：自动禁用 credentials（通过 `isMicroAppEnv` 判断）

## 11. 国际化

- 使用 i18next 和 react-i18next
- 翻译文件放在 `/src/locales` 目录
- 支持中英文切换

## 12. 错误处理

- API 错误使用统一的错误处理机制
- 页面错误使用 403/404 等错误页面
- 组件错误使用 Error Boundary

## 13. 性能优化

- 组件懒加载
- 图片优化
- 避免不必要的重渲染
- 使用 React.memo 和 useMemo/useCallback

## 14. 安全规范

- 避免在代码中硬编码敏感信息
- 使用环境变量存储配置
- API 请求添加认证和授权检查

## 15. 测试规范

- 组件测试使用 React Testing Library
- API 测试使用 Mock Service Worker
- 关键功能编写单元测试

## 16. 部署规范

- 使用 CI/CD 流程自动部署
- 构建产物放在 `/dist` 目录
- 生产环境使用 HTTPS

---

本规范会随着项目发展不断更新，请定期查阅最新版本。如有疑问或建议，请联系项目负责人。
