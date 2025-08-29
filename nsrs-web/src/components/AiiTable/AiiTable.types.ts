import React from 'react'
import type { TableProps } from 'antd'

export interface ToolbarProps {
    icon: React.ReactNode
    label: string
    onClick: () => void
}

export interface OperationColumnProps {
    title: string
    key: string
    render: (record: any) => React.ReactNode
    fixed?: 'left' | 'right'
    width?: number | string
}

export interface OperationItemProps {
    key: string
    icon?: React.ReactNode
    label: string | React.ReactNode
    danger?: boolean
    [key: `data-${string}`]: string | number
}

export interface AiiTableProps<T> extends TableProps<T> {
    pagination: {
        total: number
        current: number
        pageSize: number
    }
    toolbar?: ToolbarProps[]
    operations?:
        | ('EDIT' | 'DELETE' | 'COPY' | 'DETAIL' | { key: string; icon?: React.ReactNode; label: string })[]
        | ((
              record: T,
          ) => ('EDIT' | 'DELETE' | 'COPY' | 'DETAIL' | { key: string; icon?: React.ReactNode; label: string })[])
    onPageSizeChange: (pageSize: number) => void
    onPageChange: (page: number) => void
    onBatchDelete?: () => void
    onBatchExport?: () => void
    onOperationClick?: (key: string, record: any) => void
}

export interface BatchOperationRowProps {
    selectedCount: number
    columnsLength: number
    onDeselect: () => void
    onBatchDelete?: () => void
    onBatchExport?: () => void
    t: (key: string) => string
}
