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
 * 号码模式实体类
 */
@Data
@TableName("number_pattern")
@Schema(description = "号码模式信息")
public class NumberPattern implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 模式ID
     */
    @TableId(value = "pattern_id", type = IdType.AUTO)
    @Schema(description = "模式ID")
    private Long patternId;
    
    /**
     * 模式名称
     */
    @TableField(value = "pattern_name")
    @Schema(description = "模式名称")
    @NotBlank(message = "Pattern name cannot be blank")
    private String patternName;
    
    /**
     * 归属号码级别ID
     */
    @TableField(value = "level_id")
    @Schema(description = "归属号码级别ID")
    @NotNull(message = "Level ID cannot be null")
    private Long levelId;
    
    /**
     * 模式格式
     */
    @TableField(value = "pattern_format")
    @Schema(description = "模式格式")
    @NotBlank(message = "Pattern format cannot be blank")
    private String patternFormat;
    
    /**
     * 正则表达式
     */
    @TableField(value = "expression")
    @Schema(description = "正则表达式")
    @NotBlank(message = "Expression cannot be blank")
    private String expression;
    
    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description = "备注")
    private String remark;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(value = "status")
    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
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