import { useEffect } from 'react'

import { FullScreen, useFullScreenHandle } from 'react-full-screen'

import { useFullscreenStore } from '@/stores/system'

import FullMode from './FullMode'

const Layouts: React.FC = () => {
    const handle = useFullScreenHandle()
    const { fullscreen, setFullscreen } = useFullscreenStore()

    useEffect(() => {
        if (fullscreen) {
            handle.enter()
        } else {
            handle.exit()
        }
    }, [fullscreen])
    return (
        <FullScreen handle={handle} onChange={setFullscreen}>
            <FullMode />
        </FullScreen>
    )
}
export default Layouts
