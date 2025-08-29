package com.nsrs.simcard.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * IMSI组查询条件
 */
@Data
@Schema(description = "IMSI组查询条件")
public class ImsiGroupQuery {
    
    @Schema(description = "组名称")
    private String groupName;
    
    @Schema(description = "IMSI前缀")
    private String imsiPrefix;
    
    @Schema(description = "IMSI类型")
    private Integer imsiType;
}