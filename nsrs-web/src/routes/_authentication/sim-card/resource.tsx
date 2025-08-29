import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/sim-card/resource')({
    component: RouteComponent,
    staticData: {
        code: 'Sim_Card_Resource',
        langCode: 'Menu.Sim_Card_Resource',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/sim-card/resource"!</div>
}
