import { useCallback, useState } from 'react'

import type { ModalProps } from 'antd'

interface ModalOptions extends ModalProps {
    content?: React.ReactNode
    onOk?: () => void
    onCancel?: () => void
}

interface ModalController {
    isOpen: boolean
    modalOptions: ModalOptions
    openModal: (options?: ModalOptions) => void
    closeModal: () => void
    updateModal: (options: Partial<ModalOptions>) => void
}

export const createModal = (): ModalController => {
    const [isOpen, setIsOpen] = useState(false)
    const [modalOptions, setModalOptions] = useState<ModalOptions>({})

    const openModal = useCallback((options: ModalOptions = {}) => {
        setModalOptions(options)
        setIsOpen(true)
    }, [])

    const closeModal = useCallback(() => {
        setIsOpen(false)
        setModalOptions({})
    }, [])

    const updateModal = useCallback((options: Partial<ModalOptions>) => {
        setModalOptions((prev) => ({ ...prev, ...options }))
    }, [])

    return {
        isOpen,
        modalOptions,
        openModal,
        closeModal,
        updateModal,
    }
}

export const useModal = (ids: string[]): Record<string, ModalController> => {
    const modals = ids.reduce(
        (acc, id) => {
            acc[id] = createModal()
            return acc
        },
        {} as Record<string, ModalController>,
    )
    return modals
}
