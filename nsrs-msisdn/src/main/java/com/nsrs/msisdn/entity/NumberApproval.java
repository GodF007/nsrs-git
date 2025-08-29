package com.nsrs.msisdn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 号码审批实体�?
 */
@Data
@TableName("number_approval")
@Schema(description = "号码审批信息")
public class NumberApproval implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 审批ID
     */
    @TableId(value = "approval_id", type = IdType.AUTO)
    @Schema(description = "审批ID")
    private Long approvalId;
    
    /**
     * 审批编号
     */
    @TableField(value = "approval_no")
    @Schema(description = "审批编号")
    @NotBlank(message = "Approval number cannot be blank")
    private String approvalNo;
    
    /**
     * 号码ID
     */
    @TableField(value = "number_id")
    @Schema(description = "号码ID")
    @NotNull(message = "Number ID cannot be null")
    private Long numberId;
    
    /**
     * 号码
     */
    @TableField(value = "number")
    @Schema(description = "号码")
    @NotBlank(message = "Number cannot be blank")
    private String number;
    
    /**
     * 申请人ID
     */
    @TableField(value = "applicant_id")
    @Schema(description = "申请人ID")
    @NotNull(message = "Applicant ID cannot be null")
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    @TableField(value = "applicant_name")
    @Schema(description = "申请人姓名")
    @NotBlank(message = "Applicant name cannot be blank")
    private String applicantName;
    
    /**
     * 申请时间
     */
    @TableField(value = "apply_time")
    @Schema(description = "申请时间")
    @NotNull(message = "Apply time cannot be null")
    private Date applyTime;
    
    /**
     * 申请原因
     */
    @TableField(value = "apply_reason")
    @Schema(description = "申请原因")
    private String applyReason;
    
    /**
     * 客户信息
     */
    @TableField(value = "customer_info")
    @Schema(description = "客户信息")
    private String customerInfo;
    
    /**
     * 号码级别ID
     */
    @TableField(value = "level_id")
    @Schema(description = "号码级别ID")
    private Long levelId;
    
    /**
     * 审批状态：0-待审批，1-已通过，2-已拒绝，3-已取消
     */
    @TableField(value = "status")
    @Schema(description = "审批状态：0-待审批，1-已通过，2-已拒绝，3-已取消")
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    /**
     * 审批人ID
     */
    @TableField(value = "approver_id")
    @Schema(description = "审批人ID")
    private Long approverId;
    
    /**
     * 审批人姓�?
     */
    @TableField(value = "approver_name")
    @Schema(description = "审批人姓名")
    private String approverName;
    
    /**
     * 审批时间
     */
    @TableField(value = "approval_time")
    @Schema(description = "审批时间")
    private Date approvalTime;
    
    /**
     * 审批意见
     */
    @TableField(value = "approval_opinion")
    @Schema(description = "审批意见")
    private String approvalOpinion;
}