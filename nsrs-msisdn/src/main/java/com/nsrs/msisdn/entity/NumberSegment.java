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
 * 号码段实体类
 */
@Data
@TableName("number_segment")
@Schema(description = "号码段信息")
public class NumberSegment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 号码段ID
     */
    @TableId(value = "segment_id", type = IdType.AUTO)
    @Schema(description = "号码段ID")
    private Long segmentId;
    
    /**
     * 号码段代码
     */
    @TableField(value = "segment_code")
    @Schema(description = "号码段代码")
    @NotBlank(message = "Segment code cannot be blank")
    private String segmentCode;
    
    /**
     * 号码段类型：1-PSTN Number，2-Mobile Number，3-FTTH Number，4-SIP，5-VSAT
     */
    @TableField(value = "segment_type")
    @Schema(description = "号码段类型：1-PSTN Number，2-Mobile Number，3-FTTH Number，4-SIP，5-VSAT")
    @NotNull(message = "Segment type cannot be null")
    private Integer segmentType;
    
    /**
     * HLR/交换机ID
     */
    @TableField(value = "hlr_switch_id")
    @Schema(description = "HLR/交换机ID")
    private Long hlrSwitchId;
    
    /**
     * 区域ID
     */
    @TableField(value = "region_id")
    @Schema(description = "区域ID")
    private Long regionId;
    
    /**
     * 开始号码
     */
    @TableField(value = "start_number")
    @Schema(description = "开始号码")
    @NotBlank(message = "Start number cannot be blank")
    private String startNumber;
    
    /**
     * 结束号码
     */
    @TableField(value = "end_number")
    @Schema(description = "结束号码")
    @NotBlank(message = "End number cannot be blank")
    private String endNumber;
    
    /**
     * 总数量
     */
    @TableField(value = "total_qty")
    @Schema(description = "总数量")
    private Long totalQty;
    
    /**
     * 空闲数量
     */
    @TableField(value = "idle_qty")
    @Schema(description = "空闲数量")
    private Long idleQty;
    

    
    /**
     * 已激活数量
     */
    @TableField(value = "activated_qty")
    @Schema(description = "已激活数量")
    private Long activatedQty;
    
    /**
     * 已冻结数量
     */
    @TableField(value = "frozen_qty")
    @Schema(description = "已冻结数量")
    private Long frozenQty;
    
    /**
     * 已锁定数量
     */
    @TableField(value = "blocked_qty")
    @Schema(description = "已锁定数量")
    private Long blockedQty;
    
    /**
     * 已预留数量
     */
    @TableField(value = "reserved_qty")
    @Schema(description = "已预留数量")
    private Long reservedQty;
    
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