package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡库存预警日志实体类
 */
@Data
@TableName("sim_card_inventory_alert_log")
public class SimCardInventoryAlertLog {
    
    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 预警ID
     */
    @TableField("alert_id")
    private Long alertId;
    
    /**
     * 预警时间
     */
    @TableField("alert_time")
    private Date alertTime;
    
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
     * 当前数量
     */
    @TableField("current_count")
    private Integer currentCount;
    
    /**
     * 阈值
     */
    @TableField("threshold")
    private Integer threshold;
    
    /**
     * 预警类型：1-低库存预警，2-超量预警
     */
    @TableField("alert_type")
    private Integer alertType;
    
    /**
     * 通知状态：0-未通知，1-已通知
     */
    @TableField("notify_status")
    private Integer notifyStatus;
    
    /**
     * 通知时间
     */
    @TableField("notify_time")
    private Date notifyTime;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
} 