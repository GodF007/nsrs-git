package com.nsrs.simcard.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * IMSI组数据传输对象
 */
@Data
@Schema(description = "IMSI组信息")
public class ImsiGroupDTO {
    
    @Schema(description = "组ID")
    private Long groupId;
    
    @Schema(description = "组名称", required = true)
    @NotBlank(message = "组名称不能为空")
    private String groupName;
    
    @Schema(description = "IMSI前缀")
    private String imsiPrefix;
    
    @Schema(description = "起始IMSI")
    private String imsiStart;
    
    @Schema(description = "结束IMSI")
    private String imsiEnd;
    
    @Schema(description = "IMSI类型：1-GSM Postpaid，2-GSM Prepaid，3-CDMA......")
    private Integer imsiType;
    
    @Schema(description = "IMSI类型描述")
    private String imsiTypeDesc;
    
    @Schema(description = "总数量")
    private Integer totalCount;
    
    @Schema(description = "已使用数量")
    private Integer usedCount;
    
    @Schema(description = "剩余数量")
    private Integer remainingCount;
    
    @Schema(description = "使用率")
    private String usageRate;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
}