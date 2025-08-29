package com.nsrs.simcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡数据传输对象
 */
@Data
@Schema(description = "SIM卡信息")
public class SimCardDTO {

    @Schema(description = "SIM卡ID")
    private Long id;

    @Schema(description = "ICCID")
    private String iccid;

    @Schema(description = "MSISDN")
    private String msisdn;

    @Schema(description = "IMSI")
    private String imsi;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "批次名称")
    private String batchName;

    @Schema(description = "组织ID")
    private Long organizationId;

    @Schema(description = "组织名称")
    private String organizationName;

    @Schema(description = "状态：1-未激活，2-已激活，3-停用")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}