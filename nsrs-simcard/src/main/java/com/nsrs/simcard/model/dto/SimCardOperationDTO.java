package com.nsrs.simcard.model.dto;

import lombok.Data;

import java.util.Date;

/**
 * SIM卡操作记录数据传输对象
 */
@Data
public class SimCardOperationDTO {
    
    /**
     * 操作ID
     */
    private Long id;
    
    /**
     * 卡ID
     */
    private Long cardId;
    
    /**
     * ICCID
     */
    private String iccid;
    
    /**
     * 操作类型：1-分配，2-回收，3-激活，4-停用，5-更新
     */
    private Integer operationType;
    
    /**
     * 操作类型名称
     */
    private String operationTypeName;
    
    /**
     * 操作时间
     */
    private Date operationTime;
    
    /**
     * 操作用户ID
     */
    private Long operatorUserId;
    
    /**
     * 操作用户名称
     */
    private String operatorUserName;
    
    /**
     * 原状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收
     */
    private Integer oldStatus;
    
    /**
     * 原状态名称
     */
    private String oldStatusName;
    
    /**
     * 新状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收
     */
    private Integer newStatus;
    
    /**
     * 新状态名称
     */
    private String newStatusName;
    
    /**
     * 调出组织ID
     */
    private Long stockOutOrgId;
    
    /**
     * 调出组织名称
     */
    private String stockOutOrgName;
    
    /**
     * 调入组织ID
     */
    private Long stockInOrgId;
    
    /**
     * 调入组织名称
     */
    private String stockInOrgName;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 操作结果状态：0-失败，1-成功
     */
    private Integer resultStatus;
    
    /**
     * 操作结果状态名称
     */
    private String resultStatusName;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
} 