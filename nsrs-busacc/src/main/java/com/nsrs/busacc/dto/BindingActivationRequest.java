package com.nsrs.busacc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 绑定激活请求DTO
 */
@Data
@Schema(description = "绑定激活请求")
public class BindingActivationRequest {
    
    @NotBlank(message = "Number cannot be blank")
    @Schema(description = "号码", example = "13800138000", required = true)
    private String number;
    
    @NotBlank(message = "IMSI cannot be blank")
    @Schema(description = "IMSI号码", example = "460001234567890", required = true)
    private String imsi;
    
    @NotBlank(message = "ICCID cannot be blank")
    @Schema(description = "ICCID", example = "89860012345678901234", required = true)
    private String iccid;
    
    @Schema(description = "订单ID")
    private Long orderId;
    
    @Schema(description = "绑定类型: 1-普通绑定，2-批量绑定，3-测试", example = "1")
    private Integer bindingType = 1; // 1-普通绑定，2-批量绑定，3-测试
    
    @NotNull(message = "Operator user ID cannot be null")
    @Schema(description = "操作用户ID", required = true)
    private Long operatorUserId;
    
    @Schema(description = "备注")
    private String remark;
}