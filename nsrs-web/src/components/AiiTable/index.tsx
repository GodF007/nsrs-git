import { memo, useEffect, useMemo, useState } from 'react'

import { useTranslation } from 'react-i18next'
import type { MenuProps, TableProps } from 'antd'
import { Button, Divider, Dropdown, Pagination, Space, Table } from 'antd'

import { Copy, Delete, DocDetail, Down, DownloadFour, FileEditingOne, MoreOne } from '@icon-park/react'

import type { AiiTableProps, BatchOperationRowProps, OperationColumnProps, OperationItemProps } from './AiiTable.types'

import './index.css'

const pageSizeOptions: System.Enum[] = [
    {
        key: '10',
        label: '10',
    },
    {
        key: '20',
        label: '20',
    },
    {
        key: '50',
        label: '50',
    },
    {
        key: '100',
        label: '100',
    },
]

const thStyle = {
    paddingTop: 10,
    paddingBottom: 10,
    paddingLeft: 0,
    paddingRight: 0,
    zIndex: 1,
}

const AiiTable = <T extends unknown>(props: AiiTableProps<T>): React.ReactElement => {
    const { t } = useTranslation()

    const {
        rowKey,
        loading,
        columns,
        dataSource,
        pagination,
        toolbar,
        operations,
        onPageSizeChange,
        onPageChange,
        rowSelection,
        onBatchDelete,
        onBatchExport,
        onOperationClick,
    } = props

    const defaultOperationMenu: OperationItemProps[] = [
        {
            key: 'DETAIL',
            icon: <DocDetail />,
            label: t('Action.Detail'),
        },
        {
            key: 'EDIT',
            icon: <FileEditingOne />,
            label: t('Action.Edit'),
        },
        {
            key: 'COPY',
            icon: <Copy />,
            label: t('Action.Copy'),
        },
        {
            key: 'DELETE',
            icon: <Delete />,
            label: t('Action.Delete'),
            danger: true,
        },
    ]

    const [tableColumns, setTableColumns] = useState<TableProps<T>['columns']>([])

    const onMenuClick = (record: any) => {
        const onClick: MenuProps['onClick'] = ({ key }) => {
            onOperationClick && onOperationClick(key, record)
        }
        return onClick
    }

    const operationColumn: OperationColumnProps = {
        title: t('Action.Operate'),
        key: 'operation',
        render: (record: any) => {
            let menu: OperationItemProps[] = []
            if (typeof operations === 'function') {
                menu = (operations as any)(record)
                    .map((op: any) =>
                        typeof op === 'string'
                            ? defaultOperationMenu?.find((item) => item?.key === op.toUpperCase())
                            : { key: op.key, icon: op.icon, label: op.label, danger: op.danger },
                    )
                    .filter((item: any) => !!item)
            } else if (Array.isArray(operations)) {
                menu = operations
                    .map((op) =>
                        typeof op === 'string'
                            ? defaultOperationMenu?.find((item) => item?.key === op.toUpperCase())
                            : { key: op.key, icon: op.icon, label: op.label },
                    )
                    .filter((item) => !!item)
            }
            if (!menu || menu.length === 0) return null
            if (menu.length > 2) {
                return (
                    <Dropdown
                        menu={{
                            items: menu,
                            onClick: onMenuClick(record),
                        }}
                    >
                        <a onClick={(e) => e.preventDefault()}>
                            <Space>
                                <MoreOne />
                            </Space>
                        </a>
                    </Dropdown>
                )
            }
            return (
                <Space>
                    {menu.map((item) => (
                        <Button
                            key={item?.key}
                            type="text"
                            danger={item?.danger}
                            icon={item?.icon}
                            onClick={() => onOperationClick && onOperationClick(item.key, record)}
                        >
                            {item?.label}
                        </Button>
                    ))}
                </Space>
            )
        },
        fixed: 'right',
        width: 100,
    }

    const pageSizeChanged = (pageSize: number) => {
        onPageSizeChange(pageSize)
    }

    const onChange = (page: number) => {
        onPageChange(page)
    }

    const onDeselect = () => {
        rowSelection && rowSelection.onChange && rowSelection.onChange([], [], { type: 'none' })
    }

    const BatchOperationRow = memo(
        ({ selectedCount, columnsLength, onDeselect, onBatchDelete, onBatchExport, t }: BatchOperationRowProps) => {
            const isVisible = selectedCount > 0
            // 表格列数依赖于是否启用操作列，如果启用了操作列，会重新计算表格列数，导致每次勾选或取消勾选时都会重新渲染，导致表格闪烁
            // 因此，为避免表格闪烁，不依赖于表格列数，而是根据是否启用操作列来计算表格列数
            const computeColumnsLength = operations && operations?.length > 0 ? columnsLength + 1 : columnsLength

            const leftThStyle = useMemo(() => ({ ...thStyle, position: 'sticky' as const, left: 0 }), [])
            const middleThStyle = useMemo(() => ({ ...thStyle, zIndex: 0 }), [])
            const rightThStyle = useMemo(() => ({ ...thStyle, position: 'sticky' as const, right: 0 }), [])

            const content = useMemo(() => {
                if (!isVisible) return null
                if (computeColumnsLength <= 4) {
                    return (
                        <th style={thStyle} colSpan={computeColumnsLength + 1}>
                            <div className="flex w-full batch-operation-th">
                                <div className="flex items-center gap-16 px-10 w-1/2">
                                    <div>{`${t('Common.Choosed')}: ${selectedCount}`}</div>
                                    <Button className="primary-text-btn" type="text" onClick={onDeselect}>
                                        {t('Action.Uncheck')}
                                    </Button>
                                </div>
                                <div className="flex justify-end px-10 w-1/2">
                                    <Button
                                        className="primary-text-btn"
                                        type="text"
                                        icon={<Delete />}
                                        onClick={onBatchDelete}
                                    >
                                        {t('Action.Batch_Delete')}
                                    </Button>
                                    <Button
                                        className="error-text-btn"
                                        type="text"
                                        icon={<DownloadFour />}
                                        onClick={onBatchExport}
                                    >
                                        {t('Action.Batch_Export')}
                                    </Button>
                                </div>
                            </div>
                        </th>
                    )
                } else {
                    return (
                        <>
                            <th colSpan={3} style={leftThStyle}>
                                <div className="batch-operation-th">
                                    <div className="flex items-center gap-16 px-10">
                                        <div>{`${t('Common.Choosed')}: ${selectedCount}`}</div>
                                        <Button className="primary-text-btn" type="text" onClick={onDeselect}>
                                            {t('Action.Uncheck')}
                                        </Button>
                                    </div>
                                </div>
                            </th>
                            <th colSpan={computeColumnsLength - 5} style={middleThStyle} className="ant-table-cell">
                                <div className="batch-operation-th" />
                            </th>
                            <th colSpan={3} style={rightThStyle}>
                                <div className="batch-operation-th">
                                    <div className="text-right px-10">
                                        <Button
                                            className="error-text-btn"
                                            type="text"
                                            icon={<Delete />}
                                            onClick={onBatchDelete}
                                        >
                                            {t('Action.Batch_Delete')}
                                        </Button>
                                        <Button
                                            className="primary-text-btn"
                                            type="text"
                                            icon={<DownloadFour />}
                                            onClick={onBatchExport}
                                        >
                                            {t('Action.Batch_Export')}
                                        </Button>
                                    </div>
                                </div>
                            </th>
                        </>
                    )
                }
            }, [isVisible])

            return (
                <tr className="batch-operation-tr" style={{ height: isVisible ? 'auto' : 0, overflow: 'hidden' }}>
                    {content}
                </tr>
            )
        },
    )

    const batchOperations = useMemo(
        () => ({
            header: {
                wrapper: (props: any) => {
                    const selectedRowKeys = rowSelection?.selectedRowKeys || []
                    const columnsLength = columns?.length || 0
                    return (
                        <thead {...props}>
                            {props.children}
                            <BatchOperationRow
                                selectedCount={selectedRowKeys.length}
                                columnsLength={columnsLength}
                                onDeselect={onDeselect}
                                onBatchDelete={onBatchDelete}
                                onBatchExport={onBatchExport}
                                t={t}
                            />
                        </thead>
                    )
                },
            },
        }),
        [rowSelection],
    )

    useEffect(() => {
        setTableColumns(columns)
    }, [columns])

    useEffect(() => {
        if (operations?.length) {
            columns?.push(operationColumn)
        }
    }, [operations])

    return (
        <div className="aii-table">
            <div className="toolbar">
                {toolbar?.length &&
                    toolbar.map((tool, index) => (
                        <Button key={index} type="text" icon={tool.icon} onClick={tool.onClick}>
                            {tool.label}
                        </Button>
                    ))}
            </div>
            <Table
                loading={loading}
                components={batchOperations}
                rowKey={rowKey}
                pagination={false}
                columns={tableColumns}
                dataSource={dataSource}
                rowSelection={rowSelection}
                scroll={{ x: 'max-content' }}
                sticky={{ offsetHeader: 0 }}
            />
            <div className="footer">
                <div className="pagination">
                    <Dropdown
                        menu={{
                            items: pageSizeOptions,
                            selectable: true,
                            defaultSelectedKeys: [pagination?.pageSize.toString()],
                            onClick: ({ key }) => {
                                pageSizeChanged(parseInt(key))
                            },
                        }}
                        popupRender={(menu) => <div className="text-center">{menu}</div>}
                    >
                        <div className="flex items-center cursor-pointer">
                            <span>{`${t('Common.PageSize_show')}: ${pagination?.pageSize}`}</span>
                            <Down />
                        </div>
                    </Dropdown>
                    <Divider type="vertical" />
                    <span>{`${t('Common.Total')}: ${pagination.total}`}</span>
                </div>
                <Pagination
                    simple={{ readOnly: true }}
                    showSizeChanger={false}
                    current={pagination.current}
                    pageSize={pagination.pageSize}
                    total={pagination.total}
                    onChange={(page) => onChange(page)}
                />
            </div>
        </div>
    )
}
export default AiiTable
