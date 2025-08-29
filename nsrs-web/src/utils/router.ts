import { createRouter } from '@tanstack/react-router'
import { routeTree } from '@/routeTree.gen'
import Error404 from '@/page/404'

const router = createRouter({
    routeTree,
    defaultStructuralSharing: true,
    context: { token: undefined!, permissions: undefined! },
    defaultNotFoundComponent: Error404,
})

export default router
