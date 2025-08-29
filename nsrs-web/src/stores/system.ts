import { create } from 'zustand'

import { ThemeEnum } from '@/enums/themeEnum'
import { createJSONStorage, persist } from 'zustand/middleware'

const storagePrefix = `[${import.meta.env.VITE_TITLE || 'AII'}]`

export const useUserStore = create<System.UserState>()(
    persist(
        (set) => ({
            userInfo: {
                userId: '',
                userName: '',
                token: '',
                permissions: [],
            },
            setUserInfo: (userInfo: System.UserInfo) => set({ userInfo }),
        }),
        {
            name: `${storagePrefix}-user-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)

export const useMenuStore = create<System.MenuState>()(
    persist(
        (set) => ({
            menu: [
                // {
                //   key: 'Dashboard',
                //   label: 'Dashboard',
                //   icon: 'dashboard',
                //   path: '/dashboard',
                //   filePath: '/dashboard/index',
                // },
            ],
            setMenu: (menu: System.MenuOptions[]) => set({ menu }),
            appendMenu: (menu: System.MenuOptions[]) =>
                set((state) => {
                    const newMenuItems = menu.filter(
                        (newItem) => !state.menu.some((existingItem) => existingItem.key === newItem.key),
                    )
                    return { menu: [...state.menu, ...newMenuItems] }
                }),
        }),
        {
            name: `${storagePrefix}-menu-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)

export const useMenuCollapseStore = create<System.CollapseState>()(
    persist(
        (set) => ({
            collapsed: false,
            collapseMenuOpenedKeys: [],
            expandMenuOpenedKeys: [],
            toggleCollapsed: () => set((state) => ({ collapsed: !state.collapsed })),
            setOpenedKeys: (openedKeys: string[]) =>
                set((state) => ({
                    ...(state.collapsed
                        ? { collapseMenuOpenedKeys: openedKeys }
                        : { expandMenuOpenedKeys: openedKeys }),
                })),
        }),
        {
            name: `${storagePrefix}-collapse-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)

export const useThemeStore = create<System.ThemeState>()(
    persist(
        (set) => ({
            theme: 'light',
            color: {
                colorPrimary: ThemeEnum.colorPrimary,
                colorSuccess: ThemeEnum.colorSuccess,
                colorWarning: ThemeEnum.colorWarning,
                colorError: ThemeEnum.colorError,
            },
            setTheme: (theme: string) => set({ theme }),
            setColor: (color: System.Color) => set({ color }),
        }),
        {
            name: `${storagePrefix}-theme-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)

export const useTabStore = create<System.TabState>()(
    persist(
        (set) => ({
            tabs: [],
            addTab: (tab: System.Tab) =>
                set((state) => {
                    const existingTab = state.tabs.find((t) => t.code === tab.code)
                    if (existingTab) {
                        return state
                    }
                    return { tabs: [...state.tabs, tab] }
                }),
            removeTab: (tab: System.Tab) =>
                set((state) => {
                    const currentTabs = state.tabs
                    const currentIndex = currentTabs.findIndex((t) => t.code === tab.code)
                    if (currentIndex !== -1) {
                        const newTabs = currentTabs.filter((t) => t.code !== tab.code)
                        return { tabs: newTabs }
                    }
                    return { tabs: currentTabs }
                }),
            setTabs: (tabs: System.Tab[]) => set({ tabs }),
            closeAllTabs: () =>
                set((state) => {
                    const uncloseableTabs = state.tabs.filter((tab) => !tab.closeable)
                    return { tabs: uncloseableTabs }
                }),
            closeLeftTabs: (targetTab: System.Tab) =>
                set((state) => {
                    const currentIndex = state.tabs.findIndex((tab) => tab.code === targetTab.code)
                    if (currentIndex > 0) {
                        // 保留目标标签页及其右侧的所有标签页
                        const rightTabs = state.tabs.slice(currentIndex)
                        // 保留左侧不可关闭的标签页
                        const uncloseableLeftTabs = state.tabs.filter(
                            (tab, index) => index < currentIndex && !tab.closeable,
                        )
                        return { tabs: [...uncloseableLeftTabs, ...rightTabs] }
                    }
                    return { tabs: state.tabs }
                }),
            closeRightTabs: (targetTab: System.Tab) =>
                set((state) => {
                    const currentIndex = state.tabs.findIndex((tab) => tab.code === targetTab.code)
                    if (currentIndex < state.tabs.length - 1) {
                        const leftTabs = state.tabs.slice(0, currentIndex + 1)
                        const uncloseableRightTabs = state.tabs.filter(
                            (tab, index) => index > currentIndex && !tab.closeable,
                        )
                        return { tabs: [...leftTabs, ...uncloseableRightTabs] }
                    }
                    return { tabs: state.tabs }
                }),
        }),
        {
            name: `${storagePrefix}-tab-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)

export const useLanguageStore = create<System.LanguageState>()(
    persist(
        (set) => ({
            language: 'zh',
            setLanguage: (language: string) => set({ language }),
        }),
        {
            name: `${storagePrefix}-language-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)

export const useFullscreenStore = create<System.FullscreenState>()(
    persist(
        (set) => ({
            fullscreen: false,
            setFullscreen: (fullscreen: boolean) => set({ fullscreen }),
        }),
        {
            name: `${storagePrefix}-fullscreen-storage`,
            storage: createJSONStorage(() => localStorage),
        },
    ),
)
