import Main from '../components/main'
import Header from '../components/header'
import TabBar from '../components/tabBar'
import Sidebar from '../components/sidebar'
import { useMenuCollapseStore } from '@/stores/system'
import { isMicroAppEnv } from '@/utils/micro.ts'

const FullMode: React.FC = () => {
    const { collapsed } = useMenuCollapseStore()
    return (
        <>
            {!isMicroAppEnv && <Header />}
            <div className="flex p-10">
                <Sidebar />
                <div style={{ width: collapsed ? 'calc(100vw - 80px)' : 'calc(100vw - 270px)', marginLeft: '10px' }}>
                    <TabBar />
                    <Main />
                </div>
            </div>
        </>
    )
}
export default FullMode
