import { useState, useRef, useEffect } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import classNames from 'classnames'
import { AiiTabProps } from './AiiTab.types'

import './index.css'

const AiiTab: React.FC<AiiTabProps> = (props) => {
    const { tabs, defaultActiveKey, onTabClick, simple = true } = props
    const [activeTabKey, setActiveTabKey] = useState(defaultActiveKey || 0)

    const [indicatorPosition, setIndicatorPosition] = useState({ left: 0, width: 0, top: 0 })
    const navRef = useRef<HTMLDivElement>(null)
    const activeTabRef = useRef<HTMLDivElement>(null)

    const handleTabClick = (key: number, label: string) => {
        onTabClick?.(label)
        setActiveTabKey(key)
    }

    useEffect(() => {
        if (activeTabRef.current && navRef.current) {
            const { offsetLeft, offsetWidth, offsetTop } = activeTabRef.current
            setIndicatorPosition({ left: offsetLeft, width: offsetWidth, top: offsetTop })
        }
    }, [activeTabKey])

    useEffect(() => {
        if (activeTabRef.current && navRef.current) {
            const { offsetLeft, offsetWidth, offsetTop } = activeTabRef.current
            setIndicatorPosition({ left: offsetLeft, width: offsetWidth, top: offsetTop })
        }
    }, [activeTabKey])

    return (
        <motion.div className="aii-tab">
            <div
                ref={navRef}
                className={classNames('aii-tab-nav', { 'border-b': !simple, 'dark:border-dark-colorBorder': !simple })}
            >
                {tabs.map((item) => (
                    <div
                        key={item.key}
                        ref={activeTabKey === item.key ? activeTabRef : null}
                        className={classNames('aii-tab-nav-item', {
                            'aii-tab-nav-item-active': activeTabKey === item.key,
                        })}
                        onClick={() => handleTabClick(item.key, item.label)}
                    >
                        <div className="flex items-center gap-4">
                            {item.icon && <>{item.icon}</>}
                            {item.label}
                        </div>
                    </div>
                ))}
                <motion.div
                    className="aii-tab-nav-indicator"
                    layoutId="activeTabIndicator"
                    animate={{
                        left: indicatorPosition.left,
                        top: indicatorPosition.top,
                        width: indicatorPosition.width,
                    }}
                    transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                />
            </div>
            <div className="aii-tab-content">
                <AnimatePresence mode="wait">
                    {tabs.map((item) => (
                        <motion.div
                            key={item.key}
                            variants={{
                                active: { opacity: 1, filter: 'blur(0px)', height: 'auto' },
                                inactive: { opacity: 0, filter: 'blur(4px)', height: 0 },
                            }}
                            initial="inactive"
                            animate={activeTabKey === item.key ? 'active' : 'inactive'}
                            exit="inactive"
                            transition={{ ease: 'easeInOut', duration: 0.3 }}
                            className="aii-tab-content-item"
                            style={{ overflow: 'hidden' }}
                        >
                            {item.content}
                        </motion.div>
                    ))}
                </AnimatePresence>
            </div>
        </motion.div>
    )
}

export default AiiTab
