package com.nsrs.simcard.model.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Email;

import java.util.Date;

/**
 * 组织数据传输对象
 */
@Data
public class OrganizationDTO {
    
    /**
     * 组织ID
     */
    private Long id;
    
    /**
     * 组织名称
     */
    private String name;
    
    /**
     * 组织代码
     */
    @NotBlank(message = "Organization code cannot be blank")
    private String code;
    
    /**
     * 组织类型：1-公司，2-部门，3-分支机构，4-合作伙伴
     */
    private Integer type;
    
    /**
     * 组织类型名称
     */
    @NotBlank(message = "Organization type name cannot be blank")
    private String typeName;
    
    /**
     * 父组织ID
     */
    private Long parentId;
    
    /**
     * 父组织名称
     */
    @NotBlank(message = "Parent organization name cannot be blank")
    private String parentName;
    
    /**
     * 联系人
     */
    @NotBlank(message = "Contact person cannot be blank")
    private String contactPerson;
    
    /**
     * 联系电话
     */
    @NotBlank(message = "Contact phone cannot be blank")
    private String contactPhone;
    
    /**
     * 电子邮箱
     */
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;
    
    /**
     * 地址
     */
    @NotBlank(message = "Address cannot be blank")
    private String address;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 状态名称
     */
    private String statusName;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}