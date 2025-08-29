package com.nsrs.simcard.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡实体类
 */
@Data
@TableName("sim_card")
public class SimCard {

    /**
     * 主键ID（使用全局序列生成）
     */
    @TableId(value = "card_id", type = IdType.INPUT)
    private Long id;

    /**
     * ICCID
     */
    @Excel(name = "ICCID", width = 25, isImportField = "true")
    @TableField("iccid")
    private String iccid;

    /**
     * IMSI
     */
    @Excel(name = "IMSI", width = 20, isImportField = "true")
    @TableField("imsi")
    private String imsi;

    /**
     * 批次ID
     */
    @Excel(name = "批次ID", width = 15, isImportField = "false")
    @TableField("batch_id")
    private Long batchId;
    
    /**
     * 卡类型ID
     */
    @Excel(name = "卡类型ID", width = 15, isImportField = "false")
    @TableField("card_type_id")
    private Long cardTypeId;
    
    /**
     * 规格ID
     */
    @Excel(name = "规格ID", width = 15, isImportField = "false")
    @TableField("spec_id")
    private Long specId;
    
    /**
     * 数据类型：1-流量卡，2-语音卡，3-双模卡，4-物联网卡
     */
    @Excel(name = "数据类型", width = 15, replace = {"流量卡_1", "语音卡_2", "双模卡_3", "物联网卡_4"}, isImportField = "false")
    @TableField("data_type")
    private Integer dataType;
    
    /**
     * 供应商ID
     */
    @Excel(name = "供应商ID", width = 15, isImportField = "false")
    @TableField("supplier_id")
    private Long supplierId;
    
    /**
     * 组织ID
     */
    @Excel(name = "组织ID", width = 15, isImportField = "false")
    @TableField("org_id")
    private Long organizationId;

    /**
     * 状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收
     */
    @Excel(name = "状态", width = 15, replace = {"已发布_1", "已分配_2", "已激活_3", "已停用_4", "已回收_5"}, isImportField = "false")
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @Excel(name = "备注", width = 30, isImportField = "false")
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