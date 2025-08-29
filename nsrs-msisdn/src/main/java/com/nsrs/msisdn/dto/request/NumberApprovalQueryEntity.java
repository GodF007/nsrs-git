package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 号码审批查询参数实体
 */
@Data
@Schema(description = "号码审批查询参数")
public class NumberApprovalQueryEntity {

    /**
     * 申请号码
     */
    @Schema(description = "申请号码", example = "13800138000")
    private String applyNumber;

    /**
     * 申请人
     */
    @Schema(description = "申请人")
    private String applicant;

    /**
     * 审批状态
     */
    @Schema(description = "审批状态")
    private Integer approvalStatus;

    /**
     * 申请类型
     */
    @Schema(description = "申请类型")
    private Integer applyType;


}