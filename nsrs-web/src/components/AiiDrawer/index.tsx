import { createContext, useContext, useState } from 'react'

import { Drawer } from 'antd'

import type { AiiDrawerProps, DrawerContent, DrawerContextType, DrawerProviderProps } from './AiiDrawer.types'

const DrawerContext = createContext<DrawerContextType>({
    showDrawer: () => {},
    closeDrawer: () => {},
})

export const DrawerProvider: React.FC<DrawerProviderProps> = ({ children }) => {
    const [open, setOpen] = useState(false)
    const [content, setContent] = useState<DrawerContent>(null)
    const [drawerProps, setDrawerProps] = useState<Partial<AiiDrawerProps>>({})

    const showDrawer = (drawerContent: DrawerContent, props: Partial<AiiDrawerProps> = {}) => {
        setContent(drawerContent)
        setDrawerProps(props)
        setOpen(true)
    }

    const closeDrawer = () => {
        setOpen(false)
        setContent(null)
        setDrawerProps({})
    }

    return (
        <DrawerContext.Provider value={{ showDrawer, closeDrawer }}>
            {children}
            <Drawer title="Drawer" placement="right" onClose={closeDrawer} open={open} width={400} {...drawerProps}>
                {content}
            </Drawer>
        </DrawerContext.Provider>
    )
}

export const useDrawer = (): DrawerContextType => {
    const context = useContext(DrawerContext)
    if (!context) {
        throw new Error('useDrawer must be used in DrawerProvider')
    }
    return context
}
