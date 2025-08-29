package com.nsrs.msisdn.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 号码资源实体�?
 */
@Data
@TableName("number_resource")
@Schema(description = "号码资源信息")
public class NumberResource implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 号码ID
     */
    @TableId(value = "number_id", type = IdType.INPUT)
    @Schema(description = "号码ID")
    private Long numberId;
    
    /**
     * 号码
     */
    @TableField(value = "number")
    @Schema(description = "号码")
    @Excel(name = "号码", orderNum = "1", width = 15)
    @NotBlank(message = "Number cannot be blank")
    private String number;
    
    /**
     * 号码类型：1-固话，2-手机，3-800，4-400，5-VOIP，6-物联网
     */
    @TableField(value = "number_type")
    @Schema(description = "号码类型：1-固话，2-手机，3-800，4-400，5-VOIP，6-物联网")
    @Excel(name = "号码类型", orderNum = "2", width = 12, replace = {"固话_1", "手机_2", "800_3", "400_4", "VOIP_5", "物联网_6"})
    @NotNull(message = "Number type cannot be null")
    private Integer numberType;
    
    /**
     * 号码段ID
     */
    @TableField(value = "segment_id")
    @Schema(description = "号码段ID")
    @Excel(name = "号码段ID", orderNum = "3", width = 12)
    private Long segmentId;
    
    /**
     * 号码级别ID
     */
    @TableField(value = "level_id")
    @Schema(description = "号码级别ID")
    private Long levelId;
    
    /**
     * 号码模式ID
     */
    @TableField(value = "pattern_id")
    @Schema(description = "号码模式ID")
    private Long patternId;
    
    /**
     * HLR/交换机ID
     */
    @TableField(value = "hlr_id")
    @Schema(description = "HLR/交换机ID")
    private Long hlrId;
    
    /**
     * ICCID
     */
    @TableField(value = "iccid")
    @Schema(description = "ICCID")
    @Excel(name = "ICCID", orderNum = "4", width = 20)
    private String iccid;
    
    /**
     * 状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定
     */
    @TableField(value = "status")
    @Schema(description = "状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定")
    @Excel(name = "状态", orderNum = "5", width = 10, replace = {"空闲_1", "预留_2", "已分配_3", "已激活_4", "已使用_5", "已冻结_6", "已锁定_7"})
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    /**
     * 费用
     */
    @TableField(value = "charge")
    @Schema(description = "费用")
    @Excel(name = "费用", orderNum = "6", width = 12, numFormat = "0.00")
    private BigDecimal charge;
    
    /**
     * 归属组织
     */
    @TableField(value = "attributive_org")
    @Schema(description = "归属组织")
    @Excel(name = "归属组织", orderNum = "7", width = 15)
    private String attributiveOrg;
    
    /**
     * 备注
     */
    @TableField(value = "remark")
    @Schema(description = "备注")
    @Excel(name = "备注", orderNum = "8", width = 20)
    private String remark;
    
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