import type { DrawerProps } from 'antd'

export interface AiiDrawerProps extends DrawerProps {}

export type DrawerContent = React.ReactNode | null

export interface DrawerContextType {
    showDrawer: (content: DrawerContent, drawerProps?: Partial<AiiDrawerProps>) => void
    closeDrawer: () => void
}

export interface DrawerProviderProps {
    children: React.ReactNode
}
