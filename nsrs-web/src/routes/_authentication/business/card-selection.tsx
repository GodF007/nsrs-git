import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/business/card-selection')({
    component: RouteComponent,
    staticData: {
        code: 'Card_Selection',
        langCode: 'Menu.Business_Card_Selection',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/business/card-selection"!</div>
}
