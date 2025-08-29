package com.nsrs.busacc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 绑定激活响应DTO
 */
@Data
@Schema(description = "绑定激活响应")
public class BindingActivationResponse {
    
    @Schema(description = "绑定ID")
    private Long bindingId;
    
    @Schema(description = "号码")
    private String number;
    
    @Schema(description = "IMSI号码")
    private String imsi;
    
    @Schema(description = "ICCID")
    private String iccid;
    
    @Schema(description = "绑定状态：1-已绑定，2-已解绑")
    private Integer bindingStatus;
    
    @Schema(description = "绑定时间")
    private Date bindingTime;
    
    @Schema(description = "操作结果")
    private Boolean success;
    
    @Schema(description = "结果消息")
    private String message;
}