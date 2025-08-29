import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { Button, Result } from 'antd'
import { useTranslation } from 'react-i18next'
import Page403 from '@/assets/svg/403.svg?react'

export const Route = createFileRoute('/403')({
    component: () => <Error403 />,
    staticData: {
        code: '404',
        langCode: 'Error_Status.403',
    },
})

const Error403: React.FC = () => {
    const navigate = useNavigate()
    const { t } = useTranslation()
    return (
        <div className="w-full h-[100vh] flex justify-center items-center">
            <Result
                icon={<Page403 className="w-[28vw] h-[28vh]" />}
                subTitle={t('Error_Status.403')}
                extra={
                    <Button type="primary" onClick={() => navigate({ to: '/dashboard' })}>
                        {t('Action.Back')}
                    </Button>
                }
            />
        </div>
    )
}
