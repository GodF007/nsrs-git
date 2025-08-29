package com.nsrs.simcard.model.query;

import lombok.Data;

/**
 * SIM卡批次查询条件
 */
@Data
public class SimCardBatchQuery {
    
    /**
     * 批次名称
     */
    private String batchName;
    
    /**
     * 批次编码
     */
    private String batchCode;
    
    // 注意：orgId和status字段已从数据库表中移除
    // 如果需要按组织或状态查询，需要重新设计查询逻辑
    
//    /**
//     * 预警状态（1是 0否）
//     */
//    private Integer alertStatus;
    
    /**
     * 开始日期
     */
    private String beginDate;
    
    /**
     * 结束日期
     */
    private String endDate;
}