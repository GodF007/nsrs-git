package com.nsrs.simcard.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

import java.util.Date;

/**
 * SIM卡库存预警配置数据传输对象
 */
@Data
public class SimCardInventoryAlertDTO {
    
    /**
     * 预警ID
     */
    private Long id;
    
    /**
     * 预警名称
     */
    @NotBlank(message = "Alert name cannot be blank")
    private String name;
    
    /**
     * 卡类型ID
     */
    private Long cardTypeId;
    
    /**
     * 卡类型名称
     */
    private String cardTypeName;
    
    /**
     * 规格ID
     */
    private Long specId;
    
    /**
     * 规格名称
     */
    private String specName;
    
    /**
     * 供应商ID
     */
    private Long supplierId;
    
    /**
     * 供应商名称
     */
    private String supplierName;
    
    /**
     * 组织ID
     */
    private Long orgId;
    
    /**
     * 组织名称
     */
    private String orgName;
    
    /**
     * 最小阈值
     */
    @Min(value = 0, message = "Minimum threshold must be greater than or equal to 0")
    private Integer minThreshold;
    
    /**
     * 最大阈值
     */
    private Integer maxThreshold;
    
    /**
     * 预警类型：1-低库存预警，2-超量预警
     */
    @NotNull(message = "Alert type cannot be null")
    private Integer alertType;
    
    /**
     * 预警类型名称
     */
    private String alertTypeName;
    
    /**
     * 是否启用：0-禁用，1-启用
     */
    @NotNull(message = "Active status cannot be null")
    private Integer isActive;
    
    /**
     * 状态名称
     */
    private String statusName;
    
    /**
     * 通知邮箱，多个用逗号分隔
     */
    private String notifyEmails;
    
    /**
     * 通知手机号，多个用逗号分隔
     */
    private String notifyPhones;
    
    /**
     * 上次预警时间
     */
    private Date lastAlertTime;
    
    /**
     * 备注
     */
    private String remark;
    
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