package com.nsrs.simcard.model.query;

import lombok.Data;

/**
 * SIM卡库存预警日志查询条件
 */
@Data
public class SimCardInventoryAlertLogQuery {
    
    /**
     * 预警ID
     */
    private Long alertId;
    
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
     * 通知状态：0-未通知，1-已通知
     */
    private Integer notifyStatus;
    
    /**
     * 开始日期
     */
    private String beginDate;
    
    /**
     * 结束日期
     */
    private String endDate;
}