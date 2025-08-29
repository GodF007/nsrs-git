import { useEffect, useMemo } from 'react'

import { theme as antTheme } from 'antd'

import { useThemeStore } from '@/stores/system'
export default function useTheme() {
    const { theme, color } = useThemeStore()
    useEffect(() => {
        const html = document.documentElement as HTMLElement
        const body = document.body
        html.setAttribute('style', '')
        body.setAttribute('style', '--primary-color:' + color.colorPrimary)
        document.querySelector('html')!.setAttribute('class', theme == 'dark' ? 'dark' : '')
    }, [theme, color])

    const themeAlgorithm = useMemo(() => {
        return theme == 'dark' ? antTheme.darkAlgorithm : antTheme.defaultAlgorithm
    }, [theme])

    return {
        themeAlgorithm,
        color,
    }
}
