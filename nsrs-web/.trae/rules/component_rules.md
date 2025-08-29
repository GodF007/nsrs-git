# AII-Admin-Cli 组件使用规范

## 1. 通用组件规范

### 1.1 组件导入

- 使用绝对路径从 `@/components` 导入公共组件
- 导入时使用大驼峰命名，如：`import AiiTable from '@/components/AiiTable'`

### 1.2 Props 传递

- 所有组件 Props 必须定义 TypeScript 类型
- 非必传 Props 必须设置默认值
- 使用解构赋值明确声明所需 Props

### 1.3 状态管理

- 组件内部状态使用 `useState` 或 `useReducer`
- 共享状态通过 Zustand Store 或 Context API 传递
- 避免在组件中直接修改 props

### 1.4 样式规范

- 组件样式优先使用 Tailwind CSS 工具类
- 复杂样式可使用 CSS Modules
- 全局样式放在 `/src/styles/global.css`

## 2. 组件使用规则

### 2.1 AiiChat 组件

#### 2.1.1 组件用途

AI 对话交互组件，支持 Markdown 渲染、代码高亮和思考过程展示

#### 2.1.2 Props 说明

无外部 Props，通过内部状态管理对话流程

#### 2.1.3 基本用法

```tsx
import AiiChat from '@/components/AiiChat'

function ChatPage() {
    return (
        <div className="chat-container">
            <AiiChat />
        </div>
    )
}
```

#### 2.1.4 注意事项

- 需要配置 AI 模型环境变量（`VITE_ALIYUN_AI_URL`、`VITE_ALIYUN_AI_KEY` 等）
- 支持代码块复制功能，自动适配深色/浅色主题
- 思考过程可展开/折叠，支持中断当前思考

### 2.2 AiiDrawer 组件

#### 2.2.1 组件用途

基于 Ant Design Drawer 的增强抽屉组件，支持上下文调用

#### 2.2.2 Props 说明

```typescript
interface AiiDrawerProps extends DrawerProps {
    // 继承 Ant Design Drawer 的所有 Props
}
```

#### 2.2.3 基本用法

```tsx
// 1. 提供 Drawer 上下文
import { DrawerProvider } from '@/components/AiiDrawer'

function App() {
    return <DrawerProvider>{/* 应用内容 */}</DrawerProvider>
}

// 2. 在组件中使用
import { useDrawer } from '@/components/AiiDrawer'

function MyComponent() {
    const { showDrawer } = useDrawer()

    const handleOpenDrawer = () => {
        showDrawer(<div>抽屉内容</div>, { title: '示例抽屉', width: 500 })
    }

    return <Button onClick={handleOpenDrawer}>打开抽屉</Button>
}
```

#### 2.2.4 注意事项

- 必须在 `DrawerProvider` 中使用 `useDrawer` 钩子
- 支持 Ant Design Drawer 的所有配置项
- 通过 `showDrawer` 方法动态创建抽屉

### 2.3 AiiSearch 组件

#### 2.3.1 组件用途

高级搜索表单组件，支持字段展开/折叠

#### 2.3.2 Props 说明

```typescript
interface AiiSearchProps {
    items: React.ReactElement<typeof Form.Item>[] // 搜索字段
    cols?: number // 列数
    onSearch?: (value: any) => void // 搜索回调
}
```

#### 2.3.3 基本用法

```tsx
import AiiSearch from '@/components/AiiSearch'
import { Form, Input, Select } from 'antd'

function MySearch() {
    const handleSearch = (values: any) => {
        console.log('搜索参数:', values)
    }

    return (
        <AiiSearch onSearch={handleSearch}>
            <Form.Item name="name" label="名称">
                <Input />
            </Form.Item>
            <Form.Item name="status" label="状态">
                <Select
                    options={[
                        { label: '启用', value: '1' },
                        { label: '禁用', value: '0' },
                    ]}
                />
            </Form.Item>
            {/* 更多搜索字段 */}
        </AiiSearch>
    )
}
```

#### 2.3.4 注意事项

- 超过4个字段自动显示展开/折叠按钮
- 内置搜索和重置功能
- 使用 Ant Design Form.Item 作为子元素

### 2.4 AiiTable 组件

#### 2.4.1 组件用途

增强型表格组件，支持分页、批量操作和自定义操作列

#### 2.4.2 Props 说明

```typescript
interface AiiTableProps<T> extends TableProps<T> {
    pagination: {
        total: number
        current: number
        pageSize: number
    }
    toolbar?: ToolbarProps[] // 工具栏配置
    operations?: Array<'EDIT' | 'DELETE' | 'COPY' | 'DETAIL' | { key: string; icon?: ReactNode; label: string }>
    onPageSizeChange: (pageSize: number) => void
    onPageChange: (page: number) => void
    onBatchDelete?: () => void
    onBatchExport?: () => void
    onOperationClick?: (key: string, record: any) => void
}
```

#### 2.4.3 基本用法

推荐结合 `useTable` 钩子使用，简化数据管理：

```tsx
import AiiTable from '@/components/AiiTable'
import useTable from '@/hooks/table.hooks'
import { fetchDataApi } from '@/api/data'

function MyTable() {
  const columns = [
    { title: '名称', dataIndex: 'name', key: 'name' },
    { title: '状态', dataIndex: 'status', key: 'status' },
    // 更多列定义
  ]

  // 使用useTable钩子管理表格数据
  const { loading, dataSource, pagination, onPageChange, onPageSizeChange, onSelectChange, selectedRows } = useTable(fetchDataApi)

  const handleOperation = (key: string, record: any) => {
    switch(key) {
      case 'EDIT':
        // 编辑操作
        break
      case 'DELETE':
        // 删除操作
        break
      // 其他操作
    }
  }

  return (
    <AiiTable
      columns={columns}
      dataSource={dataSource}
      loading={loading}
      pagination={pagination}
      {/* 字符串数组形式 */}
      operations={['EDIT', 'DELETE', 'DETAIL']}

      {/* 自定义操作项示例 */}
      operations={(record) => [
        { key: 'VIEW', label: '查看', icon: <EyeOutlined /> },
        { key: 'EDIT', label: '编辑', icon: <EditOutlined /> },
        { key: 'DELETE', label: '删除', icon: <DeleteOutlined />, danger: true },
        ...(record.status === 'active' ? [{ key: 'DISABLE', label: '禁用', danger: true }] : [])
      ]}
      rowSelection={{ selectedRowKeys: selectedRows.map(row => row.id), onChange: onSelectChange }}
      onPageChange={onPageChange}
      onPageSizeChange={onPageSizeChange}
      onOperationClick={handleOperation}
    />
  )
}
```

#### 2.4.4 注意事项

- 支持内置操作：编辑(EDIT)、删除(DELETE)、复制(COPY)、详情(DETAIL)
- 可自定义操作按钮和回调
- 支持批量删除和导出功能
- 推荐使用 `useTable` 钩子管理分页状态和数据加载
- 分页参数通过 `useTable` 钩子自动同步，无需手动维护
- 选中行状态由 `useTable` 钩子统一管理，确保状态一致性

### 2.5 AppProvider 组件

#### 2.5.1 组件用途

全局状态提供者，提供消息、模态框和通知的全局访问

#### 2.5.2 基本用法

```tsx
import AppProvider from '@/components/AppProvider'
import { ConfigProvider } from 'antd'

function App() {
    return (
        <ConfigProvider>
            <AppProvider>{/* 应用内容 */}</AppProvider>
        </ConfigProvider>
    )
}
```

#### 2.5.3 使用全局 API

```tsx
// 在任何组件中
window.$message.success('操作成功')
window.$modal.confirm({
    title: '确认',
    content: '确定要删除吗？',
})
window.$notification.info({
    message: '通知',
    description: '这是一条通知',
})
```

#### 2.5.4 注意事项

- 应在应用最顶层使用
- 依赖 Ant Design 的 message、modal、notification 组件
- 提供全局访问点，避免在组件中重复创建

### 2.6 SvgIcon 组件

#### 2.6.1 组件用途

动态加载 SVG 图标组件

#### 2.6.2 Props 说明

```typescript
interface SvgIconProps extends React.SVGAttributes<SVGSVGElement> {
    icon: string // 图标名称（assets/svg目录下的SVG文件名）
}
```

#### 2.6.3 基本用法

```tsx
import SvgIcon from '@/components/SvgIcon'

function MyComponent() {
    return (
        <div>
            <SvgIcon icon="robot" width={24} height={24} />
            <SvgIcon icon="user" className="text-primary" />
        </div>
    )
}
```

#### 2.6.4 注意事项

- 图标文件必须放在 `/src/assets/svg/` 目录下
- 导入时不需要 `.svg` 扩展名
- 支持所有 SVG 元素属性（width、height、className 等）
- 图标加载失败时会显示空白

## 3. 组件开发规范

### 3.1 组件设计原则

- 单一职责：每个组件只做一件事
- 可复用性：设计时考虑多种使用场景
- 可测试性：组件逻辑清晰，便于单元测试
- 性能优化：避免不必要的渲染和计算

### 3.2 文件组织

- 每个组件一个目录，目录名使用大驼峰
- 组件入口文件为 `index.tsx`
- 类型定义放在 `[组件名].types.ts`
- 样式文件为 `index.css` 或 `index.module.css`

### 3.3 命名规范

- 组件名：大驼峰，如 `AiiTable`
- Props 接口：组件名+Props，如 `AiiTableProps`
- 类型文件：组件名+.types.ts，如 `AiiTable.types.ts`

## 4. 组件测试规范

- 关键组件编写单元测试
- 使用 React Testing Library
- 测试覆盖主要功能点和边界情况
- 测试文件放在组件目录下的 `__tests__` 文件夹

---

本规范会随着组件库扩展不断更新，请开发人员定期查阅。组件使用中有任何问题或建议，请联系前端团队负责人。
