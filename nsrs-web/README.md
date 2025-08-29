<div align="center">
  <img src="./public/favicon.png" alt="AII Logo" width="100" />
  <h1>AII Admin CLI</h1>
</div>

#### Description

`AII Admin CLI` is a React-based admin dashboard project that supports internationalization (i18n), theme switching (light/dark mode), and aims to provide a modern, extensible solution for management systems.

---

### Table of Contents

1. [Features](#features)
2. [Installation](#installation)
3. [Components](#components)
    - [AiiTable](#aiitable)
    - [AiiSearch](#aiisearch)
    - [AiiDrawer](#aiidrawer)
    - [AiiTab](#aiitab)
    - [AppProvider](#appprovider)
4. [API Integration](#api-integration)
    - [Using openapi-fetch with table hooks](#using-openapi-fetch-with-table-hooks)
5. [State Management](#state-management)
6. [Menu Format](#menu-format)
7. [Contributing](#contributing)
8. [License](#license)

---

### Features

- **Internationalization (i18n)**: Powered by `react-i18next`, supports dynamic language switching.
- **Theme Switching**: Customizable themes using Ant Design, supports light and dark modes.
- **Reusable Components**: Provides highly customizable components like tables, search forms, and drawers.
- **Global State Management**: Efficient state management using Zustand.
- **Dynamic Route Generation**: Dynamically generates routes based on the menu structure, with support for lazy-loaded components.
- **AI Chat Integration**: Includes an AI-powered chat component with support for OpenAI's GPT model, featuring real-time streaming, markdown rendering, and code block support.

---

### Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/Aii-Group/AII-Admin-Cli.git
    ```

2. Navigate to the project directory:

    ```bash
    cd aii-admin-cli
    ```

3. Install dependencies:

    ```bash
    pnpm install
    ```

4. Start the development server:

    ```bash
    pnpm dev
    ```

5. Open your browser and visit:
    ```
    http://localhost:3001
    ```

---

### Components

#### AiiTable

`AiiTable` is a highly customizable table component based on Ant Design, supporting batch operations, custom toolbars, and dynamic column rendering.

**Key Features**:

- **Dynamic Columns**: Allows dynamic definition of table columns.
- **Batch Operations**: Supports batch deletion, export, and more.
- **Pagination**: Built-in pagination with customizable page size.
- **Toolbar**: Add custom buttons above the table.
- **Row Selection**: Enables row selection for batch operations.
- **Action Buttons**: Add action buttons (e.g., edit, delete) for each row.

**Usage Example**:

```tsx
import React from 'react'
import AiiTable from '@/components/AiiTable'

const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Age', dataIndex: 'age', key: 'age' },
    { title: 'Address', dataIndex: 'address', key: 'address' },
]

const dataSource = [
    { key: '1', name: 'John Doe', age: 32, address: 'New York' },
    { key: '2', name: 'Jane Smith', age: 28, address: 'London' },
]

const App = () => (
    <AiiTable
        rowKey="key"
        columns={columns}
        dataSource={dataSource}
        pagination={{ current: 1, pageSize: 10, total: 50 }}
        toolbar={[{ label: 'Add', icon: <PlusOutlined />, onClick: () => console.log('Add clicked') }]}
        operations={['DETAIL', 'EDIT', 'DELETE']}
    />
)

export default App
```

---

#### AiiSearch

`AiiSearch` is a flexible search form component that supports dynamic form items, collapsible layouts, and search/reset actions.

**Key Features**:

- **Dynamic Form Items**: Render form fields dynamically using an array.
- **Collapsible Layout**: Supports collapsible/expandable layout for many fields.
- **Search and Reset**: Built-in search and reset buttons.

**Usage Example**:

```tsx
import React from 'react'
import { Form, Input } from 'antd'
import AiiSearch from '@/components/AiiSearch'

const formItems = [
    <Form.Item name="name" label="Name" key="name">
        <Input placeholder="Enter name" />
    </Form.Item>,
    <Form.Item name="age" label="Age" key="age">
        <Input placeholder="Enter age" />
    </Form.Item>,
]

const App = () => <AiiSearch items={formItems} onSearch={(values) => console.log('Search values:', values)} />

export default App
```

---

#### AiiDrawer

`AiiDrawer` is a global drawer component based on Ant Design's `Drawer`, supporting dynamic content and global management.

**Key Features**:

- **Global Management**: Manage drawer visibility through context.
- **Dynamic Content**: Pass dynamic content to the drawer.
- **Customizable Props**: Supports all Ant Design `Drawer` props.

**Usage Example**:

1. **Wrap your app with `DrawerProvider`**:

```tsx
import React from 'react'
import { DrawerProvider } from '@/components/AiiDrawer'

const App = () => (
    <DrawerProvider>
        <YourApp />
    </DrawerProvider>
)

export default App
```

2. **Control the drawer using `useDrawer`**:

```tsx
import React from 'react'
import { Button } from 'antd'
import { useDrawer } from '@/components/AiiDrawer'

const ExampleComponent = () => {
    const { showDrawer, closeDrawer } = useDrawer()

    const handleOpenDrawer = () => {
        showDrawer(
            <div>
                <h3>Drawer Content</h3>
                <p>This is some content inside the drawer.</p>
                <Button onClick={closeDrawer}>Close Drawer</Button>
            </div>,
            { title: 'Custom Drawer Title', width: 500 },
        )
    }

    return <Button onClick={handleOpenDrawer}>Open Drawer</Button>
}

export default ExampleComponent
```

---

### API Integration

#### Using openapi-fetch with table hooks

This project uses `openapi-fetch` for API calls, which can be integrated with the `useTable` hook for data fetching in tables.

**Key Features**:

- **Type Safety**: Full type safety with auto-generated types from OpenAPI specification.
- **Middleware Support**: Built-in middleware for authentication, logging, etc.
- **Integration with useTable**: Easy integration with the `useTable` hook for paginated data.

**Usage Example**:

1. **Create an adapter function**:

First, we need to create an adapter to convert `openapi-fetch` responses to the format expected by the `useTable` hook:

```ts
// src/utils/fetch/adapter.ts
import { client } from './index'
import { ResultData, ResPage } from '@/utils/http/interface'
import { message } from 'antd'

interface TableResponse<T> {
    list: T[]
    total: number
}

export async function fetchTableData<T>(
    path: string,
    method: 'get' | 'post' | 'put' | 'delete',
    params: Record<string, any>,
): Promise<ResultData<TableResponse<T>>> {
    try {
        let response: any

        switch (method) {
            case 'get':
                response = await client.GET(path as any, { params: { query: params } })
                break
            case 'post':
                response = await client.POST(path as any, { body: params })
                break
            case 'put':
                response = await client.PUT(path as any, { body: params })
                break
            case 'delete':
                response = await client.DELETE(path as any, { params: { query: params } })
                break
            default:
                throw new Error(`Unsupported method: ${method}`)
        }

        if (response.error) {
            message.error(response.error.message || '请求失败')
            throw new Error(response.error.message || '请求失败')
        }

        const data = response.data as ResPage<T>

        return {
            code: '200',
            msg: 'success',
            success: true,
            data: {
                list: data.datalist,
                total: data.total,
            },
        }
    } catch (error: any) {
        message.error(error.message || '请求失败')
        throw error
    }
}
```

2. **Use the adapter with the table hook**:

```tsx
// src/hooks/table-example.ts
import useTable from './table.hooks'
import { fetchTableData } from '@/utils/fetch/adapter'

interface SimCard {
    id: number
    iccId: string
    imsi: string
    status: string
}

export function useSimCardTable() {
    const apiFunction = (params: Record<string, any>) => {
        return fetchTableData<SimCard>('/simcard/list', 'get', params)
    }

    return useTable<SimCard>(apiFunction)
}
```

3. **Use the custom hook in your component**:

```tsx
import React from 'react'
import AiiTable from '@/components/AiiTable'
import { useSimCardTable } from '@/hooks/table-example'

const SimCardTable = () => {
    const { dataSource, pagination, loading, queryTableData } = useSimCardTable()

    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id' },
        { title: 'ICC ID', dataIndex: 'iccId', key: 'iccId' },
        { title: 'IMSI', dataIndex: 'imsi', key: 'imsi' },
        { title: 'Status', dataIndex: 'status', key: 'status' },
    ]

    React.useEffect(() => {
        queryTableData()
    }, [])

    return (
        <AiiTable
            rowKey="id"
            columns={columns}
            dataSource={dataSource}
            loading={loading}
            pagination={{
                current: pagination.current,
                pageSize: pagination.pageSize,
                total: pagination.total,
                onChange: (page, pageSize) => {
                    queryTableData({ current: page, pageSize })
                },
            }}
        />
    )
}

export default SimCardTable
```

---

#### AppProvider

`AppProvider` is a global context provider that integrates Ant Design's `message`, `Modal`, and `notification` APIs for global usage.

**Key Features**:

- **Global Message Notifications**: Call `message` API via `window.$message`.
- **Global Modals**: Call `Modal` API via `window.$modal`.
- **Global Notifications**: Call `notification` API via `window.$notification`.

**Usage Example**:

1. **Wrap your app with `AppProvider`**:

```tsx
import React from 'react'
import ReactDOM from 'react-dom'
import AppProvider from '@/components/AppProvider'
import App from './App'

ReactDOM.render(
    <AppProvider>
        <App />
    </AppProvider>,
    document.getElementById('root'),
)
```

2. **Call APIs globally**:

```tsx
// Show a success message
window.$message.success('This is a success message!')

// Show a confirmation modal
window.$modal.confirm({
    title: 'Confirm Action',
    content: 'Are you sure you want to proceed?',
    onOk: () => console.log('Confirmed'),
})

// Show a notification
window.$notification.info({
    message: 'Notification Title',
    description: 'This is the content of the notification.',
})
```

---

### State Management

This project uses Zustand for state management. We have several predefined stores for different purposes:

1. **User Store**: Manages user authentication and information.
2. **Menu Store**: Manages application menu structure.
3. **Theme Store**: Manages application theme (light/dark mode).
4. **Tab Store**: Manages opened tabs in the application.
5. **Language Store**: Manages application language.
6. **Fullscreen Store**: Manages fullscreen state.
7. **Tab Content Store**: Manages content of tabs to preserve state when switching between tabs.

All stores support local storage persistence, ensuring that the application state is preserved even after page refresh.

You can create your own stores following the same pattern as these existing stores.

---

#### AiiTab

`AiiTab` is a tab component that supports smooth animations and state preservation when switching between tabs. It uses Zustand for state management to ensure tab content is preserved when switching.

**Key Features**:

- **Smooth Animations**: Uses Framer Motion for smooth tab transitions.
- **State Preservation**: Uses Zustand to preserve tab content state when switching.
- **Dynamic Tabs**: Supports dynamic tab creation and management.

**Usage Example**:

```tsx
import React from 'react'
import AiiTab from '@/components/AiiTab'

const tabs = [
    { key: 'tab1', label: 'Tab 1', content: <div>Content for Tab 1</div> },
    { key: 'tab2', label: 'Tab 2', content: <div>Content for Tab 2</div> },
    { key: 'tab3', label: 'Tab 3', content: <div>Content for Tab 3</div> },
]

const App = () => <AiiTab tabs={tabs} />

export default App
```

**Props**:

- `tabs`: Array of tab objects, each with `key`, `label`, and `content` properties.
- `onTabClick`: Optional callback function triggered when a tab is clicked.

---

### Menu Format

The menu format is an array of objects, where each object represents a menu item. Each menu item can have the following properties:

- `key`: Unique key for the menu item.
- `label`: Display label for the menu item.
- `icon`: Icon for the menu item.
- `path`: Path for the menu item.
- `children`: Array of child menu items.

**Example**:

```tsx
const menu = [
    {
        key: 'Table',
        label: 'Table',
        icon: 'table',
        path: '/table',
        children: [
            {
                key: 'Basic_Table',
                label: 'Basic Table',
                path: '/table/basic',
                filePath: '/table/basic',
            },
            {
                key: 'Advanced_Table',
                label: 'Advanced Table',
                path: '/table/advanced',
                filePath: '/table/advanced',
            },
        ],
    },
]
```

---

### Contributing

We welcome contributions! Please refer to the [Contributing Guide](CONTRIBUTING.md) to get started.

---

### License

This project is licensed under the [MIT License](LICENSE).
