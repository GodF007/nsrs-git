package com.nsrs.binding.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批量解绑模板DTO
 * 用于Excel模板下载和数据导入
 */
@Data
@Schema(description = "批量解绑模板")
public class BatchUnbindTemplateDto {
    
    @Excel(name = "号码", orderNum = "1", width = 15, isImportField = "true")
    @Schema(description = "号码", example = "13800138000")
    private String number;
    
    @Excel(name = "IMSI", orderNum = "2", width = 20, isImportField = "true")
    @Schema(description = "IMSI号码", example = "460001234567890")
    private String imsi;
    
    @Excel(name = "ICCID", orderNum = "3", width = 25, isImportField = "true")
    @Schema(description = "ICCID号码", example = "89860012345678901234")
    private String iccid;
    
    @Excel(name = "备注", orderNum = "4", width = 30, isImportField = "false")
    @Schema(description = "备注信息")
    private String remark;
}