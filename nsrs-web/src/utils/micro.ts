export const isMicroAppEnv = window.__MICRO_APP_ENVIRONMENT__

export const microAppData = isMicroAppEnv && window.microApp.getGlobalData()
