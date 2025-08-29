import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/imsi/group')({
    component: RouteComponent,
    staticData: {
        code: 'IMSI_Group',
        langCode: 'Menu.IMSI_Group',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/imsi/group"!</div>
}
