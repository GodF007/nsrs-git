/** @type {import('tailwindcss').Config} */
import preset from './preset'
export default {
    content: ['./src/**/*.{js,jsx,ts,tsx}', './public/index.html'],
    theme: {
        extend: {
            ...preset,
            fontSize: ({ theme }) => ({
                ...theme('spacing'),
            }),
            animation: {
                halfspin: 'halfspin 1s infinite',
            },
            keyframes: {
                halfspin: {
                    to: { transform: 'rotate(.5turn)' },
                },
            },
        },
        spacing: Array.from({ length: 1000 }).reduce((map, _, index) => {
            map[index] = `${index}px`
            return map
        }, {}),
    },
    darkMode: 'selector',
    plugins: [],
}
