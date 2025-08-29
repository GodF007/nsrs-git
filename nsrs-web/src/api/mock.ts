import http from '@/utils/http'
import { DataType } from '@/interface/table'
import { ReqLoginForm, ResLogin, TableResponse } from '@/interface/common'

export const login = (userInfo: ReqLoginForm) => {
    return http.post<ResLogin>('/api/login', userInfo)
}

export const getMenu = () => {
    return http.get<System.MenuOptions[]>('/api/getMenu', {})
}

export const getTableData = (params: Record<string, any>) => {
    return http.post<TableResponse<DataType>>('/api/getTableData', params)
}
