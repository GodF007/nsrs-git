package com.nsrs.simcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SIM卡查询条件DTO
 */
@Data
@Schema(description = "SIM卡查询条件")
public class SimCardQueryDTO {

    @Schema(description = "ICCID")
    private String iccid;

    @Schema(description = "MSISDN")
    private String msisdn;

    @Schema(description = "IMSI")
    private String imsi;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "状态：0-未激活，1-已激活，2-使用中，3-已停用，4-已报废")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;
}