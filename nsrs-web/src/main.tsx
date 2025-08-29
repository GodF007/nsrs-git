import { createRoot } from 'react-dom/client'
import { isMicroAppEnv } from '@/utils/micro.ts'

import App from './App.tsx'

import 'normalize.css'

import '@/utils/i18n.ts'
import '@/styles/global.css'
import '@ant-design/v5-patch-for-react-19'

import './index.css'
import '../preset.js'

if (isMicroAppEnv) {
    console.log(
        '%cIN MICRO APP ENVIRONMENT',
        'color: white; background-color: #697eff; padding: 4px; border-radius: 4px;',
    )
} else {
    console.log(
        '%cNOT IN MICRO APP ENVIRONMENT',
        'color: white; background-color: #ff4d4f; padding: 4px; border-radius: 4px;',
    )
}

createRoot(document.getElementById('root')!).render(<App />)
