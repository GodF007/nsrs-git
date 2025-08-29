import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

import enUsTrans from '@/locales/en.yaml'
import zhCnTrans from '@/locales/zh.yaml'
import { languageEnums } from '@/enums/languageEnum'

i18n.use(initReactI18next).init({
    resources: {
        [languageEnums.EN]: {
            translation: enUsTrans,
        },
        [languageEnums.ZH]: {
            translation: zhCnTrans,
        },
    },
    fallbackLng: languageEnums.ZH,
    debug: false,
    interpolation: {
        escapeValue: false, // not needed for react as it escapes by default
    },
})

export default i18n
