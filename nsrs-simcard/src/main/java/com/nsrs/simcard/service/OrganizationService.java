package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.Organization;
import com.nsrs.simcard.model.dto.OrganizationDTO;
import com.nsrs.simcard.model.query.OrganizationQuery;
import com.nsrs.simcard.model.vo.OrganizationTreeVO;

import java.util.List;

/**
 * 组织服务接口
 */
public interface OrganizationService extends IService<Organization> {
    
    /**
     * 分页查询组织列表
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<OrganizationDTO> pageOrganization(PageRequest<OrganizationQuery> request);
    
    /**
     * 添加组织
     * @param organizationDTO 组织信息
     * @return 是否成功
     */
    boolean addOrganization(OrganizationDTO organizationDTO);
    
    /**
     * 修改组织
     * @param organizationDTO 组织信息
     * @return 是否成功
     */
    boolean updateOrganization(OrganizationDTO organizationDTO);
    
    /**
     * 删除组织
     * @param id 组织ID
     * @return 是否成功
     */
    boolean deleteOrganization(Long id);
    
    /**
     * 获取组织详情
     * @param id 组织ID
     * @return 组织详情
     */
    OrganizationDTO getOrganizationDetail(Long id);
    
    /**
     * 根据组织代码获取组织
     * @param code 组织代码
     * @return 组织信息
     */
    OrganizationDTO getOrganizationByCode(String code);
    
    /**
     * 获取组织树
     * @param parentId 父组织ID，为null时获取顶级组织
     * @return 组织树
     */
    List<OrganizationDTO> getOrganizationTree(Long parentId);
    
    /**
     * 获取组织树形结构
     * @param parentId 父组织ID，为null时获取顶级组织
     * @return 组织树形结构
     */
    List<OrganizationTreeVO> getOrganizationTreeStructure(Long parentId);
    
    /**
     * 修改组织状态
     * @param id 组织ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateOrganizationStatus(Long id, Integer status);
}