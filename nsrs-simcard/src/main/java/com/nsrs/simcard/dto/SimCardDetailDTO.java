package com.nsrs.simcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡详情DTO
 */
@Data
@Schema(description = "SIM卡详情")
public class SimCardDetailDTO {
    /**
     * SIM卡ID
     */
    @Schema(description = "ID")
    private Long id;
    
    /**
     * ICCID号码
     */
    @Schema(description = "ICCID")
    private String iccid;
    
    /**
     * IMSI号码
     */
    @Schema(description = "IMSI")
    private String imsi;
    
    /**
     * PIN1
     */
    @Schema(description = "PIN1")
    private String pin1;
    
    /**
     * PIN2
     */
    @Schema(description = "PIN2")
    private String pin2;
    
    /**
     * PUK1
     */
    @Schema(description = "PUK1")
    private String puk1;
    
    /**
     * PUK2
     */
    @Schema(description = "PUK2")
    private String puk2;
    
    /**
     * KI
     */
    @Schema(description = "KI")
    private String ki;
    
    /**
     * ADM
     */
    @Schema(description = "ADM")
    private String adm;
    
    /**
     * SIM卡状态：0-未激活，1-已激活，2-使用中，3-已停用，4-已报废
     */
    @Schema(description = "状态：0-未激活，1-已激活，2-使用中，3-已停用，4-已报废")
    private Integer status;
    
    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;
    
    /**
     * 批次ID
     */
    @Schema(description = "批次ID")
    private Long batchId;
    
    /**
     * 批次名称
     */
    @Schema(description = "批次名称")
    private String batchName;
    
    /**
     * 组织ID
     */
    @Schema(description = "组织ID")
    private Long orgId;
    
    /**
     * 组织名称
     */
    @Schema(description = "组织名称")
    private String orgName;
    
    /**
     * 备注信息
     */
    @Schema(description = "备注")
    private String remark;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 修改时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;
}