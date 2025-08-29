package com.nsrs.msisdn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import com.nsrs.common.enums.ResultStatusEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 号码操作日志实体�?
 */
@Data
@Accessors(chain = true)
@TableName("number_operation_log")
@Schema(description = "号码操作日志信息")
public class NumberOperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;
    
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
     * 号码类型
     */
    @TableField(value = "number_type")
    @Schema(description = "号码类型")
    private Integer numberType;
    
    /**
     * 操作类型：1-创建，2-预留，3-分配，4-激活，5-冻结，6-解冻，7-释放，8-回收
     */
    @TableField(value = "operation_type")
    @Schema(description = "操作类型：1-创建，2-预留，3-分配，4-激活，5-冻结，6-解冻，7-释放，8-回收")
    @NotNull(message = "Operation type cannot be null")
    private Integer operationType;
    
    /**
     * 原状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定
     */
    @TableField(value = "old_status")
    @Schema(description = "原状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定")
    private Integer oldStatus;
    
    /**
     * 新状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定
     */
    @TableField(value = "new_status")
    @Schema(description = "新状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定")
    private Integer newStatus;
    
    /**
     * 操作时间
     */
    @TableField(value = "operation_time")
    @Schema(description = "操作时间")
    @NotNull(message = "Operation time cannot be null")
    private Date operationTime;
    
    /**
     * 操作用户ID
     */
    @TableField(value = "operator_user_id")
    @Schema(description = "操作用户ID")
    @NotNull(message = "Operator user ID cannot be null")
    private Long operatorUserId;
    
    /**
     * 费用
     */
    @TableField(value = "charge")
    @Schema(description = "费用")
    private BigDecimal charge;
    
    /**
     * 组织名称
     */
    @TableField(value = "org_name")
    @Schema(description = "组织名称")
    private String orgName;
    
    /**
     * 操作结果状态：0-失败�?-成功
     */
    @TableField(value = "result_status")
    @Schema(description = "操作结果状态：0-失败，1-成功")
    private Integer resultStatus;
    
    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description = "备注")
    private String remark;
    

    /**
     * 记录成功操作日志
     */
    public NumberOperationLog info(String number, String remark, Integer operationType, Integer newStatus) {
        this.number = number;
        this.remark = remark;
        this.operationType = operationType;
        this.newStatus = newStatus;
        this.resultStatus = ResultStatusEnum.SUCCESS.getCode();
        this.operationTime = new Date();
        return this;
    }
    
    /**
     * 记录失败操作日志
     */
    public NumberOperationLog error(String number, String remark, Integer operationType) {
        this.number = number;
        this.remark = remark;
        this.operationType = operationType;
        this.resultStatus = ResultStatusEnum.FAILED.getCode();
        this.operationTime = new Date();
        return this;
    }
}