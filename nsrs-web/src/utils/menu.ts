export const menu = [
    {
        key: 'Dashboard',
        label: 'Dashboard',
        icon: 'dashboard',
        path: '/dashboard',
        filePath: '/dashboard/index',
    },
    {
        key: 'Number',
        label: 'Number',
        icon: 'number',
        path: '/number',
        children: [
            {
                key: 'Number_Resource',
                label: 'Number Resource',
                path: '/number/resource',
                filePath: '/number/resource',
            },
            {
                key: 'Number_Level',
                label: 'Number Level',
                path: '/number/level',
                filePath: '/number/level',
            },
            {
                key: 'Number_Segment',
                label: 'Number Segment',
                path: '/number/segment',
                filePath: '/number/segment',
            },
            {
                key: 'Number_Pattern',
                label: 'Number Pattern',
                path: '/number/pattern',
                filePath: '/number/pattern',
            },
            {
                key: 'Number_Region',
                label: 'Number Region',
                path: '/number/region',
                filePath: '/number/region',
            },
            {
                key: 'Number_Hlr',
                label: 'Number Hlr',
                path: '/number/hlr',
                filePath: '/number/hlr',
            },
        ],
    },
    {
        key: 'Business',
        label: 'Business',
        icon: 'business',
        path: '/business',
        children: [
            {
                key: 'Card_Selection',
                label: 'Card Selection',
                path: '/business/card-selection',
                filePath: '/business/card-selection',
            },
        ],
    },
    {
        key: 'Sim_Card',
        label: 'Sim Card',
        icon: 'sim-card',
        path: '/sim-card',
        children: [
            {
                key: 'Sim_Card_Resource',
                label: 'Sim Card Resource',
                path: '/sim-card/resource',
                filePath: '/sim-card/resource',
            },
            {
                key: 'Sim_Card_Type',
                label: 'Sim Card Type',
                path: '/sim-card/card-type',
                filePath: '/sim-card/card-type',
            },
            {
                key: 'Sim_Card_Specification',
                label: 'Sim Card Specification',
                path: '/sim-card/specification',
                filePath: '/sim-card/specification',
            },
            {
                key: 'Sim_Card_Supplier',
                label: 'Sim Card Supplier',
                path: '/sim-card/supplier',
                filePath: '/sim-card/supplier',
            },
            {
                key: 'Sim_Card_Organization',
                label: 'Sim Card Organization',
                path: '/sim-card/organization',
                filePath: '/sim-card/organization',
            },
            {
                key: 'Sim_Card_Batch',
                label: 'Sim Card Batch',
                path: '/sim-card/card-batch',
                filePath: '/sim-card/card-batch',
            },
            {
                key: 'Sim_Card_Alert',
                label: 'Sim Card Alert',
                path: '/sim-card/card-alert',
                filePath: '/sim-card/card-alert',
            },
        ],
    },
    {
        key: 'IMSI',
        label: 'IMSI',
        icon: 'imsi',
        path: '/imsi',
        children: [
            {
                key: 'IMSI_Group',
                label: 'IMSI Group',
                path: '/imsi/group',
                filePath: '/imsi/group',
            },
            {
                key: 'IMSI_Resource',
                label: 'IMSI Resource',
                path: '/imsi/resource',
                filePath: '/imsi/resource',
            },
            {
                key: 'IMSI_Supplier',
                label: 'IMSI Supplier',
                path: '/imsi/supplier',
                filePath: '/imsi/supplier',
            },
        ],
    },
]
