import { useEffect, useState } from 'react'

import { useTranslation } from 'react-i18next'
import { Col, Form, Input, Modal, Row, Select, Switch } from 'antd'
import type { FormInstance, SelectProps, TableColumnsType } from 'antd'

import { client } from '@/utils/fetch'
import useTable from '@/hooks/table.hooks'
import AiiTable from '@/components/AiiTable'
import AiiSearch from '@/components/AiiSearch'
import { useModal } from '@/hooks/modal.hooks'
import type { components } from '@/interface/api'
import { createFileRoute } from '@tanstack/react-router'
import { Newlybuild } from '@icon-park/react'
import { formatDate } from '@/utils/common'

export const Route = createFileRoute('/_authentication/sim-card/specification')({
    component: RouteComponent,
    staticData: {
        code: 'Sim_Card_Specification',
        langCode: 'Menu.Sim_Card_Specification',
    },
})

const modalTypeEnums: string[] = ['specification']

const SpecificationForm = (props: {
    form: FormInstance
    operation: 'create' | 'update' | 'detail'
    typeEnums: SelectProps['options']
}) => {
    const { t } = useTranslation()
    return (
        <Form
            form={props.form}
            initialValues={{
                status: 1,
            }}
            layout="vertical"
            disabled={props.operation === 'detail'}
        >
            <Row gutter={16}>
                <Form.Item name="specId" hidden>
                    <Input />
                </Form.Item>
                <Col span={12}>
                    <Form.Item
                        label={t('Sim_Card.Sim_Card_Specification_Name')}
                        name="specName"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Sim_Card.Sim_Card_Specification_Code')}
                        name="specCode"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Sim_Card.Sim_Card_Type_Name')}
                        name="typeId"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Input')} allowClear options={props.typeEnums} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label={t('Common.Status')} name="status" valuePropName="checked">
                        <Switch
                            checkedChildren={t('Enum.Enabled')}
                            unCheckedChildren={t('Enum.Disabled')}
                            onChange={(checked) => props.form.setFieldValue('status', checked ? 1 : 0)}
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
    const statusEnums: SelectProps['options'] = [
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
        useTable<components['schemas']['SimCardSpecification']>('/simcard/specification/page')

    const [typeEnums, setTypeEnums] = useState<SelectProps['options']>([])

    const searchItems = [
        <Form.Item name="specName">
            <Input placeholder={t('Sim_Card.Sim_Card_Specification_Name')} allowClear />
        </Form.Item>,
        <Form.Item name="specCode">
            <Input placeholder={t('Sim_Card.Sim_Card_Specification_Code')} allowClear />
        </Form.Item>,
        <Form.Item name="typeId">
            <Select placeholder={t('Sim_Card.Sim_Card_Type_Name')} allowClear options={typeEnums} />
        </Form.Item>,
        <Form.Item name="status">
            <Select placeholder={t('Common.Status')} allowClear options={statusEnums} />
        </Form.Item>,
    ]

    const columns: TableColumnsType<components['schemas']['SimCardSpecification']> = [
        {
            title: t('Sim_Card.Sim_Card_Specification_Name'),
            dataIndex: 'specName',
            key: 'specName',
            fixed: 'left',
            minWidth: 160,
        },
        {
            title: t('Sim_Card.Sim_Card_Specification_Code'),
            dataIndex: 'specCode',
            key: 'specCode',
            minWidth: 160,
        },
        {
            title: t('Sim_Card.Sim_Card_Type_Name'),
            dataIndex: 'typeId',
            key: 'typeId',
            render: (typeId) => {
                return typeEnums?.find((item) => item.value === typeId)?.label || '-'
            },
            minWidth: 160,
        },
        {
            title: t('Common.Status'),
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                return statusEnums.find((item) => item.value === status)?.label || '-'
            },
            minWidth: 160,
        },
        {
            title: t('Common.Remark'),
            dataIndex: 'description',
            key: 'description',
            minWidth: 160,
        },
        {
            title: t('Common.Create_Time'),
            dataIndex: 'createTime',
            key: 'createTime',
            render: (createTime) => {
                return createTime ? formatDate(createTime) : '-'
            },
            minWidth: 160,
        },
        {
            title: t('Common.Update_Time'),
            dataIndex: 'updateTime',
            key: 'updateTime',
            render: (updateTime) => {
                return updateTime ? formatDate(updateTime) : '-'
            },
            minWidth: 160,
        },
    ]

    const modal = useModal(modalTypeEnums)
    const [specificationForm] = Form.useForm()

    const openModal = (operate: 'create' | 'update' | 'detail', record: any) => {
        const resetModal = () => {
            specificationForm.resetFields()
            modal.specification.closeModal()
        }
        if (operate !== 'create') {
            specificationForm.setFieldsValue(record)
        }
        const operateType = operate.charAt(0).toUpperCase() + operate.slice(1)
        modal.specification.openModal({
            title: t(`Action.${operateType}`),
            content: <SpecificationForm form={specificationForm} operation={operate} typeEnums={typeEnums} />,
            onOk: () => {
                if (operate === 'detail') {
                    resetModal()
                    return
                }
                specificationForm.validateFields().then(async (values) => {
                    let res
                    if (operate === 'create') {
                        res = await client.POST('/simcard/specification/add', {
                            body: values,
                        })
                    }
                    if (operate === 'update') {
                        res = await client.PUT('/simcard/specification/update', {
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
                        const { data } = await client.DELETE('/simcard/specification/{id}', {
                            params: {
                                path: {
                                    id: record.specId,
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
            default:
                break
        }
    }

    const queryEnums = async () => {
        const { data } = await client.GET('/simcard/type/list')
        if (data?.success) {
            setTypeEnums(
                data.data?.map((item) => ({
                    label: item.typeName,
                    value: item.typeId,
                })) ?? [],
            )
        }
    }

    useEffect(() => {
        queryEnums()
        queryTableData()
    }, [])

    return (
        <div>
            <AiiSearch items={searchItems} onSearch={onSearch} />
            <div className="wrapper">
                <AiiTable
                    rowKey="specId"
                    loading={loading}
                    columns={columns}
                    dataSource={dataSource}
                    pagination={pagination}
                    toolbar={toolbar}
                    operations={['EDIT', 'DETAIL', 'DELETE']}
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
