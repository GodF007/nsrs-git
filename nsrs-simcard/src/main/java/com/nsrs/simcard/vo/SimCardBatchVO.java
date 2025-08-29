package com.nsrs.simcard.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * SIM卡批次VO
 */
@Data
public class SimCardBatchVO {
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 批次编号
     */
    private String batchCode;
    
    /**
     * 批次名称
     */
    private String batchName;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 供应商名称
     */
    private String supplierName;
    
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    
    /**
     * 入库日期
     */
    private LocalDate importDate;
    
    /**
     * 入库操作用户ID
     */
    private Long importUserId;
    
    /**
     * 入库操作用户名称
     */
    private String importUserName;
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 已激活数量
     */
    private Integer activatedCount;
    
    /**
     * 已停用数量
     */
    private Integer deactivatedCount;
    
    /**
     * 已回收数量
     */
    private Integer recycledCount;
    
    /**
     * 可用数量
     */
    private Integer availableCount;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}