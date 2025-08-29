package com.nsrs.simcard.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

/**
 * IMSI生成请求
 */
@Data
@Schema(description = "IMSI生成请求")
public class ImsiGenerateRequest {
    
    @Schema(description = "组ID", required = true)
    @NotNull(message = "组ID不能为空")
    private Long groupId;
    
    @Schema(description = "生成数量", required = true)
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量必须大于0")
    private Integer count;
    
    @Schema(description = "操作用户ID", required = true)
    @NotNull(message = "操作用户ID不能为空")
    private Long operatorUserId;
    
    @Schema(description = "供应商ID")
    private Long supplierId;
}