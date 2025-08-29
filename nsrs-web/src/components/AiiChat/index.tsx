import { memo, useEffect, useRef, useState } from 'react'

import OpenAI from 'openai'
import remarkGfm from 'remark-gfm'
import type { GetProp } from 'antd'
import { Button, Space, Spin } from 'antd'
import ReactMarkdown from 'react-markdown'
import { useTranslation } from 'react-i18next'
import type { Components } from 'react-markdown'
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'

import { useThemeStore } from '@/stores/system'
import Robot from '@/assets/svg/robot.svg?react'
import { Bubble, Sender, useXAgent, useXChat } from '@ant-design/x'
import { Check, Copy, Down, Send, TwoEllipses } from '@icon-park/react'
import { materialDark, materialLight } from 'react-syntax-highlighter/dist/esm/styles/prism'

import type { BubbleItemProps, CodeBlockProps } from './AiiChat.types'

import './index.css'

const client = new OpenAI({
    baseURL: import.meta.env.VITE_ALIYUN_AI_URL,
    apiKey: import.meta.env.VITE_ALIYUN_AI_KEY,
    dangerouslyAllowBrowser: true,
})

const AIModel = import.meta.env.VITE_ALIYUN_AI_MODEL
const AIThinkModel = import.meta.env.VITE_ALIYUN_AI_MODEL_THINK

const CodeBlock: React.FC<CodeBlockProps> = ({ value, language }) => {
    const { theme } = useThemeStore()
    const [copied, setCopied] = useState(false)
    const { t } = useTranslation()

    const handleCopyCode = async () => {
        try {
            await navigator.clipboard.writeText(value)
            setCopied(true)
            setTimeout(() => setCopied(false), 2000)
        } catch (error) {
            console.error('Failed to copy text: ', error)
        }
    }

    return (
        <div className="code-block-wrapper">
            <div className="code-block-header">
                <span className="text-sm">{language || 'Code'}</span>
                <span className="flex items-center text-sm gap-4 cursor-pointer" onClick={handleCopyCode}>
                    {copied ? <Check size={14} /> : <Copy size={14} />}
                    {copied ? t('Common.Copied') : t('Action.Copy')}
                </span>
            </div>
            <SyntaxHighlighter
                language={language}
                style={theme === 'dark' ? materialDark : materialLight}
                customStyle={{
                    margin: 0,
                    padding: '16px',
                    fontSize: '14px',
                }}
                showLineNumbers={value.includes('\n')}
            >
                {value.trim()}
            </SyntaxHighlighter>
        </div>
    )
}

const MarkdownComponents: Components = {
    code({ className, children }) {
        const content = String(children).trim()

        const isInline = !className || !className.includes('language-')

        if (isInline || content.split('\n').length === 1) {
            return <code className="primary-text-btn p-2">{content}</code>
        }

        const match = /language-(\w+)/.exec(className || '')
        return <CodeBlock value={content} language={match?.[1]} />
    },
    pre({ children }) {
        return <>{children}</>
    },
    a({ href, children, ...props }) {
        return (
            <a
                href={href}
                target="_blank"
                rel="noopener noreferrer"
                className="primary-text-btn hover:underline"
                {...props}
            >
                {children}
            </a>
        )
    },
    table: ({ children }) => <table className="w-full border-collapse">{children}</table>,
    thead: ({ children }) => <thead className="bg-gray-100/50 dark:bg-gray-800/50">{children}</thead>,
    tbody: ({ children }) => <tbody className="divide-y divide-gray-200 dark:divide-gray-700">{children}</tbody>,
    tr: ({ children }) => <tr className="hover:bg-gray-50 dark:hover:bg-gray-800/20">{children}</tr>,
    th: ({ children }) => (
        <th className="px-4 py-2 text-left font-semibold text-sm bg-gray-50 dark:bg-gray-900/50 border-b">
            {children}
        </th>
    ),
    td: ({ children }) => (
        <td className="px-4 py-2 text-sm border-b hover:bg-gray-50 dark:hover:bg-gray-800/20">{children}</td>
    ),
}

const BubbleItem: React.FC<BubbleItemProps> = memo(({ message, id, status, interruptedMessages }) => {
    const { t } = useTranslation()
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)
    const thinkingTimeRef = useRef(0)

    useEffect(() => {
        if (status === 'local') return
        if (interruptedMessages.length > 0) {
            if (timerRef.current) {
                clearInterval(timerRef.current)
                thinkingTimeRef.current = 0
                return
            }
        }
        if (message.thinkingContent && !message.msg) {
            timerRef.current = setInterval(() => {
                thinkingTimeRef.current += 1
            }, 1000)
        }
        if (message.msg) {
            timerRef.current && clearInterval(timerRef.current)
        }
        return () => {
            if (message.msg) {
                timerRef.current && clearInterval(timerRef.current)
            }
        }
    }, [status, message.msg, interruptedMessages])

    return (
        <>
            {!['local'].includes(status) && message.thinkingContent && (
                <>
                    <Button
                        color="default"
                        variant="filled"
                        icon={<TwoEllipses />}
                        onClick={() => {
                            const wrapper = document.querySelector(`.think-wrapper-${id}`)
                            wrapper?.classList.toggle('hidden')
                        }}
                    >
                        {interruptedMessages.length
                            ? t('AI.Thinking_Stopped')
                            : message.msg
                              ? t('AI.Thought', { time: thinkingTimeRef.current })
                              : t('AI.Thinking')}
                        <Down />
                    </Button>
                    <div
                        className={`think-wrapper-${id} border-l-2 text-light-colorFill border-light-colorPrimary pl-10 my-10`}
                    >
                        {message.thinkingContent}
                    </div>
                </>
            )}
            {message.msg ? (
                <ReactMarkdown remarkPlugins={[remarkGfm]} components={MarkdownComponents}>
                    {message.msg}
                </ReactMarkdown>
            ) : (
                !interruptedMessages.length && <Spin size="small" />
            )}
        </>
    )
})

const AiiChat: React.FC = () => {
    const { t } = useTranslation()
    const [content, setContent] = useState<string>('')
    const [isThinking, setIsThinking] = useState<boolean>(false)
    const [model, setModel] = useState<string>('')
    const isThinkingRef = useRef(isThinking)
    const modelRef = useRef(model)
    const abortRef = useRef(() => {})
    const [interruptedMessages, setInterruptedMessages] = useState<OpenAI.ChatCompletionMessageParam[]>([])

    const [agent] = useXAgent({
        request: async (info, callbacks) => {
            const abortController = new AbortController()
            abortRef.current = () => {
                abortController.abort()
                console.log(formattedMessages)
                setInterruptedMessages(formattedMessages)
            }
            const { messages = [], message } = info
            const { onSuccess, onUpdate, onError } = callbacks

            // 将历史消息作为上下文传递给下一次请求
            const formattedMessages: OpenAI.ChatCompletionMessageParam[] = messages
                .filter((msg) => msg !== message)
                .map((msg) => ({
                    role: 'assistant',
                    content: msg.match(/THINKINGMSG:([\s\S]*?),MSG:([\s\S]*)/)?.[2] || '',
                }))

            if (message) {
                formattedMessages.push({ role: 'user', content: message })
            }

            try {
                const stream = await client.chat.completions.create(
                    {
                        model: modelRef.current,
                        messages: formattedMessages,
                        web_search_options: {},
                        stream: true,
                    },
                    {
                        signal: abortController.signal,
                    },
                )

                let content = ''
                let reasoningContent = ''

                for await (const chunk of stream) {
                    const delta = chunk.choices[0]?.delta
                    content += delta?.content || ''
                    if (delta && 'reasoning_content' in delta) {
                        reasoningContent += delta.reasoning_content || ''
                    }
                    onUpdate({ data: `THINKINGMSG:${reasoningContent || ''},MSG:${content || ''}` })
                }
                onSuccess([{ data: `THINKINGMSG:${reasoningContent},MSG:${content}` }])
            } catch (error) {
                const err = error instanceof Error ? error : new Error('Unknown error')
                onError(err)
            }
        },
    })

    const { onRequest, setMessages, parsedMessages } = useXChat({
        agent,
        // requestPlaceholder: 'Waiting...',
        requestFallback: (msg, { error }) => {
            const errorMsg = error instanceof Error ? error.message : String(error)
            return errorMsg
        },
        parser: (content) => {
            let thinkingContent = ''
            let msg = ''
            const match = content.match(/THINKINGMSG:([\s\S]*?),MSG:([\s\S]*)/)
            if (match) {
                thinkingContent = match[1]
                msg = match[2]
            } else {
                msg = content
            }
            return { thinkingContent, msg }
        },
    })

    // 刷新中断的请求
    const continueRequest = async (id: string | number) => {
        console.log(interruptedMessages)
        //todo
        try {
            const stream = await client.chat.completions.create({
                model,
                messages: interruptedMessages,
                stream: true,
            })

            let regenerateContent = ''
            let regenerateThinkingContent = ''
            for await (const chunk of stream) {
                const delta = chunk.choices[0]?.delta
                regenerateContent += delta?.content || ''
                if (delta && 'reasoning_content' in delta) {
                    regenerateThinkingContent += delta.reasoning_content || ''
                }
                setMessages((prev) =>
                    prev.map((msg) =>
                        msg.id === id
                            ? {
                                  ...msg,
                                  message: `THINKINGMSG:${regenerateThinkingContent || ''},MSG:${regenerateContent || ''}`,
                                  status: 'success',
                              }
                            : msg,
                    ),
                )
            }
            setInterruptedMessages([])
        } catch (error) {
            console.error('Failed to continue request:', error)
        }
    }

    const roles: GetProp<typeof Bubble.List, 'roles'> = {
        assistant: {
            placement: 'start',
            avatar: <Robot className="w-54 h-54" />,
            typing: { step: 5, interval: 20 },
            style: { maxWidth: 640 },
            classNames: {
                content: '!bg-light-colorPrimaryBg dark:!bg-dark-colorBorderSecondary',
            },
        },
        user: {
            placement: 'end',
            classNames: {
                content: '!bg-light-colorPrimaryBg dark:!bg-dark-colorBorderSecondary',
            },
        },
    }

    const bubbleItems = parsedMessages.map(({ message, id, status }) => ({
        key: id,
        content: <BubbleItem message={message} id={id} status={status} interruptedMessages={interruptedMessages} />,
        role: status === 'local' ? 'user' : 'assistant',
        autoScroll: true,
        // footer: (
        //   <div>
        //     {status === 'local' ? (
        //       <Space>
        //         <Button size="small" type="text" icon={<Pencil size={14} />} />
        //         <Button size="small" type="text" icon={<Copy size={14} />} />
        //       </Space>
        //     ) : (
        //       <Space>
        //         <Button size="small" type="text" icon={<Refresh size={14} />} onClick={() => continueRequest(id)} />
        //         <Button size="small" type="text" icon={<Copy size={14} />} />
        //       </Space>
        //     )}
        //   </div>
        // ),
    }))

    useEffect(() => {
        return () => {
            abortRef.current()
        }
    }, [])

    useEffect(() => {
        modelRef.current = model
    }, [model])

    useEffect(() => {
        isThinkingRef.current = isThinking
        isThinking ? setModel(AIThinkModel) : setModel(AIModel)
    }, [isThinking])

    return (
        <div className="aii-chat">
            <Bubble.List
                style={{ maxHeight: 'calc(100% - 160px)', overflowY: 'auto' }}
                roles={roles}
                items={bubbleItems}
            />
            <Sender
                className="sender"
                classNames={{
                    input: 'sender-content',
                    prefix: 'sender-prefix',
                    actions: 'sender-actions',
                }}
                loading={agent.isRequesting()}
                value={content}
                onChange={setContent}
                onSubmit={(nextContent) => {
                    onRequest(nextContent)
                    setContent('')
                }}
                onCancel={() => {
                    abortRef.current()
                }}
                actions={(_, info) => {
                    const { SendButton, LoadingButton } = info.components
                    return (
                        <div className="w-full flex justify-between items-center">
                            <Space>
                                <Button
                                    shape="round"
                                    color={isThinking ? 'primary' : 'default'}
                                    variant={isThinking ? 'filled' : 'outlined'}
                                    onClick={() => setIsThinking(!isThinking)}
                                    icon={<TwoEllipses />}
                                >
                                    {t('AI.Think')}
                                </Button>
                            </Space>
                            {agent.isRequesting() ? (
                                <LoadingButton type="default" />
                            ) : (
                                <SendButton type="primary" icon={<Send />} />
                            )}
                        </div>
                    )
                }}
            />
        </div>
    )
}

export default AiiChat
