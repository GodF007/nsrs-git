package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * SIM卡类型实体类
 */
@Data
@TableName("sim_card_type")
public class SimCardType {
    
    /**
     * 类型ID
     */
    @TableId(value = "type_id", type = IdType.AUTO)
    private Long typeId;
    
    /**
     * 类型名称
     */
    @NotBlank(message = "Type name cannot be blank")
    private String typeName;
    
    /**
     * 类型代码，GSM、CDMA
     */
    @NotBlank(message = "Type code cannot be blank")
    private String typeCode;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 创建用户ID
     */
    private Long createUserId;
    
    /**
     * 更新用户ID
     */
    private Long updateUserId;
}