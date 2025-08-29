declare module '*.svg?react' {
    import { ReactComponent } from 'react'
    const ReactComponent: React.FC<React.SVGProps<SVGSVGElement>>
    export default ReactComponent
}
