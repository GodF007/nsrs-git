import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/_authentication/sim-card/card-alert')({
    component: RouteComponent,
    staticData: {
        code: 'Sim_Card_Alert',
        langCode: 'Menu.Sim_Card_Alert',
    },
})

function RouteComponent() {
    return <div>Hello "/_authentication/sim-card/card-alert"!</div>
}
