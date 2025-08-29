package com.nsrs.simcard.model.query;

import lombok.Data;

/**
 * SIM卡操作记录查询条件
 */
@Data
public class SimCardOperationQuery {
    
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
     * 操作用户ID
     */
    private Long operatorUserId;
    
    /**
     * 调出组织ID
     */
    private Long stockOutOrgId;
    
    /**
     * 调入组织ID
     */
    private Long stockInOrgId;
    
    /**
     * 操作结果状态：0-失败，1-成功
     */
    private Integer resultStatus;
    
    /**
     * 开始日期
     */
    private String beginDate;
    
    /**
     * 结束日期
     */
    private String endDate;
}