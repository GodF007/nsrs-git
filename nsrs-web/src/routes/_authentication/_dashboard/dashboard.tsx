import { useEffect, useState } from 'react'

import { createFileRoute } from '@tanstack/react-router'
import { client } from '@/utils/fetch'
import { Row, Col, Typography, Space, Badge, Avatar, Progress } from 'antd'
import { formatDate } from '@/utils/common'
import { Remind, SpeedOne, Disk, RocketOne, Fire } from '@icon-park/react'

export const Route = createFileRoute('/_authentication/_dashboard/dashboard')({
    component: () => <Dashboard />,
    staticData: {
        code: 'Dashboard',
        langCode: 'Menu.Dashboard',
    },
})

const Dashboard: React.FC = () => {
    const [currentTime, setCurrentTime] = useState(new Date())

    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentTime(new Date())
        }, 1000)
        return () => {
            clearInterval(timer)
        }
    }, [])

    return (
        <div>
            <div className="wrapper mb-10">
                <Row align="middle" justify="space-between">
                    <Col span={8}>
                        <Space size="large" align="center">
                            <Avatar size={64} src="/src/assets/png/logo.png"></Avatar>
                            <div>
                                <Typography.Title level={2} style={{ margin: 0 }}>
                                    号卡资源管理系统
                                </Typography.Title>
                                <Typography.Text type="secondary">智能化资源管理平台</Typography.Text>
                            </div>
                        </Space>
                    </Col>
                    <Col span={8}></Col>
                    <Col span={8} style={{ textAlign: 'right' }}>
                        <Space direction="vertical" size="small">
                            <Typography.Text strong>{formatDate(currentTime)}</Typography.Text>
                            <Space>
                                <Badge count={3} size="small">
                                    <Remind />
                                </Badge>
                                <Typography.Text type="secondary">系统运行正常</Typography.Text>
                            </Space>
                        </Space>
                    </Col>
                </Row>
            </div>
            <div className="monitor">
                <Row gutter={10}>
                    <Col span={6}>
                        <div className="wrapper h-140">
                            <div className="mb-10 ">
                                <Typography.Text>系统负载</Typography.Text>
                            </div>
                            <div className="text-light-colorPrimary font-bold text-[24px]">
                                <Space size="small">
                                    <SpeedOne size={24} />
                                    <span>68.8%</span>
                                </Space>
                            </div>
                            <div>
                                <Progress percent={68} strokeColor="#697eff" showInfo={false} />
                            </div>
                        </div>
                    </Col>
                    <Col span={6}>
                        <div className="wrapper h-140">
                            <div className="mb-10 ">
                                <Typography.Text>内存使用</Typography.Text>
                            </div>
                            <div className="text-light-colorSuccess font-bold text-[24px]">
                                <Space size="small">
                                    <Disk size={24} />
                                    <span>72.3%</span>
                                </Space>
                            </div>
                            <div>
                                <Progress percent={68} strokeColor="#19c36d" showInfo={false} />
                            </div>
                        </div>
                    </Col>
                    <Col span={6}>
                        <div className="wrapper h-140">
                            <div className="mb-10 ">
                                <Typography.Text>响应时间</Typography.Text>
                            </div>
                            <div className="text-light-colorWarning font-bold text-[24px]">
                                <Space size="small">
                                    <RocketOne size={24} />
                                    <span>100ms</span>
                                </Space>
                            </div>
                            <div className="bg-[#fbad201a] px-16 py-2">
                                <span className="text-[#fbad20]">优秀</span>
                            </div>
                        </div>
                    </Col>
                    <Col span={6}>
                        <div className="wrapper h-140">
                            <div className="mb-10 ">
                                <Typography.Text>吞吐量</Typography.Text>
                            </div>
                            <div className="text-[#5956d6] font-bold text-[24px]">
                                <Space size="small">
                                    <Fire size={24} />
                                    <span>2847 / s</span>
                                </Space>
                            </div>
                            <div>
                                <span className="text-light-colorSuccess">↗ 2.3%</span>
                            </div>
                        </div>
                    </Col>
                </Row>
            </div>
        </div>
    )
}
