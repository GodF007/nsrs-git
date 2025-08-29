package com.nsrs.simcard.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * SIM卡库存预警日志数据传输对象
 */
@Data
public class SimCardInventoryAlertLogDTO {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 预警ID
     */
    private Long alertId;
    
    /**
     * 预警名称
     */
    private String alertName;
    
    /**
     * 预警时间
     */
    private Date alertTime;
    
    /**
     * 卡类型ID
     */
    private Long cardTypeId;
    
    /**
     * 卡类型名称
     */
    private String cardTypeName;
    
    /**
     * 规格ID
     */
    private Long specId;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 供应商名称
     */
    private String supplierName;
    
    /**
     * 组织ID
     */
    private Long orgId;
    
    /**
     * 组织名称
     */
    private String orgName;
    
    /**
     * 当前数量
     */
    private Integer currentCount;
    
    /**
     * 阈值
     */
    private Integer threshold;
    
    /**
     * 预警类型：1-低库存预警，2-超量预警
     */
    private Integer alertType;
    
    /**
     * 预警类型名称
     */
    private String alertTypeName;
    
    /**
     * 通知状态：0-未通知，1-已通知
     */
    private Integer notifyStatus;
    
    /**
     * 通知状态名称
     */
    private String notifyStatusName;
    
    /**
     * 通知时间
     */
    private Date notifyTime;
    
    /**
     * 备注
     */
    private String remark;
} 