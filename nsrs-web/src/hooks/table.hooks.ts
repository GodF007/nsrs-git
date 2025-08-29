import { useState } from 'react'
import { client } from '@/utils/fetch'

// 通用分页数据类型
interface PageData<T> {
    total?: number
    current?: number
    records?: T[]
    pages?: number
    size?: number
}

// 通用响应结果类型
interface CommonResult<T> {
    code?: number
    message?: string
    data?: T
    success?: boolean
    timestamp?: number
}

// 表格响应类型
type TableResponse<T> = CommonResult<PageData<T>>

interface Pagination {
    current: number
    pageSize: number
    total: number
}

interface UseTableResult<T> {
    loading: boolean
    dataSource: T[]
    pagination: Pagination
    queryTableData: (
        initParams?: Record<string, any>,
        paginationOverride?: Pagination,
    ) => Promise<TableResponse<T> | undefined>
    onPageChange: (current: number) => void
    onPageSizeChange: (pageSize: number) => void
    onSearch: (initParams?: Record<string, any>) => void
    onSetDataSource: (data: T[]) => void
    selectedRows: T[]
    selectedRowKeys: React.Key[]
    onSelectChange: (
        selectedRowKeys: React.Key[],
        selectedRows: T[],
        info: { type: 'all' | 'none' | 'invert' | 'single' | 'multiple' },
    ) => void
}

function useTable<T>(
    apiPath: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' = 'GET',
    cb?: (response: PageData<T>) => PageData<T>,
): UseTableResult<T> {
    const [loading, setLoading] = useState(false)
    const [dataSource, setDataSource] = useState<T[]>([])
    const [pagination, setPagination] = useState<Pagination>({
        current: 1,
        pageSize: 10,
        total: 0,
    })
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
    const [selectedRows, setSelectedRows] = useState<T[]>([])
    const [params, setParams] = useState<Record<string, any>>({})

    function queryTableData(
        initParams?: Record<string, any>,
        paginationOverride?: Pagination,
    ): Promise<TableResponse<T> | undefined> {
        setLoading(true)
        const mergedParams = {
            ...(params || {}),
            ...(initParams || {}),
            current: paginationOverride?.current ?? pagination.current,
            size: paginationOverride?.pageSize ?? pagination.pageSize,
        }

        if (initParams) setParams((prev) => ({ ...prev, ...initParams }))

        // 根据请求方法调用对应的客户端方法
        const requestMethod = method.toUpperCase()
        let requestPromise

        switch (requestMethod) {
            case 'GET':
                requestPromise = client.GET(apiPath as any, {
                    params: { query: mergedParams },
                })
                break
            case 'POST':
                requestPromise = client.POST(apiPath as any, mergedParams)
                break
            case 'PUT':
                requestPromise = client.PUT(apiPath as any, mergedParams)
                break
            case 'DELETE':
                requestPromise = client.DELETE(apiPath as any, {
                    params: { query: mergedParams },
                })
                break
            default:
                requestPromise = client.GET(apiPath as any, {
                    params: { query: mergedParams },
                })
        }

        return requestPromise
            .then((res) => {
                return new Promise<TableResponse<T> | undefined>((resolve) => {
                    setTimeout(() => {
                        setLoading(false)
                        if (res.data && res.response.status === 200) {
                            const resultData = res.data as TableResponse<T>
                            if (resultData.data) {
                                let data = resultData.data
                                if (cb) {
                                    data = cb(data)
                                }
                                setDataSource((data.records as T[]) ?? [])
                                setPagination((prev) => ({
                                    ...prev,
                                    total: data?.total ?? 0,
                                }))
                                resolve(resultData)
                            }
                        }
                    }, 500)
                })
            })
            .catch((error) => {
                console.error('Error fetching table data:', error)
                setTimeout(() => {
                    setLoading(false)
                }, 500)
                return undefined
            })
    }

    function onPageChange(current: number) {
        setPagination((prev) => {
            const newPagination = {
                ...prev,
                current: current,
            }
            queryTableData(undefined, newPagination)
            return newPagination
        })
    }

    function onPageSizeChange(pageSize: number) {
        setPagination((prev) => {
            const newPagination = {
                ...prev,
                pageSize,
            }
            queryTableData(undefined, newPagination)
            return newPagination
        })
    }

    function onSearch(initParams: Record<string, any> = {}) {
        setPagination((prev) => {
            const newPagination = {
                ...prev,
                current: 1,
            }
            queryTableData(initParams, newPagination)
            return newPagination
        })
    }

    function onSetDataSource(data: T[]) {
        setDataSource(data)
    }

    function onSelectChange(
        newSelectedRowKeys: React.Key[],
        newSelectedRows: T[],
        info: { type: 'all' | 'none' | 'invert' | 'single' | 'multiple' },
    ) {
        setSelectedRowKeys(newSelectedRowKeys)
        setSelectedRows(newSelectedRows)
    }

    return {
        loading,
        dataSource,
        pagination,
        queryTableData,
        onPageChange,
        onPageSizeChange,
        onSearch,
        onSetDataSource,
        selectedRows,
        selectedRowKeys,
        onSelectChange,
    }
}

export default useTable
