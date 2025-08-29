import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/imsi/resource')({
    component: RouteComponent,
    staticData: {
        code: 'IMSI_Resource',
        langCode: 'Menu.IMSI_Resource',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/imsi/resource"!</div>
}
