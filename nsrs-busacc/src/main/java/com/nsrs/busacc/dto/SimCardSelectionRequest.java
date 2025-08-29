package com.nsrs.busacc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * SIM卡选择请求DTO
 */
@Data
@Schema(description = "SIM卡选择请求")
public class SimCardSelectionRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * ICCID后缀（可选）
     * 如果提供，则根据ICCID后缀查询对应分表；如果不提供，则随机返回
     */
    @Schema(description = "ICCID suffix for table partitioning query")
    @Pattern(regexp = "^[0-9]{1,3}$", message = "ICCID suffix must be 1-3 digits")
    private String iccidSuffix;
    
    /**
     * Number of SIM cards to return
     */
    @Schema(description = "Number of SIM cards to return, uses default configuration value")
    @Min(value = 1, message = "Pool size must be at least 1")
    @Max(value = 100, message = "Pool size cannot exceed 100")
    private Integer poolSize;
    
    /**
     * 卡类型过滤（可选）
     * 1-流量卡，2-语音卡，3-双模卡，4-物联网卡
     */
    @Schema(description = "卡类型：1-流量卡，2-语音卡，3-双模卡，4-物联网卡")
    private Integer dataType;
    
    /**
     * SIM卡状态过滤（可选）
     * 默认只返回已发布状态的SIM卡
     */
    @Schema(description = "SIM卡状态过滤，默认只返回已发布状态")
    private Integer[] statusFilter;
    
    /**
     * 供应商ID过滤（可选）
     */
    @Schema(description = "供应商ID过滤")
    private Long supplierId;
    
    /**
     * 组织ID过滤（可选）
     */
    @Schema(description = "组织ID过滤")
    private Long organizationId;
    
    /**
     * 批次ID过滤（可选）
     */
    @Schema(description = "批次ID过滤")
    private Long batchId;
}