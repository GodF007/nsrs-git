package com.nsrs.simcard.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * SIM卡导入数据项DTO
 */
@Data
public class SimCardImportItem {
    
    /**
     * 序号
     */
    private Integer rowNum;
    
    /**
     * ICCID
     */
    @NotBlank(message = "ICCID cannot be empty")
    @Pattern(regexp = "^[0-9]{19,20}$", message = "ICCID format is incorrect")
    private String iccid;
    
    /**
     * IMSI
     */
    @Pattern(regexp = "^[0-9]{15}$", message = "IMSI format is incorrect")
    private String imsi;
    
    /**
     * 导入状态：0-失败，1-成功
     */
    private Integer importStatus;
    
    /**
     * 错误信息
     */
    private String errorMsg;
}