import { Button, Space } from 'antd'
import { useTranslation } from 'react-i18next'

import { Welcome } from '@ant-design/x'
import AiiChat from '@/components/AiiChat'
import Robot from '@/assets/svg/robot.svg?react'
import { useDrawer } from '@/components/AiiDrawer'
import { useFullscreenStore, useLanguageStore, useThemeStore } from '@/stores/system'
import { FullScreenOne, MessageEmoji, Moon, OffScreenOne, Remind, SunOne, Translate } from '@icon-park/react'

interface ActionItem {
    key: string
    icon: React.ReactNode
    onClick?: (event: unknown) => void
    ariaLabel?: string
}

const Actions: React.FC = () => {
    const { t } = useTranslation()
    const { theme, setTheme } = useThemeStore()
    const { language, setLanguage } = useLanguageStore()
    const { fullscreen, setFullscreen } = useFullscreenStore()
    const { showDrawer } = useDrawer()
    const enableTransitions = () =>
        'startViewTransition' in document && window.matchMedia('(prefers-reduced-motion: no-preference)').matches

    const handleLanguageToggle = () => {
        language === 'zh' ? setLanguage('en') : setLanguage('zh')
    }

    const handleThemeToggle = async (event: unknown) => {
        const { clientX: x, clientY: y } = event as MouseEvent
        const isDark = theme === 'dark'

        if (!enableTransitions()) {
            setTheme(theme === 'light' ? 'dark' : 'light')
            return
        }

        const clipPath = [
            `circle(0px at ${x}px ${y}px)`,
            `circle(${Math.hypot(Math.max(x, innerWidth - x), Math.max(y, innerHeight - y))}px at ${x}px ${y}px)`,
        ]

        await document.startViewTransition(async () => {
            setTheme(theme === 'light' ? 'dark' : 'light')
        }).ready

        document.documentElement.animate(
            { clipPath: !isDark ? clipPath.reverse() : clipPath },
            {
                duration: 500,
                easing: 'ease-in',
                pseudoElement: `::view-transition-${!isDark ? 'old' : 'new'}(root)`,
            },
        )
    }

    const handleFullscreenToggle = () => {
        setFullscreen(!fullscreen)
    }

    const handleOpenAI = () => {
        showDrawer(<AiiChat></AiiChat>, {
            title: (
                <Welcome
                    variant="borderless"
                    icon={<Robot className="w-64 h-64" />}
                    title={t('AI.Welcome')}
                    description={t('AI.Description')}
                    className="p-4"
                />
            ),
            width: '50%',
        })
    }

    const actionItems: ActionItem[] = [
        {
            key: 'message',
            icon: <Remind />,
            ariaLabel: 'Remind',
        },
        {
            key: 'theme',
            icon: theme === 'light' ? <SunOne /> : <Moon />,
            onClick: handleThemeToggle,
            ariaLabel: theme === 'light' ? 'Light theme' : 'Dark theme',
        },
        {
            key: 'language',
            icon: <Translate />,
            onClick: handleLanguageToggle,
            ariaLabel: 'Change language',
        },
        {
            key: 'fullscreen',
            icon: fullscreen ? <OffScreenOne /> : <FullScreenOne />,
            onClick: handleFullscreenToggle,
            ariaLabel: 'Enter fullscreen',
        },
        {
            key: 'aiChat',
            icon: <MessageEmoji />,
            ariaLabel: 'AI Chat',
            onClick: handleOpenAI,
        },
    ]

    return (
        <Space>
            {actionItems.map(({ key, icon, onClick, ariaLabel }) => (
                <Button key={key} type="text" icon={icon} onClick={onClick} aria-label={ariaLabel} />
            ))}
        </Space>
    )
}

export default Actions
