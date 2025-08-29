package com.nsrs.simcard.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * SIM卡批次数据传输对象
 */
@Data
public class SimCardBatchDTO {
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 批次名称
     */
    @NotBlank(message = "Batch name cannot be blank")
    private String batchName;
    
    /**
     * 批次编码
     */
    @NotBlank(message = "Batch code cannot be blank")
    private String batchCode;
    
    /**
     * 供应商ID
     */
    @NotNull(message = "Supplier ID cannot be null")
    private Long supplierId;
    
    /**
     * 生产日期
     */
    private Date productionDate;
    
    /**
     * 入库日期
     */
    private Date importDate;
    
    /**
     * 入库操作用户ID
     */
    private Long importUserId;
    
    /**
     * 总数量
     */
    @NotNull(message = "Total count cannot be null")
    @Min(value = 1, message = "Total count must be greater than 0")
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
    
    // 注意：status字段已从数据库表中移除
    
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
    
    /**
     * 创建用户ID
     */
    private Long createUserId;
    
    /**
     * 更新用户ID
     */
    private Long updateUserId;
}