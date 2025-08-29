package com.nsrs.simcard.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * IMSI资源实体类
 */
@Data
@TableName("imsi_resource")
@Schema(description = "IMSI资源信息")
public class ImsiResource implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "imsi_id", type = IdType.INPUT)
    @Schema(description = "IMSI ID")
    private Long imsiId;
    
    @Schema(description = "IMSI号码")
    @Excel(name = "IMSI号码", orderNum = "1", width = 20)
    private String imsi;
    
    @Schema(description = "IMSI类型：1-GSM Postpaid，2-GSM Prepaid，3-CDMA......")
    @Excel(name = "IMSI类型", orderNum = "2", width = 15, replace = {"GSM Postpaid_1", "GSM Prepaid_2", "CDMA_3"})
    private Integer imsiType;
    
    @Schema(description = "所属组ID")
    @Excel(name = "所属组ID", orderNum = "3", width = 12)
    private Long groupId;
    
    @Schema(description = "供应商ID")
    @Excel(name = "供应商ID", orderNum = "4", width = 12)
    private Long supplierId;
    
    @Schema(description = "密码")
    @Excel(name = "密码", orderNum = "5", width = 15)
    private String password;
    
    @Schema(description = "账单ID")
    @Excel(name = "账单ID", orderNum = "6", width = 15)
    private String billId;
    
    @Schema(description = "状态：1-空闲，2-已绑定，3-已使用，4-已锁定")
    @Excel(name = "状态", orderNum = "7", width = 10, replace = {"空闲_1", "已绑定_2", "已使用_3", "已锁定_4"})
    private Integer status;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "创建用户ID")
    private Long createUserId;
    
    @Schema(description = "更新用户ID")
    private Long updateUserId;
}