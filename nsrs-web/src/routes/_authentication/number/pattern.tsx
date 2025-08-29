import { useEffect, useState } from 'react'

import { useTranslation } from 'react-i18next'
import type { FormInstance, TableColumnsType, SelectProps } from 'antd'
import { Col, Form, Input, Modal, Row, Select } from 'antd'

import { client } from '@/utils/fetch'
import useTable from '@/hooks/table.hooks'
import AiiTable from '@/components/AiiTable'
import AiiSearch from '@/components/AiiSearch'
import { useModal } from '@/hooks/modal.hooks'
import type { components } from '@/interface/api'
import { createFileRoute } from '@tanstack/react-router'
import { CheckOne, Forbid, Newlybuild } from '@icon-park/react'

export const Route = createFileRoute('/_authentication/number/pattern')({
    component: RouteComponent,
    staticData: {
        code: 'Number_Pattern',
        langCode: 'Menu.Number_Pattern',
    },
})

const modalTypeEnums: string[] = ['numberPattern']

const NumberPatternForm = (props: {
    form: FormInstance
    operation: 'create' | 'update' | 'detail'
    levelEnums: SelectProps['options']
}) => {
    const { t } = useTranslation()
    return (
        <Form form={props.form} layout="vertical" disabled={props.operation === 'detail'}>
            <Row gutter={16}>
                <Form.Item name="patternId" hidden>
                    <Input />
                </Form.Item>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Pattern_Name')}
                        name="patternName"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={t('Number_Resource.Level_Name')} name="levelId">
                        <Select placeholder={t('Common.Please_Input')} allowClear options={props.levelEnums} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Pattern_Format')}
                        name="patternFormat"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Regular_Expressions')}
                        name="expression"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
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
    const [levelEnums, setLevelEnums] = useState<SelectProps['options']>([])

    const { queryTableData, dataSource, loading, onPageChange, onPageSizeChange, pagination, onSearch } =
        useTable<components['schemas']['NumberPattern']>('/msisdn/numberPattern/page')

    const searchItems = [
        <Form.Item name="patternName">
            <Input placeholder={t('Number_Resource.Pattern_Name')} allowClear />
        </Form.Item>,
        <Form.Item name="levelId">
            <Select placeholder={t('Number_Resource.Level_Name')} allowClear options={levelEnums} />
        </Form.Item>,
        <Form.Item name="status">
            <Select placeholder={t('Common.Status')} allowClear options={statusEnums} />
        </Form.Item>,
    ]

    const columns: TableColumnsType<components['schemas']['NumberPattern']> = [
        {
            title: t('Number_Resource.Pattern_Name'),
            dataIndex: 'patternName',
            key: 'patternName',
            fixed: 'left',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Level_Name'),
            dataIndex: 'levelId',
            key: 'levelId',
            render: (levelId) => {
                return levelEnums?.find((item) => item.value === levelId)?.label || '-'
            },
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Pattern_Format'),
            dataIndex: 'patternFormat',
            key: 'patternFormat',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Regular_Expressions'),
            dataIndex: 'expression',
            key: 'expression',
            minWidth: 160,
        },
        {
            title: t('Common.Status'),
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                return statusEnums.find((item) => item.value === status)?.label
            },
            minWidth: 160,
        },
        {
            title: t('Common.Remark'),
            dataIndex: 'remark',
            key: 'remark',
            minWidth: 160,
        },
    ]

    const modal = useModal(modalTypeEnums)
    const [levelForm] = Form.useForm()

    const openModal = (operate: 'create' | 'update' | 'detail', record: any) => {
        const resetModal = () => {
            levelForm.resetFields()
            modal.numberPattern.closeModal()
        }
        if (operate !== 'create') {
            levelForm.setFieldsValue(record)
        }
        const operateType = operate.charAt(0).toUpperCase() + operate.slice(1)
        modal.numberPattern.openModal({
            title: t(`Action.${operateType}`),
            content: <NumberPatternForm form={levelForm} operation={operate} levelEnums={levelEnums} />,
            onOk: () => {
                if (operate === 'detail') {
                    resetModal()
                    return
                }
                levelForm.validateFields().then(async (values) => {
                    let res
                    if (operate === 'create') {
                        res = await client.POST('/msisdn/numberPattern/add', {
                            body: values,
                        })
                    }
                    if (operate === 'update') {
                        res = await client.PUT('/msisdn/numberPattern/update', {
                            body: values,
                        })
                    }

                    if (res?.data?.success) {
                        window.$message.success(t(`Message.${operateType}_Success`))
                        onSearch()
                        resetModal()
                    }
                })
            },
            onCancel: () => {
                resetModal()
            },
        })
    }

    const toolbar = [
        {
            icon: <Newlybuild />,
            label: t('Action.Create'),
            onClick: () => {
                openModal('create', {})
            },
        },
    ]

    const onOperationClick = (key: string, record: any) => {
        switch (key) {
            case 'EDIT':
                openModal('update', record)
                break
            case 'DETAIL':
                openModal('detail', record)
                break
            case 'DELETE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Delete_Tips'),
                    onOk: async () => {
                        const { data } = await client.DELETE('/msisdn/numberPattern/{patternId}', {
                            params: {
                                path: {
                                    patternId: record.patternId,
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
                        const { data } = await client.PUT('/msisdn/numberPattern/enable/{patternId}', {
                            params: {
                                path: {
                                    patternId: record.patternId,
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
                        const { data } = await client.PUT('/msisdn/numberPattern/disable/{patternId}', {
                            params: {
                                path: {
                                    patternId: record.patternId,
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

    const queryLevelEnums = async () => {
        const { data } = await client.GET('/msisdn/numberLevel/listAllEnabled')
        if (data?.success) {
            setLevelEnums(
                data.data?.map((item) => ({
                    label: item.levelName,
                    value: item.levelId,
                })),
            )
        }
    }

    useEffect(() => {
        queryLevelEnums()
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
