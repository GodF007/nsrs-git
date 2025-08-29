package com.nsrs.simcard.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * SIM卡入库导入请求DTO
 */
@Data
public class SimCardImportRequest {
    
    /**
     * 批次名称
     */
    @NotBlank(message = "Batch name cannot be empty")
    private String batchName;
    
    /**
     * 供应商ID
     */
    @NotNull(message = "Supplier cannot be empty")
    private Long supplierId;
    
    /**
     * 卡类型ID
     */
    @NotNull(message = "Card type cannot be empty")
    private Long cardTypeId;
    
    /**
     * 规格ID
     */
    private Long specId;
    
    /**
     * 数据类型：1-流量卡，2-语音卡，3-双模卡，4-物联网卡
     */
    private Integer dataType;
    
    /**
     * 组织ID
     */
    private Long orgId;
    
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    
    /**
     * 入库日期
     */
    private LocalDate importDate = LocalDate.now();
    
    /**
     * 备注
     */
    private String remark;
}