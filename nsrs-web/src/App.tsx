import { useEffect } from 'react'

import router from '@/utils/router'
import { XProvider } from '@ant-design/x'
import { isMicroAppEnv } from '@/utils/micro'
import AppProvider from '@/components/AppProvider'
import { DrawerProvider } from '@/components/AiiDrawer'
import { RouterProvider } from '@tanstack/react-router'
import { DEFAULT_ICON_CONFIGS, IconProvider } from '@icon-park/react'

import useTheme from './hooks/theme.hooks'
import useLanguage from './hooks/language.hooks'
import { useLanguageStore, useThemeStore, useUserStore } from './stores/system'

const IconConfig = { ...DEFAULT_ICON_CONFIGS, prefix: 'icon', size: 18 }

function App() {
    const { setTheme } = useThemeStore()
    const { setLanguage } = useLanguageStore()
    const { locale } = useLanguage()
    const { themeAlgorithm, color } = useTheme()
    const { userInfo } = useUserStore()

    useEffect(() => {
        isMicroAppEnv &&
            window.microApp.addGlobalDataListener((data: any) => {
                if (data.language) {
                    setLanguage(data.language)
                }
                if (data.theme) {
                    setTheme(data.theme)
                }
            }, true)
    }, [])

    return (
        <XProvider
            locale={locale}
            theme={{
                token: { ...color, borderRadius: 8 },
                algorithm: themeAlgorithm,
            }}
        >
            <AppProvider>
                <IconProvider value={IconConfig}>
                    <DrawerProvider>
                        <RouterProvider
                            router={router}
                            context={{ token: userInfo.token, permissions: userInfo.permissions }}
                        />
                    </DrawerProvider>
                </IconProvider>
            </AppProvider>
        </XProvider>
    )
}

export default App
