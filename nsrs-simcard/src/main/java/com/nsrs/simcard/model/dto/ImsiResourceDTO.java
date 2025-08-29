package com.nsrs.simcard.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * IMSI资源数据传输对象
 */
@Data
@Schema(description = "IMSI资源信息")
public class ImsiResourceDTO {
    
    @Schema(description = "IMSI ID")
    private Long imsiId;
    
    @Schema(description = "IMSI号码", required = true)
    @NotBlank(message = "IMSI号码不能为空")
    private String imsi;
    
    @Schema(description = "IMSI类型：1-GSM Postpaid，2-GSM Prepaid，3-CDMA......")
    private Integer imsiType;
    
    @Schema(description = "IMSI类型描述")
    private String imsiTypeDesc;
    
    @Schema(description = "所属组ID")
    private Long groupId;
    
    @Schema(description = "所属组名称")
    private String groupName;
    
    @Schema(description = "供应商ID")
    private Long supplierId;
    
    @Schema(description = "供应商名称")
    private String supplierName;
    
    @Schema(description = "密码")
    private String password;
    
    @Schema(description = "账单ID")
    private String billId;
    
    @Schema(description = "状态：1-空闲，2-已绑定，3-已使用，4-已锁定")
    private Integer status;
    
    @Schema(description = "状态描述")
    private String statusDesc;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
}