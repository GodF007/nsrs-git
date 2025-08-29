export interface Tab {
    key: number
    label: string
    icon?: React.ReactNode
    content?: React.ReactNode | string
}

export interface AiiTabProps {
    defaultActiveKey: number
    tabs: Tab[]
    onTabClick?: (label: string) => void
    simple?: boolean
}
