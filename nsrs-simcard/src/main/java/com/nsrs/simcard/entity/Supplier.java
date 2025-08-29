package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 供应商实体类
 */
@Data
@TableName("supplier")
public class Supplier {
    
    /**
     * 供应商ID
     */
    @TableId(value = "supplier_id", type = IdType.AUTO)
    private Long supplierId;
    
    /**
     * 供应商名称
     */
    private String supplierName;
    
    /**
     * 供应商代码
     */
    private String supplierCode;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 状态：0-禁用，1-启用
     */
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