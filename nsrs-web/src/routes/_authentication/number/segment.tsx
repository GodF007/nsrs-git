import { useEffect, useState } from 'react'

import { useTranslation } from 'react-i18next'
import { Col, Form, Input, Modal, Row, Select } from 'antd'
import type { FormInstance, SelectProps, TableColumnsType } from 'antd'

import { client } from '@/utils/fetch'
import useTable from '@/hooks/table.hooks'
import AiiTable from '@/components/AiiTable'
import AiiSearch from '@/components/AiiSearch'
import { useModal } from '@/hooks/modal.hooks'
import type { components } from '@/interface/api'
import { createFileRoute } from '@tanstack/react-router'
import { CheckOne, Forbid, Newlybuild } from '@icon-park/react'

export const Route = createFileRoute('/_authentication/number/segment')({
    component: RouteComponent,
    staticData: {
        code: 'Number_Segment',
        langCode: 'Menu.Number_Segment',
    },
})

const modalTypeEnums: string[] = ['segment']

const SegmentForm = (props: {
    form: FormInstance
    operation: 'create' | 'update' | 'detail'
    segmentTypeEnums: SelectProps['options']
    regionEnums: SelectProps['options']
    hlrSwitchEnums: SelectProps['options']
}) => {
    const { t } = useTranslation()
    return (
        <Form
            form={props.form}
            layout="vertical"
            initialValues={{ needApproval: 0 }}
            disabled={props.operation === 'detail'}
        >
            <Row gutter={16}>
                <Form.Item name="segmentId" hidden>
                    <Input />
                </Form.Item>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number_Segment_Code')}
                        name="segmentCode"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number_Segment_Type')}
                        name="segmentType"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Input')} allowClear options={props.segmentTypeEnums} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Hlr_Switch')}
                        name="hlrSwitchId"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Input')} allowClear options={props.hlrSwitchEnums} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Region')}
                        name="regionId"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Input')} allowClear options={props.regionEnums} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Starting_Number')}
                        name="startNumber"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Ending_Number')}
                        name="endNumber"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
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
    const segmentTypeEnums: SelectProps['options'] = [
        {
            label: 'PSTN',
            value: 1,
        },
        {
            label: 'Mobile',
            value: 2,
        },
        {
            label: 'FTTH',
            value: 3,
        },
        {
            label: 'SIP',
            value: 4,
        },
        {
            label: 'VSAT',
            value: 5,
        },
    ]

    const [regionEnums, setRegionEnums] = useState<SelectProps['options']>([])
    const [hlrSwitchEnums, setHlrSwitchEnums] = useState<SelectProps['options']>([])

    const { queryTableData, dataSource, loading, onPageChange, onPageSizeChange, pagination, onSearch } =
        useTable<components['schemas']['NumberSegment']>('/msisdn/numberSegment/page')

    const searchItems = [
        <Form.Item name="segmentCode">
            <Input placeholder={t('Number_Resource.Number_Segment_Code')} allowClear />
        </Form.Item>,
        <Form.Item name="segmentType">
            <Select placeholder={t('Number_Resource.Number_Segment_Type')} allowClear options={segmentTypeEnums} />
        </Form.Item>,
        <Form.Item name="regionId">
            <Select placeholder={t('Number_Resource.Region')} allowClear />
        </Form.Item>,
    ]

    const columns: TableColumnsType<components['schemas']['NumberSegment']> = [
        {
            title: t('Number_Resource.Number_Segment_Code'),
            dataIndex: 'segmentCode',
            key: 'segmentCode',
            fixed: 'left',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Number_Segment_Type'),
            dataIndex: 'segmentType',
            key: 'segmentType',
            render: (segmentType) => {
                return segmentTypeEnums.find((item) => item.value === segmentType)?.label
            },
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Starting_Number'),
            dataIndex: 'startNumber',
            key: 'startNumber',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Ending_Number'),
            dataIndex: 'endNumber',
            key: 'endNumber',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Total'),
            dataIndex: 'totalQty',
            key: 'totalQty',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Idle_Count'),
            dataIndex: 'idleQty',
            key: 'idleQty',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Utilization_Rate'),
            dataIndex: 'utilizationRate',
            key: 'utilizationRate',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Region'),
            dataIndex: 'regionId',
            key: 'regionId',
            render: (regionId) => {
                return regionEnums?.find((item) => item.value === regionId)?.label || '-'
            },
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
    ]

    const modal = useModal(modalTypeEnums)
    const [levelForm] = Form.useForm()

    const toolbar = [
        {
            icon: <Newlybuild />,
            label: t('Action.Create'),
            onClick: () => {
                modal.segment.openModal({
                    title: t('Action.Create'),
                    content: (
                        <SegmentForm
                            form={levelForm}
                            operation="create"
                            segmentTypeEnums={segmentTypeEnums}
                            regionEnums={regionEnums}
                            hlrSwitchEnums={hlrSwitchEnums}
                        />
                    ),
                    onOk: () => {
                        levelForm.validateFields().then(async (values) => {
                            const { data } = await client.POST('/msisdn/numberSegment/add', {
                                body: values,
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Create_Success'))
                                onSearch()
                                levelForm.resetFields()
                                modal.segment.closeModal()
                            }
                        })
                    },
                    onCancel: () => {
                        levelForm.resetFields()
                        modal.segment.closeModal()
                    },
                })
            },
        },
    ]

    const onOperationClick = (key: string, record: any) => {
        switch (key) {
            case 'EDIT':
                levelForm.setFieldsValue(record)
                modal.segment.openModal({
                    title: t('Action.Edit'),
                    content: (
                        <SegmentForm
                            form={levelForm}
                            operation="update"
                            segmentTypeEnums={segmentTypeEnums}
                            regionEnums={regionEnums}
                            hlrSwitchEnums={hlrSwitchEnums}
                        />
                    ),
                    onOk: () => {
                        levelForm.validateFields().then(async (values) => {
                            const { data } = await client.PUT('/msisdn/numberSegment/update', {
                                body: values,
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Update_Success'))
                                onSearch()
                                levelForm.resetFields()
                                modal.segment.closeModal()
                            }
                        })
                    },
                    onCancel: () => {
                        levelForm.resetFields()
                        modal.segment.closeModal()
                    },
                })
                break
            case 'DETAIL':
                levelForm.setFieldsValue(record)
                modal.segment.openModal({
                    title: t('Action.Detail'),
                    content: (
                        <SegmentForm
                            form={levelForm}
                            operation="detail"
                            segmentTypeEnums={segmentTypeEnums}
                            regionEnums={regionEnums}
                            hlrSwitchEnums={hlrSwitchEnums}
                        />
                    ),
                    onOk: () => {
                        levelForm.resetFields()
                        modal.segment.closeModal()
                    },
                    onCancel: () => {
                        levelForm.resetFields()
                        modal.segment.closeModal()
                    },
                })
                break
            case 'DELETE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Delete_Tips'),
                    onOk: async () => {
                        const { data } = await client.DELETE('/msisdn/numberSegment/{segmentId}', {
                            params: {
                                path: {
                                    segmentId: record.segmentId,
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
                        const { data } = await client.PUT('/msisdn/numberSegment/enable/{segmentId}', {
                            params: {
                                path: {
                                    segmentId: record.segmentId,
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
                        const { data } = await client.PUT('/msisdn/numberSegment/disable/{segmentId}', {
                            params: {
                                path: {
                                    segmentId: record.segmentId,
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

    const queryEnums = async () => {
        const [regionRes, switchTypeRes] = await Promise.all([
            client.GET('/msisdn/region/listAllEnabled'),
            client.GET('/msisdn/hlrSwitch/listAllEnabled'),
        ])
        if (regionRes?.data?.success) {
            setRegionEnums(
                regionRes?.data?.data?.map((item) => ({
                    label: item.regionName,
                    value: item.regionId,
                })) ?? [],
            )
        }
        if (switchTypeRes?.data?.success) {
            setHlrSwitchEnums(
                switchTypeRes?.data?.data?.map((item) => ({
                    label: item.hlrName,
                    value: item.hlrId,
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
