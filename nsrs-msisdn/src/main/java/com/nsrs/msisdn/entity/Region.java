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
import java.util.List;

/**
 * 区域实体类
 */
@Data
@TableName("region")
@Schema(description = "区域信息")
public class Region implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 区域ID
     */
    @TableId(value = "region_id", type = IdType.AUTO)
    @Schema(description = "区域ID")
    private Long regionId;
    
    /**
     * 区域代码
     */
    @TableField(value = "region_code")
    @Schema(description = "区域代码")
    @NotBlank(message = "Region code cannot be blank")
    private String regionCode;
    
    /**
     * 区域名称
     */
    @TableField(value = "region_name")
    @Schema(description = "区域名称")
    @NotBlank(message = "Region name cannot be blank")
    private String regionName;
    
    /**
     * 区域类型：1-国家，2-省份，3-城市，4-区县
     */
    @TableField(value = "region_type")
    @Schema(description = "区域类型：1-国家，2-省份，3-城市，4-区县")
    private Integer regionType;
    
    /**
     * 父区域ID
     */
    @TableField(value = "parent_id")
    @Schema(description = "父区域ID")
    private Long parentId;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField(value = "status")
    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    /**
     * 描述
     */
    @TableField(value = "description")
    @Schema(description = "描述")
    private String description;
    
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
    
    /**
     * 子区域列表（非数据库字段）
     */
    @TableField(exist = false)
    @Schema(description = "子区域列表")
    private List<Region> children;
}