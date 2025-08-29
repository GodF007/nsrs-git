package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 号码操作日志查询参数实体
 */
@Data
@Schema(description = "号码操作日志查询参数")
public class NumberOperationLogQueryEntity {

    /**
     * 操作号码
     */
    @Schema(description = "操作号码", example = "13800138000")
    private String operationNumber;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型")
    private Integer operationType;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String operator;

    /**
     * 操作结果
     */
    @Schema(description = "操作结果")
    private Integer operationResult;


}