import NProgress from '@/utils/nprogress'
import { Middleware } from 'openapi-fetch'
import { useUserStore } from '@/stores/system'
import router from '@/utils/router'
import { message } from 'antd'
import { ResultEnum } from '@/enums/httpEnum'
import i18n from '@/utils/i18n'
import { checkStatus } from './checkStatus'

const downloadTypes: string[] = [
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/octet-stream',
    'application/pdf',
    'application/zip',
    'application/msword',
    'application/vnd.ms-excel',
    'application/vnd.ms-powerpoint',
    'text/csv',
    'image/',
]

const typeToExt: Record<string, string> = {
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
    'application/vnd.ms-excel': 'xls',
    'application/pdf': 'pdf',
    'application/zip': 'zip',
    'application/msword': 'doc',
    'application/vnd.ms-powerpoint': 'ppt',
    'text/csv': 'csv',
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/jpg': 'jpg',
    'image/svg+xml': 'svg',
    'image/gif': 'gif',
    'application/octet-stream': 'bin',
}

export const baseMiddleware: Middleware = {
    async onRequest({ request }) {
        NProgress.start()
        return request
    },
    async onResponse({ response }) {
        const contentType = response.headers.get('content-type')
        NProgress.done()
        if (response.status > 400) {
            checkStatus(response.status)
            return response
        }
        if (
            contentType &&
            downloadTypes.some((type) =>
                type.endsWith('/') ? contentType.startsWith(type) : contentType.includes(type),
            )
        ) {
            return response
        }
        const data = await response.clone().json()
        if (data.code !== ResultEnum.SUCCESS) {
            message.error(data.message)
            return response
        }
        return response
    },
}

export const authMiddleware: Middleware = {
    async onRequest({ request }) {
        const userInfo = useUserStore.getState().userInfo
        if (!userInfo.token) {
            message.error('请先登录')
            router.navigate({
                to: '/login',
            })
            return request
        } else {
            request.headers.set('Authorization', userInfo.token)
        }

        return request
    },
}

export const downloadMiddleware: Middleware = {
    async onResponse({ response }) {
        const contentType = response.headers.get('content-type') || ''
        if (
            contentType &&
            downloadTypes.some((type) =>
                type.endsWith('/') ? contentType.startsWith(type) : contentType.includes(type),
            )
        ) {
            const contentDisposition = response.headers.get('content-disposition') || ''
            let ext = 'bin'
            for (const [type, e] of Object.entries(typeToExt)) {
                if (contentType.includes(type)) {
                    ext = e
                    break
                }
            }
            let fileName = `download.${ext}`
            const match = contentDisposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i)
            if (match) {
                fileName = decodeURIComponent(match[1] || match[2])
            }
            const blob = await response.clone().blob()
            const link = document.createElement('a')
            if (blob) {
                link.href = URL.createObjectURL(blob)
                link.download = fileName
                link.click()
                URL.revokeObjectURL(link.href)
            }
            return response
        }
    },
}

export const timeoutMiddleware: Middleware = {
    async onRequest({ request }) {
        const timeout = 60000

        const controller = new AbortController()
        const timeoutId = setTimeout(() => {
            controller.abort()
            window.$message.error('请求超时，请稍后重试')
            NProgress.done()
        }, timeout)

        const newRequest = new Request(request, {
            signal: controller.signal,
        })

        newRequest.signal.addEventListener('abort', () => {
            clearTimeout(timeoutId)
        })

        Object.defineProperty(newRequest, '_timeoutId', {
            value: timeoutId,
            writable: false,
            enumerable: false,
            configurable: true,
        })

        return newRequest
    },
    async onResponse({ response, request }) {
        const timeoutId = (request as any)._timeoutId
        if (timeoutId) {
            clearTimeout(timeoutId)
        }
        return response
    },
}
