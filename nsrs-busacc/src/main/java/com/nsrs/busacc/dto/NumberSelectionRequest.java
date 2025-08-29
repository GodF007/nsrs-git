package com.nsrs.busacc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 号码选择请求DTO
 */
@Data
@Schema(description = "号码选择请求")
public class NumberSelectionRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 号段前缀（可选）
     * 如果提供，则根据号段查询；如果不提供，则随机返回
     */
    @Schema(description = "号段前缀，如139、177等")
    @Pattern(regexp = "^[0-9]{2,4}$", message = "Number prefix must be 2-4 digits")
    private String numberPrefix;
    
    /**
     * 返回的号码数量
     */
    @Schema(description = "返回的号码数量，默认使用配置值")
    @Min(value = 1, message = "Pool size must be at least 1")
    @Max(value = 100, message = "Pool size cannot exceed 100")
    private Integer poolSize;
    
    /**
     * 号码类型过滤（可选）
     * 1-固话，2-手机，3-800，4-400，5-VOIP，6-物联网
     */
    @Schema(description = "号码类型：1-固话，2-手机，3-800，4-400，5-VOIP，6-物联网")
    private Integer numberType;
    
    /**
     * 号码状态过滤（可选）
     * 默认只返回空闲状态的号码
     */
    @Schema(description = "号码状态过滤，默认只返回空闲状态")
    private Integer[] statusFilter;
}