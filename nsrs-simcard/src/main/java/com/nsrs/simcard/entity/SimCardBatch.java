package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡批次实体类
 */
@Data
@TableName("sim_card_batch")
public class SimCardBatch {

    /**
     * 批次ID
     */
    @TableId(value = "batch_id", type = IdType.AUTO)
    private Long batchId;

    /**
     * 批次名称
     */
    @TableField("batch_name")
    private String batchName;

    /**
     * 批次编号
     */
    @TableField("batch_code")
    private String batchCode;

    /**
     * 供应商ID
     */
    @TableField("supplier_id")
    private Long supplierId;

    /**
     * 生产日期
     */
    @TableField("production_date")
    private Date productionDate;

    /**
     * 入库日期
     */
    @TableField("import_date")
    private Date importDate;

    /**
     * 入库操作用户ID
     */
    @TableField("import_user_id")
    private Long importUserId;

    /**
     * 总数量
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 已激活数量
     */
    @TableField("activated_count")
    private Integer activatedCount;

    /**
     * 已停用数量
     */
    @TableField("deactivated_count")
    private Integer deactivatedCount;

    /**
     * 已回收数量
     */
    @TableField("recycled_count")
    private Integer recycledCount;

    /**
     * 可用数量
     */
    @TableField("available_count")
    private Integer availableCount;

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