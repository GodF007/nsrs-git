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
 * HLR/交换机实体类
 */
@Data
@TableName("hlr_switch")
@Schema(description = "HLR/交换机信息")
public class HlrSwitch implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * HLR/交换机ID
     */
    @TableId(value = "hlr_id", type = IdType.AUTO)
    @Schema(description = "HLR/交换机ID")
    private Long hlrId;
    
    /**
     * HLR/交换机名称
     */
    @TableField(value = "hlr_name")
    @Schema(description = "HLR/交换机名称")
    @NotBlank(message = "HLR/Switch name cannot be blank")
    private String hlrName;
    
    /**
     * HLR/交换机代码
     */
    @TableField(value = "hlr_code")
    @Schema(description = "HLR/交换机代码")
    @NotBlank(message = "HLR/Switch code cannot be blank")
    private String hlrCode;
    
    /**
     * HLR/交换机类型：1-HLR�?-交换机，3-服务平台
     */
    @TableField(value = "hlr_type")
    @Schema(description = "HLR/交换机类型：1-HLR，2-交换机，3-服务平台")
    private Integer hlrType;
    
    /**
     * 区域ID
     */
    @TableField(value = "region_id")
    @Schema(description = "区域ID")
    private Long regionId;
    
    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    @Schema(description = "IP地址")
    private String ipAddress;
    
    /**
     * 端口
     */
    @TableField(value = "port")
    @Schema(description = "端口")
    private String port;
    
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