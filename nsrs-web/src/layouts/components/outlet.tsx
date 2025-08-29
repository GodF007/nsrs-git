import { forwardRef, useContext, useRef } from 'react'

import { cloneDeep } from 'lodash-es'
import { CSSTransition, SwitchTransition } from 'react-transition-group'

import { getRouterContext, Outlet, useLocation } from '@tanstack/react-router'

const AnimatedOutlet = forwardRef<HTMLDivElement>(() => {
    const { pathname } = useLocation()

    const nodeRef = useRef(null)

    const RouterContext = getRouterContext()

    const routerContext = useContext(RouterContext)

    const renderedContext = useRef(routerContext)

    const isPresent = useRef(true)

    if (isPresent) {
        renderedContext.current = cloneDeep(routerContext)
    }

    return (
        <SwitchTransition>
            <CSSTransition
                nodeRef={nodeRef}
                key={pathname}
                timeout={300}
                classNames="animate"
                unmountOnExit
                onExited={() => (isPresent.current = true)}
                onEntered={() => (isPresent.current = false)}
            >
                <div ref={nodeRef}>
                    <div key={pathname}>
                        <RouterContext.Provider value={renderedContext.current}>
                            <Outlet />
                        </RouterContext.Provider>
                    </div>
                </div>
            </CSSTransition>
        </SwitchTransition>
    )
})

export default AnimatedOutlet
