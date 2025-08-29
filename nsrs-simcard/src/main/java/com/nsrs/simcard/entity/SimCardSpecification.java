package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * SIM卡规格实体类
 */
@Data
@TableName("sim_card_specification")
public class SimCardSpecification {
    
    /**
     * 规格ID
     */
    @TableId(value = "spec_id", type = IdType.AUTO)
    private Long specId;
    
    /**
     * 规格名称
     */
    @NotBlank(message = "Specification name cannot be blank")
    private String specName;
    
    /**
     * 规格代码
     */
    @NotBlank(message = "Specification code cannot be blank")
    private String specCode;
    
    /**
     * 类型ID
     */
    @NotNull(message = "Type ID cannot be null")
    private Long typeId;
    
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