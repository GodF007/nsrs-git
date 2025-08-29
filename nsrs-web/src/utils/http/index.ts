import { message } from 'antd'
import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

import i18n from '@/utils/i18n'
import NProgress from '@/utils/nprogress'
import { ResultEnum } from '@/enums/httpEnum'
import { useUserStore } from '@/stores/system'

import { ResultData } from './interface'
import { AxiosCanceler } from './helper/axiosCancel'

import { isMicroAppEnv } from '@/utils/micro'
import { resetLogout } from '@/utils/system'
import router from '@/utils/router'
import { checkStatus } from './helper/checkStatus'

const defaultConfig = {
    baseURL: '/',
    timeout: 10000,
    withCredentials: !isMicroAppEnv,
}
const axiosCanceler = new AxiosCanceler()

// 基类，无拦截器
export class HttpService {
    service: AxiosInstance
    constructor(config: AxiosRequestConfig = {}) {
        this.service = axios.create({
            ...defaultConfig,
            ...config,
        })
        this.useRequestInterceptors()
        this.useResponseInterceptors()
    }
    useRequestInterceptors() {}
    useResponseInterceptors() {}
    // * 常用请求方法封装
    get<T>(url: string, params?: object, _object = {}): Promise<ResultData<T>> {
        return this.service.get(url, { params, ..._object })
    }
    post<T>(url: string, params?: object, _object = {}): Promise<ResultData<T>> {
        return this.service.post(url, params, _object)
    }
    put<T>(url: string, params?: object, _object = {}): Promise<ResultData<T>> {
        return this.service.put(url, params, _object)
    }
    delete<T>(url: string, params?: any, _object = {}): Promise<ResultData<T>> {
        return this.service.delete(url, { params, ..._object })
    }
    request<T>(request: AxiosRequestConfig): Promise<ResultData<T>> {
        return this.service.request(request)
    }
}

export class CommonService extends HttpService {
    useRequestInterceptors() {
        this.service.interceptors.request.use(
            (config: InternalAxiosRequestConfig) => {
                NProgress.start()
                axiosCanceler.addPending(config)
                const userInfo = useUserStore.getState().userInfo
                const token: string = userInfo.token ?? ''
                config.headers['token'] = token
                return config
            },
            (error: AxiosError) => {
                return Promise.reject(error)
            },
        )
    }
    useResponseInterceptors() {
        this.service.interceptors.response.use(
            (response: AxiosResponse) => {
                const { data, config } = response
                NProgress.done()
                axiosCanceler.removePending(config)
                // * 登录失效（code == 401）根据实际业务需求修改
                if (data.code === ResultEnum.UNAUTHORIZED) {
                    resetLogout()
                    router.navigate({
                        to: '/login',
                    })
                    if (isMicroAppEnv) {
                        window.microApp.forceSetGlobalData({
                            errorCode: ResultEnum.UNAUTHORIZED,
                        })
                    }
                    return Promise.resolve()
                    // const refresher = Refresher.getInstance()
                    // return refresher.predict(response.config)
                }
                // * 全局错误信息拦截（防止下载文件得时候返回数据流，没有code，直接报错）
                if (data.code && data.code !== ResultEnum.SUCCESS) {
                    message.error(data.msg)
                    return Promise.reject(data)
                }
                return Promise.resolve(data)
            },
            (error: AxiosError) => {
                NProgress.done()
                if (error.config) {
                    axiosCanceler.removePending(error.config)
                }
                if (error.code != 'ERR_CANCELED') {
                    checkStatus(error.status as number)
                }
                return Promise.reject(error)
            },
        )
    }
}
export class RefresherService extends HttpService {
    useRequestInterceptors() {
        this.service.interceptors.request.use(
            (config: InternalAxiosRequestConfig) => {
                const userInfo = useUserStore.getState().userInfo
                const token: string = userInfo.token ?? ''
                config.headers['token'] = token
                return config
            },
            (error: AxiosError) => {
                return Promise.reject(error)
            },
        )
    }
    useResponseInterceptors() {
        this.service.interceptors.response.use(
            (response: AxiosResponse) => {
                const { data } = response
                if (data.code && data.code !== ResultEnum.SUCCESS) {
                    //todo 登出逻辑
                    message.error(i18n.t('Message.Login_Failed'))
                    return Promise.resolve()
                }
                return Promise.resolve(data)
            },
            (error: AxiosError) => {
                //todo 登出逻辑
                message.error(error.message)
                return Promise.reject(error)
            },
        )
    }
}

const commonService = new CommonService()

export default commonService
