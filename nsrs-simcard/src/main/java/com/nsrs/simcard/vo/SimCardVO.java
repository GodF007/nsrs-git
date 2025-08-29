package com.nsrs.simcard.vo;

import lombok.Data;

import java.util.Date;

/**
 * SIM Card VO
 */
@Data
public class SimCardVO {
    
    /**
     * Card ID
     */
    private Long cardId;
    
    /**
     * ICCID
     */
    private String iccid;
    
    /**
     * IMSI
     */
    private String imsi;
    
    /**
     * Batch ID
     */
    private Long batchId;
    
    /**
     * Batch Code
     */
    private String batchCode;
    
    /**
     * Batch Name
     */
    private String batchName;
    
    /**
     * Card Type ID
     */
    private Long cardTypeId;
    
    /**
     * Card Type Name
     */
    private String cardTypeName;
    
    /**
     * Specification ID
     */
    private Long specId;
    
    /**
     * Specification Name
     */
    private String specName;
    
    /**
     * Data Type: 1-Data Card, 2-Voice Card, 3-Dual Mode Card, 4-IoT Card
     */
    private Integer dataType;
    
    /**
     * Data Type Name
     */
    private String dataTypeName;
    
    /**
     * Supplier ID
     */
    private Long supplierId;
    
    /**
     * Supplier Name
     */
    private String supplierName;
    
    /**
     * Organization ID
     */
    private Long orgId;
    
    /**
     * Organization Name
     */
    private String orgName;
    
    /**
     * Status: 1-Published, 2-Assigned, 3-Activated, 4-Deactivated, 5-Recycled
     */
    private Integer status;
    
    /**
     * Status Name
     */
    private String statusName;
    
    /**
     * Remark
     */
    private String remark;
    
    /**
     * Create Time
     */
    private Date createTime;
    
    /**
     * Update Time
     */
    private Date updateTime;
}