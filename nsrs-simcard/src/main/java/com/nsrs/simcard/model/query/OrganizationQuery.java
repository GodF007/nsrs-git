package com.nsrs.simcard.model.query;

import lombok.Data;

/**
 * 组织查询条件
 */
@Data
public class OrganizationQuery {
    
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
     * 开始日期
     */
    private String beginDate;
    
    /**
     * 结束日期
     */
    private String endDate;
}