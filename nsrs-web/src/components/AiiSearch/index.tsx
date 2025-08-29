import { useState, useMemo, useCallback, memo } from 'react'
import { Col, Row, Form, Space, Button, Tooltip } from 'antd'
import { Down, Clear, Search } from '@icon-park/react'
import { useTranslation } from 'react-i18next'
import { motion, AnimatePresence } from 'framer-motion'
import classNames from 'classnames'

interface AiiSearchProps {
    items: React.ReactElement<typeof Form.Item>[]
    cols?: number
    onSearch?: (value: any) => void
    wrapper?: boolean
}

interface DynamicFieldsRowsProps {
    items: React.ReactElement<typeof Form.Item>[]
    expand: boolean
    Operations: React.FC
}

const DynamicFieldsRows: React.FC<DynamicFieldsRowsProps> = memo(({ items, expand, Operations }) => {
    const renderCols = useCallback(
        (start: number, end: number) =>
            items.slice(start, end).map((item, index) => (
                <Col key={start + index} span={6}>
                    {item}
                </Col>
            )),
        [items],
    )

    if (items.length < 4) {
        return (
            <Row gutter={24}>
                {renderCols(0, items.length)}
                <Operations />
            </Row>
        )
    }

    return (
        <>
            <Row gutter={24}>{renderCols(0, 4)}</Row>
            <AnimatePresence mode="wait">
                <motion.div
                    initial={false}
                    animate={expand ? { height: 'auto', opacity: 1 } : { height: 0, opacity: 0 }}
                    transition={{ duration: 0.3 }}
                    style={{
                        overflow: 'hidden',
                        pointerEvents: expand ? 'auto' : 'none',
                    }}
                >
                    <Row gutter={24}>{renderCols(4, items.length)}</Row>
                </motion.div>
            </AnimatePresence>
            <Operations />
        </>
    )
})

const AiiSearch: React.FC<AiiSearchProps> = (props) => {
    const { items, onSearch, wrapper = true } = props
    const { t } = useTranslation()
    const [form] = Form.useForm()
    const [expand, setExpand] = useState(false)

    const handleExpand = useCallback(() => setExpand((e) => !e), [])

    const Operations = useMemo(() => {
        return () => (
            <Col span={items.length < 4 ? 6 : 24}>
                <div className={items.length < 4 ? '' : 'text-right'}>
                    <Space size="small">
                        <Button icon={<Search />} type="primary" htmlType="submit">
                            {t('Action.Search')}
                        </Button>
                        <Button
                            icon={<Clear />}
                            onClick={() => {
                                form.resetFields()
                            }}
                        >
                            {t('Action.Reset')}
                        </Button>
                        {items.length > 4 && (
                            <Tooltip title={expand ? t('Action.Collapse') : t('Action.Expand')}>
                                <a
                                    className={`flex items-center hover:text-light-colorPrimary dark:hover:text-dark-colorPrimary transition-all duration-300 ease-in-out ${expand ? 'rotate-180' : 'rotate-0'}`}
                                    onClick={handleExpand}
                                >
                                    <Down />
                                </a>
                            </Tooltip>
                        )}
                    </Space>
                </div>
            </Col>
        )
    }, [items.length, t, expand, form, handleExpand])

    const onFinish = useCallback(
        (values: Record<string, any> = {}) => {
            const result = Object.keys(values).reduce(
                (acc, key) => {
                    acc[key] = values[key] === undefined ? '' : values[key]
                    return acc
                },
                {} as Record<string, any>,
            )
            onSearch && onSearch(result)
        },
        [onSearch],
    )

    return (
        <div className={classNames('mb-10', { 'pb-0': items.length < 4, wrapper: wrapper })}>
            <Form form={form} name="advanced_search" onFinish={onFinish}>
                <DynamicFieldsRows items={items} expand={expand} Operations={Operations} />
            </Form>
        </div>
    )
}
export default AiiSearch
