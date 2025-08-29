package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 号码模式查询参数实体
 */
@Data
@Schema(description = "号码模式查询参数")
public class NumberPatternQueryEntity {

    /**
     * 模式名称
     */
    @Schema(description = "模式名称", example = "连号")
    private String patternName;

    /**
     * 模式代码
     */
    @Schema(description = "模式代码", example = "CONSECUTIVE")
    private String patternCode;

    /**
     * 模式描述
     */
    @Schema(description = "模式描述")
    private String patternDescription;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;


}