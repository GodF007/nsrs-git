package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡库存预警配置实体类
 */
@Data
@TableName("sim_card_inventory_alert")
public class SimCardInventoryAlert {
    
    /**
     * 预警ID
     */
    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 预警名称
     */
    @TableField("alert_name")
    private String name;
    
    /**
     * 卡类型ID
     */
    @TableField("card_type_id")
    private Long cardTypeId;
    
    /**
     * 规格ID
     */
    @TableField("spec_id")
    private Long specId;
    
    /**
     * 供应商ID
     */
    @TableField("supplier_id")
    private Long supplierId;
    
    /**
     * 组织ID
     */
    @TableField("org_id")
    private Long orgId;
    
    /**
     * 最小阈值
     */
    @TableField("min_threshold")
    private Integer minThreshold;
    
    /**
     * 最大阈值
     */
    @TableField("max_threshold")
    private Integer maxThreshold;
    
    /**
     * 预警类型：1-低库存预警，2-超量预警
     */
    @TableField("alert_type")
    private Integer alertType;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    @TableField("is_active")
    private Integer isActive;
    
    /**
     * 通知邮箱，多个用逗号分隔
     */
    @TableField("notify_emails")
    private String notifyEmails;
    
    /**
     * 通知手机号，多个用逗号分隔
     */
    @TableField("notify_phones")
    private String notifyPhones;
    
    /**
     * 上次预警时间
     */
    @TableField("last_alert_time")
    private Date lastAlertTime;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
    
    /**
     * 创建用户ID
     */
    @TableField("create_user_id")
    private Long createUserId;
    
    /**
     * 更新用户ID
     */
    @TableField("update_user_id")
    private Long updateUserId;
} 