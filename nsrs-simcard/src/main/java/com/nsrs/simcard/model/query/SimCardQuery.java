package com.nsrs.simcard.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * SIM卡查询条件
 */
@Data
@Schema(description = "SIM卡查询条件")
public class SimCardQuery {

    @NotBlank(message = "ICCID不能为空，分表查询必须提供ICCID")
    @Schema(description = "ICCID")
    private String iccid;

    @Schema(description = "IMSI")
    private String imsi;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "状态：0-未激活，1-已激活，2-使用中，3-已停用，4-已报废")
    private Integer status;
    
    @Schema(description = "卡类型ID")
    private Long cardType;
}