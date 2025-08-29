package com.nsrs.simcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import java.util.List;

/**
 * SIM卡批量操作请求
 */
@Data
@Schema(description = "SIM卡批量操作请求")
public class SimCardBatchOperationRequest {

    /**
     * 操作类型: ACTIVATE-激活, DEACTIVATE-停用, DELETE-删除, TRANSFER-转移批次, SET_GROUP-设置组织
     */
    @Schema(description = "操作类型", required = true)
    @NotBlank(message = "Operation type cannot be blank")
    private String operationType;

    /**
     * SIM卡ID列表
     */
    @Schema(description = "SIM卡ID列表", required = true)
    @NotEmpty(message = "SIM card IDs cannot be empty")
    private List<Long> simCardIds;

    /**
     * 批次ID（转移批次时使用）
     */
    private Long batchId;

    /**
     * 组织ID（设置组织时使用）
     */
    private Long orgId;

    /**
     * 操作备注
     */
    private String remark;
}