import { useEffect } from 'react'

import { useTranslation } from 'react-i18next'

import { createRootRouteWithContext, Outlet, redirect, useMatches } from '@tanstack/react-router'

interface RootRouteContext {
    token: string
    permissions: string[]
}

export const Route = createRootRouteWithContext<RootRouteContext>()({
    component: () => {
        const { t } = useTranslation()
        const locationCur = useMatches().find((item) => item.pathname === location.pathname)
        const code =
            locationCur?.staticData.code === 'Iframe'
                ? `Menu.${locationCur?.params.name}`
                : locationCur?.staticData.langCode

        useEffect(() => {
            document.title = `${t('System.System_Name')} | ${t(code)}🌟`
        }, [code])

        return (
            <>
                <Outlet />
            </>
        )
    },
    beforeLoad: (ctx) => {
        if (location.pathname === '/') {
            throw redirect({ to: '/dashboard' })
        }
    },
})
