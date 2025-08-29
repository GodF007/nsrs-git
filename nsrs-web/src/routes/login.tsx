import { useTranslation } from 'react-i18next'
import { Button, Checkbox, Flex, Form, Input } from 'antd'

import Logo from '@/assets/png/logo.png'
import { getMenu, login } from '@/api/mock'
import { enableTransitions } from '@/utils/system'
import { DotLottieReact } from '@lottiefiles/dotlottie-react'
import { Earth, Lock, Moon, SunOne, User } from '@icon-park/react'
import { createFileRoute, useNavigate } from '@tanstack/react-router'
import { useLanguageStore, useMenuStore, useThemeStore, useUserStore } from '@/stores/system'
import { menu } from '@/utils/menu'

export const Route = createFileRoute('/login')({
    component: () => <Login />,
    staticData: {
        code: 'Login',
        langCode: 'Common.Login',
    },
})

const Login: React.FC = () => {
    const [form] = Form.useForm()
    const { t } = useTranslation()
    const { setUserInfo } = useUserStore()
    const { theme, setTheme } = useThemeStore()
    const { language, setLanguage } = useLanguageStore()
    const { appendMenu } = useMenuStore()
    const navigate = useNavigate()

    const onChangeTheme = async (event: unknown) => {
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
    const onChangeLanguage = () => {
        language === 'zh' ? setLanguage('en') : setLanguage('zh')
    }
    const onFinish = async (values: any) => {
        const loginRes = await login(values)
        if (loginRes.success && loginRes.data) {
            setUserInfo(loginRes.data)
            setTimeout(() => {
                appendMenu(menu)
                navigate({ to: '/dashboard' })
            }, 500)
            // getMenuData()
        }
    }
    const getMenuData = async () => {
        const menuRes = await getMenu()
        if (menuRes.success) {
            appendMenu(menuRes.data ?? [])
            navigate({ to: '/dashboard' })
        }
    }
    return (
        <div className="login">
            <div className="login-center-box">
                <div className="login-banner">
                    <div className="system-logo">
                        <img src={Logo} />
                        <span>{t('System.System_Name')}</span>
                    </div>
                    <div className="lottie-wrapper">
                        <DotLottieReact src="/banner.json" autoplay loop />
                    </div>
                </div>
                <div className="login-form">
                    <div className="setting">
                        <Button
                            type="text"
                            icon={theme === 'light' ? <SunOne /> : <Moon />}
                            onClick={(event) => onChangeTheme(event)}
                        />
                        <Button type="text" icon={<Earth />} onClick={onChangeLanguage} />
                    </div>
                    <div className="login-form-title">{t('System.Welcome')}</div>
                    <Form
                        size="large"
                        name="login"
                        initialValues={{ remember: true }}
                        style={{ width: '60%', minWidth: 360 }}
                        onFinish={onFinish}
                    >
                        <Form.Item
                            name="username"
                            rules={[{ required: true, message: t('Required.User_Name_Required') }]}
                        >
                            <Input prefix={<User size={14} />} placeholder={t('Common.Name')} />
                        </Form.Item>
                        <Form.Item
                            name="password"
                            rules={[
                                {
                                    required: true,
                                    message: t('Required.User_Password_Required'),
                                },
                            ]}
                        >
                            <Input prefix={<Lock size={14} />} type="password" placeholder={t('Common.Password')} />
                        </Form.Item>
                        <Form.Item>
                            <Flex justify="space-between" align="center">
                                <Form.Item name="remember" valuePropName="checked" noStyle>
                                    <Checkbox>{t('Common.Remember_Me')}</Checkbox>
                                </Form.Item>
                                <a href="">{t('Common.Forgot_Password')}</a>
                            </Flex>
                            <Button className="my-4" block type="primary" htmlType="submit">
                                {t('Common.Log_In')}
                            </Button>
                            <div className="text-center">
                                <a href="">{t('Common.Register_Now')}</a>
                            </div>
                        </Form.Item>
                    </Form>
                </div>
            </div>
        </div>
    )
}
