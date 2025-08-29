package com.nsrs.simcard.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 组织树视图对象
 */
@Data
public class OrganizationTreeVO {
    
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
    private String code;
    
    /**
     * 组织类型：1-公司，2-部门，3-分支机构，4-合作伙伴
     */
    private Integer type;
    
    /**
     * 组织类型名称
     */
    private String typeName;
    
    /**
     * 父组织ID
     */
    private Long parentId;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
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
     * 子组织列表
     */
    private List<OrganizationTreeVO> children = new ArrayList<>();
} 