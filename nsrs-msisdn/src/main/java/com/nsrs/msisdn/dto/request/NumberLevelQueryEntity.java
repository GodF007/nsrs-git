package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 号码等级查询参数实体
 */
@Data
@Schema(description = "号码等级查询参数")
public class NumberLevelQueryEntity {

    /**
     * 等级名称
     */
    @Schema(description = "等级名称", example = "普通号码")
    private String levelName;

    /**
     * 等级代码
     */
    @Schema(description = "等级代码", example = "NORMAL")
    private String levelCode;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;


}