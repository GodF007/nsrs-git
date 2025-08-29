import createClient from 'openapi-fetch'
import type { paths } from '@/interface/api'
import { baseMiddleware, authMiddleware, downloadMiddleware, timeoutMiddleware } from './helper/middleware'

// 创建默认客户端
const client = createClient<paths>({
    baseUrl: '/nsrs',
})

// 创建超时测试客户端
const timeoutClient = createClient({
    baseUrl: '/',
})

client.use(timeoutMiddleware)
client.use(baseMiddleware)
client.use(authMiddleware)
client.use(downloadMiddleware)

timeoutClient.use(timeoutMiddleware)
timeoutClient.use(baseMiddleware)
timeoutClient.use(authMiddleware)
timeoutClient.use(downloadMiddleware)

export { client, timeoutClient }
