package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * SIM卡操作记录实体类
 */
@Data
@TableName("sim_card_operation")
public class SimCardOperation {
    
    /**
     * 操作ID
     */
    @TableId(value = "operation_id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 卡ID
     */
    @TableField("card_id")
    private Long cardId;
    
    /**
     * ICCID
     */
    @TableField("iccid")
    private String iccid;
    
    /**
     * 操作类型：1-分配，2-回收，3-激活，4-停用，5-更新
     *  int OPERATION_DELETE = 6;
        OPERATION_ADD = 7;
        OPERATION_TYPE_IMPORT = 8;
     */
    @TableField("operation_type")
    private Integer operationType;
    
    /**
     * 操作时间
     */
    @TableField("operation_time")
    private Date operationTime;
    
    /**
     * 操作用户ID
     */
    @TableField("operator_user_id")
    private Long operatorUserId;
    
    /**
     * 原状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收
     */
    @TableField("old_status")
    private Integer oldStatus;
    
    /**
     * 新状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收
     */
    @TableField("new_status")
    private Integer newStatus;
    
    /**
     * 调出组织ID
     */
    @TableField("stock_out_org_id")
    private Long stockOutOrgId;
    
    /**
     * 调入组织ID
     */
    @TableField("stock_in_org_id")
    private Long stockInOrgId;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 操作结果状态：0-失败，1-成功
     */
    @TableField("result_status")
    private Integer resultStatus;
    
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