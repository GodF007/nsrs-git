package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 组织实体类
 */
@Data
@TableName("organization")
public class Organization {
    
    /**
     * 组织ID
     */
    @TableId(value = "org_id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 组织名称
     */
    @TableField("org_name")
    private String name;
    
    /**
     * 组织代码
     */
    @TableField("org_code")
    private String code;
    
    /**
     * 组织类型：1-公司，2-部门，3-分支机构，4-合作伙伴
     */
    @TableField("org_type")
    private Integer type;
    
    /**
     * 父组织ID
     */
    @TableField("parent_id")
    private Long parentId;
    
    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;
    
    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;
    
    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 地址
     */
    @TableField("address")
    private String address;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;
    
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