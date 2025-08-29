import { useRef } from 'react'

import { FloatButton } from 'antd'

import { ToTop } from '@icon-park/react'

import AnimatedOutlet from './outlet'

const Main: React.FC = () => {
    const scrollRef = useRef<HTMLDivElement>(null)

    return (
        <div ref={scrollRef} className="main">
            <AnimatedOutlet />
            <FloatButton.BackTop
                shape="square"
                target={() => scrollRef.current || window}
                visibilityHeight={100}
                icon={<ToTop className="!animate-bounce" />}
                style={{ bottom: 100 }}
            />
        </div>
    )
}
export default Main
