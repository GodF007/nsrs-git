import OpenAI from 'openai'
export interface CodeBlockProps {
    value: string
    language?: string
}

export interface Message {
    thinkingContent: string | undefined
    msg: string
}

export interface BubbleItemProps {
    message: Message
    id: string | number
    status: string
    interruptedMessages: OpenAI.ChatCompletionMessageParam[]
}
