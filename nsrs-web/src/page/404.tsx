import { Button, Result } from 'antd'
import { useNavigate } from '@tanstack/react-router'
import { useTranslation } from 'react-i18next'
import Page404 from '@/assets/svg/404.svg?react'

const Error404: React.FC = () => {
    const navigate = useNavigate()
    const { t } = useTranslation()
    return (
        <div className="w-full h-[100vh] flex justify-center items-center">
            <Result
                icon={<Page404 className="w-[28vw] h-[28vh]" />}
                subTitle={t('Error_Status.404')}
                extra={
                    <Button type="primary" onClick={() => navigate({ to: '/dashboard' })}>
                        {t('Action.Back')}
                    </Button>
                }
            />
        </div>
    )
}

export default Error404
