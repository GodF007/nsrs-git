package com.nsrs.simcard.model.query;

import lombok.Data;

/**
 * SIM卡库存预警配置查询条件
 */
@Data
public class SimCardInventoryAlertQuery {
    
    /**
     * 预警名称
     */
    private String name;
    
    /**
     * 卡类型ID
     */
    private Long cardTypeId;
    
    /**
     * 规格ID
     */
    private Long specId;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 组织ID
     */
    private Long orgId;
    
    /**
     * 预警类型：1-低库存预警，2-超量预警
     */
    private Integer alertType;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer isActive;
    
    /**
     * 开始日期
     */
    private String beginDate;
    
    /**
     * 结束日期
     */
    private String endDate;
}