export interface ReqLoginForm {
    username: string
    password: string
}
export interface ResLogin {
    token: string
}

export interface TableResponse<T> {
    list: T[]
    total: number
}
