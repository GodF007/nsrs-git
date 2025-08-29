import { useEffect } from 'react'

import Layouts from '@/layouts/index'
import { useTabStore } from '@/stores/system'
import { createFileRoute, redirect, useMatches } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication')({
    component: () => {
        const { addTab } = useTabStore()
        const match = useMatches().find((item) => item.pathname === location.pathname)
        useEffect(() => {
            if (match) {
                addTab({
                    code: match.staticData.code === 'Iframe' ? match.params.name : match.staticData.code,
                    path: match.pathname,
                    closeable: match.staticData.code !== 'Dashboard',
                    link: match.staticData.code === 'Iframe' ? match.search.url : '',
                })
            }
        }, [match, location.pathname])
        return <Layouts />
    },
    beforeLoad: ({ context, matches, location }) => {
        const { permissions, token } = context
        if (!token) throw redirect({ to: '/login' })
        const code = matches.find((item) => item.pathname === location.pathname)?.staticData.code
        if (permissions && permissions.length > 0) {
            if (code !== 'Iframe' && !permissions.includes(code)) throw redirect({ to: '/403' })
        }
    },
})
