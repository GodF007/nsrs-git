import { MockMethod } from 'vite-plugin-mock'
import Mock from 'mockjs'

export default [
    // user login
    {
        url: '/api/login',
        method: 'post',
        timeout: 10,
        response: ({ body }: { body: { username: string; password: string } }) => {
            const { username, password } = body

            const validUsername = 'admin'
            const validPassword = '123456'

            if (username === validUsername && password === validPassword) {
                return {
                    code: 200,
                    msg: 'Login successful',
                    success: true,
                    data: {
                        userId: '1008',
                        userName: 'admin',
                        token: Mock.Random.guid(),
                        permissions: [
                            'Dashboard',
                            'Number_Resource',
                            'Number_Level',
                            'Number_Segment',
                            'Number_Pattern',
                            'Number_Region',
                            'Number_Hlr',
                            'Business',
                            'Card_Selection',
                            'Sim_Card',
                            'Sim_Card_Resource',
                            'Sim_Card_Type',
                            'Sim_Card_Specification',
                            'Sim_Card_Supplier',
                            'Sim_Card_Organization',
                            'Sim_Card_Batch',
                            'Sim_Card_Alert',
                            'IMSI',
                            'IMSI_Group',
                            'IMSI_Resource',
                            'IMSI_Supplier',
                        ],
                    },
                }
            } else {
                return {
                    code: 401,
                    msg: 'Invalid username or password',
                    success: false,
                    data: null,
                }
            }
        },
    },
    // get menu
    {
        url: '/api/getMenu',
        method: 'get',
        timeout: 0,
        statusCode: 200,
        response: () => {
            return {
                code: 200,
                msg: 'success',
                success: true,
                data: [
                    {
                        key: 'Dashboard',
                        label: 'Dashboard',
                        icon: 'dashboard',
                        path: '/dashboard',
                        filePath: '/dashboard/index',
                    },
                    {
                        key: 'Tab',
                        label: 'Tab',
                        icon: 'tab',
                        path: '/tab',
                        filePath: '/tab/index',
                    },
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
                    {
                        key: 'External_Link',
                        label: 'External Link',
                        icon: 'link',
                        path: '/iframe',
                        children: [
                            {
                                key: 'Baidu',
                                label: 'Baidu',
                                path: '/iframe/Baidu',
                                link: 'https://www.baidu.com/',
                                filePath: '/iframe/index',
                            },
                            {
                                key: 'React',
                                label: 'React',
                                path: '/iframe/React',
                                link: 'https://zh-hans.react.dev/',
                                filePath: '/iframe/index',
                            },
                        ],
                    },
                ],
            }
        },
    },
    // get table data
    {
        url: '/api/getTableData',
        method: 'post',
        timeout: 0,
        statusCode: 200,
        response: ({
            body,
        }: {
            body: {
                current: number
                pageSize: number
                address: string
                age: string
                email: string
                username: string
            }
        }) => {
            const { current = 1, pageSize = 10 } = body
            const currentPage = current
            const pageSizeNum = pageSize

            // 生成1000条模拟数据
            const total = 1000
            const data = Mock.mock({
                [`list|${pageSizeNum}`]: [
                    {
                        'id|+1': (currentPage - 1) * pageSizeNum + 1,
                        name: '@cname',
                        'age|18-60': 1,
                        address:
                            '@province @city @county(true) - @city @county(true) - 详细地址: @street @natural(100, 999)号',
                        email: '@email',
                        phone: /^1[385][1-9]\d{8}/,
                        createTime: '@datetime',
                    },
                ],
            }).list

            return {
                code: 200,
                msg: 'success',
                success: true,
                data: {
                    list: data,
                    total,
                },
            }
        },
    },
] as MockMethod[]
