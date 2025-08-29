import { useTranslation } from 'react-i18next'

import { useTabStore } from '@/stores/system'
import { CloseOutlined } from '@ant-design/icons'
import { Link, useLocation, useNavigate } from '@tanstack/react-router'
import { useEffect, useRef, useState } from 'react'
import { Dropdown, MenuProps } from 'antd'
import { CloseOne, ArrowLeft, ArrowRight, CloseSmall } from '@icon-park/react'

const TabBar: React.FC = () => {
    const { t } = useTranslation()
    const location: Location = useLocation()
    const { tabs, removeTab, setTabs, closeAllTabs, closeLeftTabs, closeRightTabs } = useTabStore()
    const navigate = useNavigate()
    const tabBarRef = useRef<HTMLDivElement>(null)
    const activeTabRef = useRef<HTMLDivElement>(null)
    const tabRefs = useRef<{ [key: string]: HTMLDivElement | null }>({})
    const [contextMenuTab, setContextMenuTab] = useState<System.Tab | null>(null)
    const [dropdownVisible, setDropdownVisible] = useState(false)

    const handleRemoveTab = (tab: System.Tab) => {
        removeTab(tab)
        if (tabs.length > 0) {
            const currentIndex = tabs.findIndex((t) => t.code === tab.code)
            const lastTab = tabs[currentIndex - 1]

            if (lastTab && tab.path === location.pathname) {
                navigate({ to: lastTab.path, search: { url: lastTab.link ?? undefined } })
            }
        }
    }

    const scrollToTargetTab = (tabRef: HTMLDivElement | null) => {
        if (tabRef && tabBarRef.current) {
            const tabBar = tabBarRef.current
            const tab = tabRef

            const tabBarRect = tabBar.getBoundingClientRect()
            const tabRect = tab.getBoundingClientRect()

            if (tabRect.right > tabBarRect.right || tabRect.left < tabBarRect.left) {
                tab.scrollIntoView({
                    behavior: 'smooth',
                    inline: 'center',
                    block: 'nearest',
                })
            }
        }
    }

    const handleCloseCurrentTab = () => {
        if (contextMenuTab) {
            handleRemoveTab(contextMenuTab)
            setDropdownVisible(false)
        }
    }

    const handleCloseAllTabs = () => {
        closeAllTabs()
        setDropdownVisible(false)
        // 导航到 dashboard
        navigate({ to: '/dashboard' })
    }

    const handleCloseLeftTabs = () => {
        if (contextMenuTab) {
            closeLeftTabs(contextMenuTab)
            // 导航到目标标签页
            navigate({ to: contextMenuTab.path, search: { url: contextMenuTab.link ?? undefined } })
        }
        setDropdownVisible(false)
    }

    const handleCloseRightTabs = () => {
        if (contextMenuTab) {
            closeRightTabs(contextMenuTab)
            // 导航到目标标签页
            navigate({ to: contextMenuTab.path, search: { url: contextMenuTab.link ?? undefined } })
        }
        setDropdownVisible(false)
    }

    useEffect(() => {
        if (activeTabRef.current) {
            scrollToTargetTab(activeTabRef.current)
        }
    }, [location.pathname, tabs])

    const dropdownMenu: MenuProps['items'] = [
        {
            key: 'closeCurrent',
            icon: <CloseOne />,
            label: t('Common.Close_Current'),
        },
        {
            key: 'closeLeft',
            icon: <ArrowLeft />,
            label: t('Common.Close_Left'),
        },
        {
            key: 'closeRight',
            icon: <ArrowRight />,
            label: t('Common.Close_Right'),
        },
        {
            key: 'closeAll',
            icon: <CloseSmall />,
            label: t('Common.Close_All'),
        },
    ]

    const handleClickDropdownMenu: MenuProps['onClick'] = ({ key }) => {
        switch (key) {
            case 'closeCurrent':
                handleCloseCurrentTab()
                break
            case 'closeLeft':
                handleCloseLeftTabs()
                break
            case 'closeRight':
                handleCloseRightTabs()
                break
            case 'closeAll':
                handleCloseAllTabs()
                break
            default:
                break
        }
    }

    return (
        <div ref={tabBarRef} className="tab-bar overflow-x-auto whitespace-nowrap">
            {tabs.map((tab, index) => {
                return (
                    <Dropdown
                        key={tab.code}
                        trigger={['contextMenu']}
                        open={dropdownVisible && contextMenuTab?.code === tab.code}
                        onOpenChange={(open) => {
                            setDropdownVisible(open)
                            if (open) {
                                setContextMenuTab(tab)
                            } else if (contextMenuTab?.code === tab.code) {
                                setContextMenuTab(null)
                            }
                        }}
                        menu={{ items: dropdownMenu, onClick: handleClickDropdownMenu }}
                    >
                        <Link key={index} to={tab.path} search={{ url: tab.link ?? undefined }}>
                            <div
                                ref={(el) => {
                                    if (tab.path === location.pathname) {
                                        activeTabRef.current = el
                                    } else {
                                        tabRefs.current[tab.path] = el
                                    }
                                }}
                                className={`tab inline-block ${tab.path === location.pathname ? ' tab-active' : ''} `}
                                onContextMenu={(e) => {
                                    e.preventDefault()
                                    setContextMenuTab(tab)
                                    setDropdownVisible(true)
                                }}
                            >
                                <span className="px-10">{t(`Menu.${tab.code}`)}</span>
                                {tab.closeable && (
                                    <CloseOutlined
                                        className="tab-close-btn"
                                        onClick={(event) => {
                                            event.preventDefault()
                                            event.stopPropagation()
                                            handleRemoveTab(tab)
                                        }}
                                    />
                                )}
                            </div>
                        </Link>
                    </Dropdown>
                )
            })}
        </div>
    )
}

export default TabBar
