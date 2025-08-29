import { useEffect, useState } from 'react'

import { useTranslation } from 'react-i18next'
import { Col, Form, Input, Modal, Row, Select, TreeSelect } from 'antd'
import type { FormInstance, SelectProps, TableColumnsType } from 'antd'

import { client } from '@/utils/fetch'
import useTable from '@/hooks/table.hooks'
import { formatDate } from '@/utils/common'
import AiiTable from '@/components/AiiTable'
import AiiSearch from '@/components/AiiSearch'
import { useModal } from '@/hooks/modal.hooks'
import type { components } from '@/interface/api'
import { createFileRoute } from '@tanstack/react-router'
import { CheckOne, Forbid, Newlybuild } from '@icon-park/react'

export const Route = createFileRoute('/_authentication/number/region')({
    component: RouteComponent,
    staticData: {
        code: 'Number_Region',
        langCode: 'Menu.Number_Region',
    },
})

const modalTypeEnums: string[] = ['region']

const RegionForm = (props: {
    form: FormInstance
    operation: 'create' | 'update' | 'detail'
    treeData: components['schemas']['Region'][]
    regionTypeEnums: SelectProps['options']
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
                <Form.Item name="regionId" hidden>
                    <Input />
                </Form.Item>
                <Col span={12}>
                    <Form.Item label={t('Number_Resource.Parent_Region')} name="parentId">
                        <TreeSelect
                            placeholder={t('Common.Please_Input')}
                            allowClear
                            treeData={props.treeData}
                            fieldNames={{ label: 'regionName', value: 'regionId', children: 'children' }}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Region_Code')}
                        name="regionCode"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Region_Name')}
                        name="regionName"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Input placeholder={t('Common.Please_Input')} allowClear />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        label={t('Number_Resource.Region_Type')}
                        name="regionType"
                        rules={[{ required: true, message: t('Common.Required') }]}
                    >
                        <Select placeholder={t('Common.Please_Input')} allowClear options={props.regionTypeEnums} />
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
    const regionTypeEnums = [
        {
            label: t('Enum.Country'),
            value: 1,
        },
        {
            label: t('Enum.Province'),
            value: 2,
        },
        {
            label: t('Enum.City'),
            value: 3,
        },
        {
            label: t('Enum.County'),
            value: 4,
        },
    ]

    const { queryTableData, dataSource, loading, onPageChange, onPageSizeChange, pagination, onSearch } =
        useTable<components['schemas']['Region']>('/msisdn/region/page')

    const [treeData, setTreeData] = useState<components['schemas']['Region'][]>([])
    const [flatTreeData, setFlatTreeData] = useState<components['schemas']['Region'][]>([])

    const searchItems = [
        <Form.Item name="regionName">
            <Input placeholder={t('Number_Resource.Region_Name')} allowClear />
        </Form.Item>,
        <Form.Item name="regionType">
            <Select placeholder={t('Number_Resource.Region_Type')} allowClear options={regionTypeEnums} />
        </Form.Item>,
    ]

    const columns: TableColumnsType<components['schemas']['NumberLevel']> = [
        {
            title: t('Number_Resource.Region_Code'),
            dataIndex: 'regionCode',
            key: 'regionCode',
            fixed: 'left',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Region_Name'),
            dataIndex: 'regionName',
            key: 'regionName',
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Region_Type'),
            dataIndex: 'regionType',
            key: 'regionType',
            render: (regionType) => {
                return regionTypeEnums.find((item) => item.value === regionType)?.label
            },
            minWidth: 160,
        },
        {
            title: t('Number_Resource.Parent_Region'),
            dataIndex: 'parentId',
            key: 'parentId',
            render: (parentId) => {
                return flatTreeData.find((item) => item.regionId === parentId)?.regionName || '-'
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
        {
            title: t('Common.Create_Time'),
            dataIndex: 'createTime',
            key: 'createTime',
            minWidth: 160,
            render: (createTime) => {
                return formatDate(createTime)
            },
        },
    ]

    const modal = useModal(modalTypeEnums)
    const [regionForm] = Form.useForm()

    const toolbar = [
        {
            icon: <Newlybuild />,
            label: t('Action.Create'),
            onClick: () => {
                queryTreeData()
                modal.region.openModal({
                    title: t('Action.Create'),
                    content: (
                        <RegionForm
                            form={regionForm}
                            operation="create"
                            treeData={treeData}
                            regionTypeEnums={regionTypeEnums}
                        />
                    ),
                    onOk: () => {
                        regionForm.validateFields().then(async (values) => {
                            const { data } = await client.POST('/msisdn/region/add', {
                                body: values,
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Create_Success'))
                                onSearch()
                                regionForm.resetFields()
                                modal.region.closeModal()
                            }
                        })
                    },
                    onCancel: () => {
                        regionForm.resetFields()
                        modal.region.closeModal()
                    },
                })
            },
        },
    ]

    const onOperationClick = (key: string, record: any) => {
        switch (key) {
            case 'EDIT':
                queryTreeData()
                regionForm.setFieldsValue(record)
                modal.region.openModal({
                    title: t('Action.Edit'),
                    content: (
                        <RegionForm
                            form={regionForm}
                            operation="update"
                            treeData={treeData}
                            regionTypeEnums={regionTypeEnums}
                        />
                    ),
                    onOk: () => {
                        regionForm.validateFields().then(async (values) => {
                            const { data } = await client.PUT('/msisdn/region/update', {
                                body: values,
                            })
                            if (data?.success) {
                                window.$message.success(t('Message.Update_Success'))
                                onSearch()
                                regionForm.resetFields()
                                modal.region.closeModal()
                            }
                        })
                    },
                    onCancel: () => {
                        regionForm.resetFields()
                        modal.region.closeModal()
                    },
                })
                break
            case 'DETAIL':
                queryTreeData()
                regionForm.setFieldsValue(record)
                modal.region.openModal({
                    title: t('Action.Detail'),
                    content: (
                        <RegionForm
                            form={regionForm}
                            operation="detail"
                            treeData={treeData}
                            regionTypeEnums={regionTypeEnums}
                        />
                    ),
                    onOk: () => {
                        regionForm.resetFields()
                        modal.region.closeModal()
                    },
                    onCancel: () => {
                        regionForm.resetFields()
                        modal.region.closeModal()
                    },
                })
                break
            case 'DELETE':
                window.$modal.confirm({
                    title: t('Common.Tips'),
                    content: t('Tips.Delete_Tips'),
                    onOk: async () => {
                        const { data } = await client.DELETE('/msisdn/region/{regionId}', {
                            params: {
                                path: {
                                    regionId: record.regionId,
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
                        const { data } = await client.PUT('/msisdn/region/enable/{regionId}', {
                            params: {
                                path: {
                                    regionId: record.regionId,
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
                        const { data } = await client.PUT('/msisdn/region/disable/{regionId}', {
                            params: {
                                path: {
                                    regionId: record.regionId,
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

    const flattenTree = (tree: components['schemas']['Region'][]) => {
        const result: components['schemas']['Region'][] = []
        function traverse(nodes: components['schemas']['Region'][]) {
            nodes?.forEach((node) => {
                result.push(node)
                if (node.children && node.children.length) {
                    traverse(node.children)
                }
            })
        }
        traverse(tree)
        return result
    }

    const queryTreeData = async () => {
        const { data } = await client.GET('/msisdn/region/tree')
        if (data?.success) {
            setTreeData(data.data ?? [])
            setFlatTreeData(flattenTree(data.data ?? []))
        }
    }

    useEffect(() => {
        queryTreeData()
        queryTableData()
    }, [])

    return (
        <div>
            <AiiSearch items={searchItems} onSearch={onSearch} />
            <div className="wrapper">
                <AiiTable
                    rowKey="regionId"
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
