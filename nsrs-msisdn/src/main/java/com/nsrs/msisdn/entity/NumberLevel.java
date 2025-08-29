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
import java.math.BigDecimal;
import java.util.Date;

/**
 * 号码级别实体类
 */
@Data
@TableName("number_level")
@Schema(description = "号码级别信息")
public class NumberLevel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 级别ID
     */
    @TableId(value = "level_id", type = IdType.AUTO)
    @Schema(description = "级别ID")
    private Long levelId;
    
    /**
     * 级别名称
     */
    @TableField(value = "level_name")
    @Schema(description = "级别名称")
    @NotBlank(message = "Level name cannot be blank")
    private String levelName;
    
    /**
     * 级别代码
     */
    @TableField(value = "level_code")
    @Schema(description = "级别代码")
    @NotBlank(message = "Level code cannot be blank")
    private String levelCode;
    
    /**
     * 费用
     */
    @TableField(value = "charge")
    @Schema(description = "费用")
    private BigDecimal charge;
    
    /**
     * 描述
     */
    @TableField(value = "description")
    @Schema(description = "描述")
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(value = "status")
    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    /**
     * 是否需要审批：0-不需要审批，1-需要审批
     */
    @TableField(value = "need_approval")
    @Schema(description = "是否需要审批：0-不需要审批，1-需要审批")
    @NotNull(message = "Need approval flag cannot be null")
    private Integer needApproval;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    @Schema(description = "创建时间")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    @Schema(description = "更新时间")
    private Date updateTime;
    
    /**
     * 创建用户ID
     */
    @TableField(value = "create_user_id")
    @Schema(description = "创建用户ID")
    private Long createUserId;
    
    /**
     * 更新用户ID
     */
    @TableField(value = "update_user_id")
    @Schema(description = "更新用户ID")
    private Long updateUserId;
}