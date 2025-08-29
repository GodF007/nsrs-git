package com.nsrs.simcard.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * SIM卡数据传输对象
 */
@Data
@Schema(description = "SIM卡信息")
public class SimCardDTO {

    @Schema(description = "SIM卡ID")
    private Long id;

    @Schema(description = "ICCID", required = true)
    @NotBlank(message = "ICCID cannot be blank")
    private String iccid;

    @Schema(description = "MSISDN")
    private String msisdn;

    @Schema(description = "IMSI")
    private String imsi;

    @Schema(description = "PIN1")
    private String pin1;
    
    @Schema(description = "PIN2")
    private String pin2;
    
    @Schema(description = "PUK1")
    private String puk1;
    
    @Schema(description = "PUK2")
    private String puk2;
    
    @Schema(description = "KI")
    private String ki;
    
    @Schema(description = "ADM")
    private String adm;

    @Schema(description = "批次ID", required = true)
    @NotNull(message = "Batch ID cannot be null")
    private Long batchId;

    @Schema(description = "批次名称")
    private String batchName;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "组织名称")
    private String orgName;

    @Schema(description = "状态：1-未激活，2-已激活，3-停用", required = true)
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "备注")
    private String remark;
}