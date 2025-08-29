package com.nsrs.simcard.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * IMSI资源查询条件
 */
@Data
@Schema(description = "IMSI资源查询条件")
public class ImsiResourceQuery {
    
    @NotBlank(message = "IMSI号码不能为空，分表查询必须提供IMSI")
    @Schema(description = "IMSI号码")
    private String imsi;
    
    @Schema(description = "IMSI类型")
    private Integer imsiType;
    
    @Schema(description = "所属组ID")
    private Long groupId;
    
    @Schema(description = "供应商ID")
    private Long supplierId;
    
    @Schema(description = "状态")
    private Integer status;
}