import { useEffect, useState } from 'react'

import { useTranslation } from 'react-i18next'
import { Col, Form, Input, Modal, Row, Select } from 'antd'
import type { FormInstance, SelectProps, TableColumnsType, TableProps } from 'antd'

import { client } from '@/utils/fetch'
import useTable from '@/hooks/table.hooks'
import AiiTable from '@/components/AiiTable'
import AiiSearch from '@/components/AiiSearch'
import { useModal } from '@/hooks/modal.hooks'
import type { components } from '@/interface/api'
import { createFileRoute } from '@tanstack/react-router'
import { DownloadOne, Newlybuild, UploadOne } from '@icon-park/react'

export const Route = createFileRoute('/_authentication/number/resource')({
    component: RouteComponent,
    staticData: {
        code: 'Number_Resource',
        langCode: 'Menu.Number_Resource',
    },
})

type TableRowSelection<T extends object = object> = TableProps<T>['rowSelection']

const modalTypeEnums: string[] = ['number']

const NumberForm = (props: {
    form: FormInstance
    operation: 'create' | 'update' | 'detail'
    numberTypeOptions: SelectProps['options']
    levelOptions: SelectProps['options']
    patternOptions: SelectProps['options']
    hlrOptions: SelectProps['options']
    segmentOptions: SelectProps['options']
}) => {
    const { t } = useTranslation()
    return (
        <Form form={props.form} layout="vertical" disabled={props.operation === 'detail'}>
            <Row gutter={16}>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number')}
                        name="number"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input
                            placeholder={t('Common.Please_Input')}
                            disabled={props.operation !== 'create'}
                            allowClear
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number_Type')}
                        name="numberType"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Select')} options={props.numberTypeOptions} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number_Segment')}
                        name="numberSegment"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Select')} options={props.segmentOptions} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number_Level')}
                        name="numberLevel"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Select')} options={props.levelOptions} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Number_Pattern')}
                        name="numberPattern"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Select')} options={props.patternOptions} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Hlr_Switch')}
                        name="hlrSwitch"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Select')} options={props.hlrOptions} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Attributive_Org')}
                        name="attributiveOrg"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Select')} />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label="ICCID" name="iccid" rules={[{ required: true, message: t('Common.Required') }]}>
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={24}>
                    <Form.Item label={t('Common.Remark')} name="remark">
                        <Input.TextArea placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
            </Row>
        </Form>
    )
}

function RouteComponent() {
    const { t } = useTranslation()
    const {
        queryTableData,
        dataSource,
        loading,
        onPageChange,
        onPageSizeChange,
        pagination,
        selectedRowKeys,
        selectedRows,
        onSelectChange,
        onSearch,
    } = useTable<components['schemas']['NumberResourceVO']>('/msisdn/number/page')

    const [levelOptions, setLevelOptions] = useState<SelectProps['options']>([])
    const [patternOptions, setPatternOptions] = useState<SelectProps['options']>([])
    const [hlrOptions, setHlrOptions] = useState<SelectProps['options']>([])
    const [segmentOptions, setSegmentOptions] = useState<SelectProps['options']>([])
    const [numberTypeOptions, setNumberTypeOptions] = useState<SelectProps['options']>([
        {
            label: '手机号码',
            value: 1,
        },
        {
            label: '固定电话',
            value: 2,
        },
        {
            label: '虚拟号码',
            value: 3,
        },
    ])

    const searchItems = [
        <Form.Item name="number">
            <Input placeholder={t('Number_Resource.Number')} allowClear />
        </Form.Item>,
        <Form.Item name="numberType">
            <Select placeholder={t('Number_Resource.Number_Type')} options={numberTypeOptions} allowClear />
        </Form.Item>,
        <Form.Item name="numberLevel">
            <Select placeholder={t('Number_Resource.Number_Level')} options={levelOptions} allowClear />
        </Form.Item>,
        <Form.Item name="status">
            <Select placeholder={t('Common.Status')} allowClear />
        </Form.Item>,
        <Form.Item name="hlrSwitch">
            <Select placeholder={t('Number_Resource.Hlr_Switch')} options={hlrOptions} allowClear />
        </Form.Item>,
        <Form.Item name="attributiveOrg">
            <Input placeholder={t('Number_Resource.Attributive_Org')} allowClear />
        </Form.Item>,
    ]
    const columns: TableColumnsType<components['schemas']['NumberResourceVO']> = [
        {
            title: t('Number_Resource.Number'),
            dataIndex: 'number',
            key: 'number',
            fixed: 'left',
        },
        {
            title: t('Number_Resource.Number_Type'),
            dataIndex: 'numberType',
            key: 'numberType',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Number_Level'),
            dataIndex: 'levelId',
            key: 'levelId',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Number_Pattern'),
            dataIndex: 'patternId',
            key: 'patternId',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Hlr_Switch'),
            dataIndex: 'hlrId',
            key: 'hlrSwitch',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Attributive_Org'),
            dataIndex: 'attributiveOrg',
            key: 'attributiveOrg',
            minWidth: 200,
        },
        {
            title: t('Common.Status'),
            dataIndex: 'statusName',
            key: 'status',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Charge'),
            dataIndex: 'charge',
            key: 'charge',
            minWidth: 160,
        },
        {
            title: 'ICCID',
            dataIndex: 'iccid',
            key: 'iccid',
            minWidth: 160,
        },
        {
            title: t('Common.Remark'),
            dataIndex: 'remark',
            key: 'remark',
            minWidth: 160,
        },
        {
            title: t('Common.Create_Time'),
            dataIndex: 'createTime',
            key: 'createTime',
        },
    ]

    const queryDicts = async () => {
        const [levelsRes, segmentsRes, patternsRes, hlrRes] = await Promise.all([
            client.GET('/msisdn/numberLevel/listAllEnabled'),
            client.GET('/msisdn/numberSegment/listAllEnabled'),
            client.GET('/msisdn/numberPattern/listAllEnabled'),
            client.GET('/msisdn/hlrSwitch/listAllEnabled'),
        ])
        console.log('levelsRes', levelsRes)
        if (levelsRes.data?.success) {
            setLevelOptions(
                levelsRes.data?.data?.map((item) => ({
                    label: item.levelName,
                    value: item.levelId,
                })),
            )
        }
        if (segmentsRes.data?.success) {
            setSegmentOptions(
                segmentsRes.data?.data?.map((item) => ({
                    label: item.segmentCode,
                    value: item.segmentId,
                })),
            )
        }
        if (patternsRes.data?.success) {
            setPatternOptions(
                patternsRes.data?.data?.map((item) => ({
                    label: item.patternName,
                    value: item.patternId,
                })),
            )
        }
        if (hlrRes.data?.success) {
            setHlrOptions(
                hlrRes.data?.data?.map((item) => ({
                    label: item.hlrName,
                    value: item.hlrId,
                })),
            )
        }
    }

    useEffect(() => {
        queryDicts()
        queryTableData({
            number: '',
            numberType: undefined,
            segmentId: undefined,
            levelId: undefined,
            patternId: undefined,
            hlrSwitchId: undefined,
            attributiveOrg: '',
            status: undefined,
        })
    }, [])

    const modal = useModal(modalTypeEnums)
    const [numberForm] = Form.useForm()

    const toolbar = [
        {
            icon: <Newlybuild />,
            label: t('Action.Create'),
            onClick: () => {
                modal.number.openModal({
                    title: '创建',
                    content: (
                        <NumberForm
                            form={numberForm}
                            operation="create"
                            numberTypeOptions={numberTypeOptions}
                            levelOptions={levelOptions}
                            patternOptions={patternOptions}
                            hlrOptions={hlrOptions}
                            segmentOptions={segmentOptions}
                        />
                    ),
                    onOk: () => {
                        numberForm.validateFields().then(async (values) => {
                            const { data } = await client.POST('/msisdn/number/add', {
                                body: {
                                    ...values,
                                },
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Create_Success'))
                                numberForm.resetFields()
                                modal.number.closeModal()
                                onSearch()
                            }
                        })
                    },
                    onCancel: () => {
                        numberForm.resetFields()
                        modal.number.closeModal()
                    },
                })
            },
        },
        {
            icon: <UploadOne />,
            label: t('Action.Import'),
            onClick: () => {},
        },
        {
            icon: <DownloadOne />,
            label: t('Action.Export'),
            onClick: async () => {
                await client.GET('/msisdn/number/export-excel', {
                    params: {
                        query: {
                            queryParams: {},
                        },
                    },
                    parseAs: 'blob',
                })
            },
        },
    ]

    const rowSelection: TableRowSelection<components['schemas']['NumberResourceVO']> = {
        selectedRowKeys,
        onChange: onSelectChange,
    }

    const onBatchDelete = () => {
        console.log('onBatchDelete', selectedRows)
    }

    const onBatchExport = () => {
        console.log('onBatchExport', selectedRows)
    }

    const onOperationClick = (key: string, record: any) => {
        switch (key) {
            case 'EDIT':
                numberForm.setFieldsValue(record)
                modal.number.openModal({
                    title: t('Action.Edit'),
                    content: (
                        <NumberForm
                            form={numberForm}
                            operation="update"
                            numberTypeOptions={numberTypeOptions}
                            levelOptions={levelOptions}
                            patternOptions={patternOptions}
                            hlrOptions={hlrOptions}
                            segmentOptions={segmentOptions}
                        />
                    ),
                    onOk: () => {
                        numberForm.validateFields().then(async (values) => {
                            const { data } = await client.PUT('/msisdn/number/update/{number}', {
                                params: {
                                    path: {
                                        number: record.number,
                                    },
                                },
                                body: {
                                    ...values,
                                },
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Update_Success'))
                                numberForm.resetFields()
                                modal.number.closeModal()
                                onSearch()
                            }
                        })
                    },
                    onCancel: () => {
                        numberForm.resetFields()
                        modal.number.closeModal()
                    },
                })
                break
            case 'DETAIL':
                numberForm.setFieldsValue(record)
                modal.number.openModal({
                    title: t('Action.Detail'),
                    content: (
                        <NumberForm
                            form={numberForm}
                            operation="detail"
                            numberTypeOptions={numberTypeOptions}
                            levelOptions={levelOptions}
                            patternOptions={patternOptions}
                            hlrOptions={hlrOptions}
                            segmentOptions={segmentOptions}
                        />
                    ),
                    onOk: () => {
                        numberForm.resetFields()
                        modal.number.closeModal()
                    },
                    onCancel: () => {
                        numberForm.resetFields()
                        modal.number.closeModal()
                    },
                })
                break
            case 'DELETE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Delete_Tips'),
                    onOk: async () => {
                        const { data } = await client.DELETE('/msisdn/number/delete/{number}', {
                            params: {
                                path: {
                                    number: record.number,
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
                    operations={['EDIT', 'DETAIL', 'DELETE']}
                    onPageSizeChange={onPageSizeChange}
                    onPageChange={onPageChange}
                    rowSelection={rowSelection}
                    onBatchDelete={onBatchDelete}
                    onBatchExport={onBatchExport}
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
