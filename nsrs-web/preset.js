import { theme } from 'antd'
import { ThemeEnum } from './src/enums/themeEnum'

const { getDesignToken } = theme

// 定义基本颜色
const color = {
    colorPrimary: ThemeEnum.colorPrimary,
    colorSuccess: ThemeEnum.colorSuccess,
    colorWarning: ThemeEnum.colorWarning,
    colorError: ThemeEnum.colorError,
}

// 获取 dark 和 light 的主题设计令牌
const darkGlobalToken = getDesignToken({
    token: { ...color },
    algorithm: theme.darkAlgorithm,
})

const lightGlobalToken = getDesignToken({
    token: { ...color },
    algorithm: theme.defaultAlgorithm,
})

// 提取颜色
const extractColors = (tokens) => {
    const colors = {}
    Object.keys(tokens).forEach((key) => {
        if (key.startsWith('color')) {
            colors[key] = tokens[key]
        }
    })
    return colors
}

// 提取字体
const extractFonts = (tokens) => {
    const fontSize = {}
    Object.keys(tokens).forEach((key) => {
        if (key.startsWith('fontSize')) {
            fontSize[key] = tokens[key]
        }
    })
    return fontSize
}

// 提取圆角
const extractRadius = (tokens) => {
    const radius = {}
    Object.keys(tokens).forEach((key) => {
        if (key.startsWith('borderRadius')) {
            radius[key] = tokens[key]
        }
    })
    return radius
}

// 提取阴影
const extractShadows = (tokens) => {
    const shadows = {}
    Object.keys(tokens).forEach((key) => {
        if (key.startsWith('boxShadow')) {
            shadows[key] = tokens[key]
        }
    })
    return shadows
}

// 提取动画和过渡
const extractAnimationsDuration = (tokens) => {
    const duration = {}
    Object.keys(tokens).forEach((key) => {
        if (key.startsWith('motionDuration')) {
            duration[key] = tokens[key]
        }
    })
    return duration
}

const extractAnimationsTimingFunction = (tokens) => {
    const timingFunction = {}
    Object.keys(tokens).forEach((key) => {
        if (key.startsWith('motionEase')) {
            timingFunction[key] = tokens[key]
        }
    })
    return timingFunction
}

// 提取所有设计令牌
const extractDesignTokens = (tokens) => {
    return {
        colors: extractColors(tokens),
        fontSize: extractFonts(tokens),
        radius: extractRadius(tokens),
        shadows: extractShadows(tokens),
        duration: extractAnimationsDuration(tokens),
        timingFunction: extractAnimationsTimingFunction(tokens),
    }
}

const lightTokens = extractDesignTokens(lightGlobalToken)
const darkTokens = extractDesignTokens(darkGlobalToken)

const preset = {
    colors: {
        light: lightTokens.colors,
        dark: darkTokens.colors,
    },
    fontSize: lightTokens.fontSize || darkTokens.fontSize,
    borderRadius: lightTokens.radius || darkTokens.radius,
    boxShadow: lightTokens.shadows || darkTokens.shadows,
    transitionDuration: lightTokens.duration || darkTokens.duration,
    transitionTimingFunction: lightTokens.timingFunction || darkTokens.timingFunction,
}

export default preset
