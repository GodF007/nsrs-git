import { useEffect } from 'react'

import { useTranslation } from 'react-i18next'
import type { FormInstance, TableColumnsType } from 'antd'
import { Col, Form, Input, Modal, Row, Select, Switch } from 'antd'

import { client } from '@/utils/fetch'
import useTable from '@/hooks/table.hooks'
import AiiTable from '@/components/AiiTable'
import AiiSearch from '@/components/AiiSearch'
import { useModal } from '@/hooks/modal.hooks'
import type { components } from '@/interface/api'
import { createFileRoute } from '@tanstack/react-router'
import { CheckOne, Forbid, Newlybuild } from '@icon-park/react'

export const Route = createFileRoute('/_authentication/number/level')({
    component: RouteComponent,
    staticData: {
        code: 'Number_Level',
        langCode: 'Menu.Number_Level',
    },
})

const modalTypeEnums: string[] = ['level']

const LevelForm = (props: { form: FormInstance; operation: 'create' | 'update' | 'detail' }) => {
    const { t } = useTranslation()
    return (
        <Form
            form={props.form}
            layout="vertical"
            initialValues={{ needApproval: 0 }}
            disabled={props.operation === 'detail'}
        >
            <Row gutter={16}>
                <Form.Item name="levelId" hidden>
                    <Input />
                </Form.Item>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Level_Name')}
                        name="levelName"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Level_Code')}
                        name="levelCode"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Charge')}
                        name="charge"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={t('Common.Approval')} name="needApproval" valuePropName="checked">
                        <Switch
                            checkedChildren={t('Common.Yes')}
                            unCheckedChildren={t('Common.No')}
                            onChange={(checked) => props.form.setFieldValue('needApproval', checked ? 1 : 0)}
                        />
                    </Form.Item>
                </Col>
                <Col span={24}>
                    <Form.Item label={t('Common.Remark')} name="description">
                        <Input.TextArea placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
            </Row>
        </Form>
    )
}

function RouteComponent() {
    const { t } = useTranslation()
    const statusEnums = [
        {
            label: t('Enum.Enabled'),
            value: 1,
        },
        {
            label: t('Enum.Disabled'),
            value: 0,
        },
    ]

    const { queryTableData, dataSource, loading, onPageChange, onPageSizeChange, pagination, onSearch } =
        useTable<components['schemas']['NumberLevel']>('/msisdn/numberLevel/page')

    const searchItems = [
        <Form.Item name="numberLevel">
            <Input placeholder={t('Number_Resource.Level_Name')} allowClear />
        </Form.Item>,
        <Form.Item name="levelCode">
            <Input placeholder={t('Number_Resource.Level_Code')} allowClear />
        </Form.Item>,
        <Form.Item name="status">
            <Select placeholder={t('Common.Status')} allowClear options={statusEnums} />
        </Form.Item>,
    ]

    const columns: TableColumnsType<components['schemas']['NumberLevel']> = [
        {
            title: t('Number_Resource.Level_Name'),
            dataIndex: 'levelName',
            key: 'levelName',
            fixed: 'left',
        },
        {
            title: t('Number_Resource.Level_Code'),
            dataIndex: 'levelCode',
            key: 'levelCode',
        },
        {
            title: t('Number_Resource.Charge'),
            dataIndex: 'charge',
            key: 'charge',
        },
        {
            title: t('Common.Approval'),
            dataIndex: 'needApproval',
            key: 'needApproval',
            render: (needApproval) => {
                return needApproval === 1 ? t('Common.Yes') : t('Common.No')
            },
        },
        {
            title: t('Common.Status'),
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                return statusEnums.find((item) => item.value === status)?.label
            },
        },
        {
            title: t('Common.Remark'),
            dataIndex: 'description',
            key: 'description',
        },
    ]

    const modal = useModal(modalTypeEnums)
    const [levelForm] = Form.useForm()

    const toolbar = [
        {
            icon: <Newlybuild />,
            label: t('Action.Create'),
            onClick: () => {
                modal.level.openModal({
                    title: t('Action.Create'),
                    content: <LevelForm form={levelForm} operation="create" />,
                    onOk: () => {
                        levelForm.validateFields().then(async (values) => {
                            const { data } = await client.POST('/msisdn/numberLevel/add', {
                                body: values,
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Create_Success'))
                                onSearch()
                                levelForm.resetFields()
                                modal.level.closeModal()
                            }
                        })
                    },
                    onCancel: () => {
                        levelForm.resetFields()
                        modal.level.closeModal()
                    },
                })
            },
        },
    ]

    const onOperationClick = (key: string, record: any) => {
        switch (key) {
            case 'EDIT':
                levelForm.setFieldsValue(record)
                modal.level.openModal({
                    title: t('Action.Edit'),
                    content: <LevelForm form={levelForm} operation="update" />,
                    onOk: () => {
                        levelForm.validateFields().then(async (values) => {
                            const { data } = await client.PUT('/msisdn/numberLevel/update', {
                                body: values,
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Update_Success'))
                                onSearch()
                                levelForm.resetFields()
                                modal.level.closeModal()
                            }
                        })
                    },
                    onCancel: () => {
                        levelForm.resetFields()
                        modal.level.closeModal()
                    },
                })
                break
            case 'DETAIL':
                levelForm.setFieldsValue(record)
                modal.level.openModal({
                    title: t('Action.Detail'),
                    content: <LevelForm form={levelForm} operation="detail" />,
                    onOk: () => {
                        levelForm.resetFields()
                        modal.level.closeModal()
                    },
                    onCancel: () => {
                        levelForm.resetFields()
                        modal.level.closeModal()
                    },
                })
                break
            case 'DELETE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Delete_Tips'),
                    onOk: async () => {
                        const { data } = await client.DELETE('/msisdn/numberLevel/{levelId}', {
                            params: {
                                path: {
                                    levelId: record.levelId,
                                },
                            },
                        })
                        if (data?.success) {
                            window.$message.success(t('Message.Delete_Success'))
                            onSearch()
                        }
                    },
                })
                break
            case 'ENABLE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Enable_Tips'),
                    onOk: async () => {
                        const { data } = await client.PUT('/msisdn/numberLevel/enable/{levelId}', {
                            params: {
                                path: {
                                    levelId: record.levelId,
                                },
                            },
                        })
                        if (data?.success) {
                            window.$message.success(t('Message.Update_Success'))
                            onSearch()
                        }
                    },
                })
                break
            case 'DISABLE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Disable_Tips'),
                    onOk: async () => {
                        const { data } = await client.PUT('/msisdn/numberLevel/disable/{levelId}', {
                            params: {
                                path: {
                                    levelId: record.levelId,
                                },
                            },
                        })
                        if (data?.success) {
                            window.$message.success(t('Message.Update_Success'))
                            onSearch()
                        }
                    },
                })
                break
            default:
                break
        }
    }

    useEffect(() => {
        queryTableData()
    }, [])

    return (
        <div>
            <AiiSearch items={searchItems} onSearch={onSearch} />
            <div className="wrapper">
                <AiiTable
                    rowKey="numberId"
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    pagination={pagination}
                    toolbar={toolbar}
                    operations={(record) => [
                        'EDIT',
                        'DETAIL',
                        record.status === 0
                            ? {
                                  key: 'ENABLE',
                                  label: t('Action.Enable'),
                                  icon: <CheckOne />,
                              }
                            : {
                                  key: 'DISABLE',
                                  label: t('Action.Disable'),
                                  danger: true,
                                  icon: <Forbid />,
                              },
                        'DELETE',
                    ]}
                    onPageSizeChange={onPageSizeChange}
                    onPageChange={onPageChange}
                    onOperationClick={onOperationClick}
                />
            </div>
            {modalTypeEnums?.length &&
                modalTypeEnums.map((type) => (
                    <Modal key={type} open={modal[type].isOpen} {...modal[type].modalOptions}>
                        {modal[type].modalOptions.content}
                    </Modal>
                ))}
        </div>
    )
}
