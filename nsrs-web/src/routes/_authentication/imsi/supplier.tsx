import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/imsi/supplier')({
    component: RouteComponent,
    staticData: {
        code: 'IMSI_Supplier',
        langCode: 'Menu.IMSI_Supplier',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/imsi/supplier"!</div>
}
