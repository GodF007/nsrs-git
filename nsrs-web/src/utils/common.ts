import dayjs from 'dayjs'

export function importImg(file: string, name: string) {
    return new URL(`/src/assets/${file}/${name}`, import.meta.url).href
}

export function formatDate(date: string | Date) {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
