import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/sim-card/organization')({
    component: RouteComponent,
    staticData: {
        code: 'Sim_Card_Organization',
        langCode: 'Menu.Sim_Card_Organization',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/sim-card/organization"!</div>
}
