import React, { useState, useEffect } from 'react'

const SvgIcon: React.FC<React.SVGAttributes<SVGSVGElement> & { icon: string }> = (props) => {
    const { icon, ...svgProps } = props
    const [Icon, setIcon] = useState<React.ComponentType<React.SVGAttributes<SVGSVGElement>> | null>(null)

    useEffect(() => {
        if (icon.length === 0) return
        import(`../../assets/svg/${icon}.svg?react`)
            .then((module) => setIcon(() => module.default))
            .catch(() => setIcon(null))
    }, [icon])

    if (!Icon) return null

    return <Icon {...svgProps} />
}

export default SvgIcon
