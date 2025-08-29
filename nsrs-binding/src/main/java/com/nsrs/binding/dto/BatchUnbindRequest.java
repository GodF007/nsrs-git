package com.nsrs.binding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 批量解绑请求DTO
 */
@Data
@Schema(description = "批量解绑请求")
public class BatchUnbindRequest {
    
    @NotEmpty(message = "Unbind items cannot be empty")
    @Valid
    @Schema(description = "解绑项目列表", required = true)
    private List<UnbindItem> unbindItems;
    
    @NotNull(message = "Operator user ID cannot be null")
    @Schema(description = "操作用户ID", required = true)
    private Long operatorUserId;
    
    @Schema(description = "备注")
    private String remark;
    
    /**
     * 解绑项目
     */
    @Data
    @Schema(description = "解绑项目")
    public static class UnbindItem {
        
        @NotBlank(message = "Number cannot be blank")
        @Schema(description = "号码", required = true)
        private String number;
        
        @Schema(description = "IMSI")
        private String imsi;
        
        @Schema(description = "ICCID")
        private String iccid;
    }
}