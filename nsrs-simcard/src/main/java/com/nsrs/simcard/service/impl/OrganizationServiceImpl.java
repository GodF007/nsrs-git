package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.simcard.entity.Organization;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.mapper.OrganizationMapper;
import com.nsrs.simcard.model.dto.OrganizationDTO;
import com.nsrs.simcard.model.query.OrganizationQuery;
import com.nsrs.simcard.model.vo.OrganizationTreeVO;
import com.nsrs.simcard.service.OrganizationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织服务实现类
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Override
    public PageResult<OrganizationDTO> pageOrganization(PageRequest<OrganizationQuery> request) {
        // 参数校验
        if (request == null || request.getQuery() == null) {
            throw new IllegalArgumentException("Request and query data cannot be null");
        }
        
        OrganizationQuery query = request.getQuery();
        
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.hasText(query.getName())) {
            queryWrapper.like(Organization::getName, query.getName());
        }
        
        if (StringUtils.hasText(query.getCode())) {
            queryWrapper.like(Organization::getCode, query.getCode());
        }
        
        if (query.getType() != null) {
            queryWrapper.eq(Organization::getType, query.getType());
        }
        
        if (query.getParentId() != null) {
            queryWrapper.eq(Organization::getParentId, query.getParentId());
        }
        
        if (StringUtils.hasText(query.getContactPerson())) {
            queryWrapper.like(Organization::getContactPerson, query.getContactPerson());
        }
        
        if (StringUtils.hasText(query.getContactPhone())) {
            queryWrapper.like(Organization::getContactPhone, query.getContactPhone());
        }
        
        if (query.getStatus() != null) {
            queryWrapper.eq(Organization::getStatus, query.getStatus());
        }
        
        // 处理日期范围查询
        if (StringUtils.hasText(query.getBeginDate())) {
            queryWrapper.ge(Organization::getCreateTime, query.getBeginDate());
        }
        
        if (StringUtils.hasText(query.getEndDate())) {
            queryWrapper.le(Organization::getCreateTime, query.getEndDate());
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Organization::getCreateTime);
        
        // 执行分页查询
        Page<Organization> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Organization> pageResult = page(page, queryWrapper);
        
        // 转换为DTO
        List<OrganizationDTO> orgDTOList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 返回分页结果
        return new PageResult<>(
            orgDTOList, 
            pageResult.getTotal(), 
            pageResult.getCurrent(), 
            pageResult.getSize()
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrganization(OrganizationDTO organizationDTO) {
        // 检查组织代码是否重复
        if (StringUtils.hasText(organizationDTO.getCode())) {
            LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Organization::getCode, organizationDTO.getCode());
            if (count(queryWrapper) > 0) {
                throw new BusinessException(ErrorMessageEnum.ORG_CODE_ALREADY_EXISTS.getMessage());
            }
        }
        
        // 检查父组织是否存在
        if (organizationDTO.getParentId() != null && organizationDTO.getParentId() > 0) {
            Organization parent = getById(organizationDTO.getParentId());
            if (parent == null) {
                throw new BusinessException(ErrorMessageEnum.PARENT_ORG_NOT_EXISTS.getMessage());
            }
        }
        
        // 创建组织实体并设置属性
        Organization organization = new Organization();
        BeanUtils.copyProperties(organizationDTO, organization);
        
        // 设置状态，默认启用
        if (organization.getStatus() == null) {
            organization.setStatus(1);
        }
        
        // 设置创建和更新时间
        Date now = new Date();
        organization.setCreateTime(now);
        organization.setUpdateTime(now);
        
        // 保存组织
        return save(organization);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrganization(OrganizationDTO organizationDTO) {
        // 检查组织是否存在
        Organization existingOrg = getById(organizationDTO.getId());
        if (existingOrg == null) {
            throw new BusinessException(ErrorMessageEnum.ORG_NOT_EXISTS.getMessage());
        }
        
        // 检查组织代码是否重复
        if (StringUtils.hasText(organizationDTO.getCode()) 
                && !organizationDTO.getCode().equals(existingOrg.getCode())) {
            LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Organization::getCode, organizationDTO.getCode());
            if (count(queryWrapper) > 0) {
                throw new BusinessException(ErrorMessageEnum.ORG_CODE_ALREADY_EXISTS.getMessage());
            }
        }
        
        // 检查父组织是否存在
        if (organizationDTO.getParentId() != null && organizationDTO.getParentId() > 0) {
            // 检查是否选择自己作为父节点
            if (organizationDTO.getParentId().equals(organizationDTO.getId())) {
                throw new BusinessException(ErrorMessageEnum.CANNOT_SELECT_SELF_AS_PARENT.getMessage());
            }
            
            // 只有当父组织ID发生变化时才检查循环引用
            if (!organizationDTO.getParentId().equals(existingOrg.getParentId())) {
                // 检查是否形成循环引用
                if (checkCircularReference(organizationDTO.getId(), organizationDTO.getParentId())) {
                    throw new BusinessException(ErrorMessageEnum.CANNOT_SELECT_CHILD_AS_PARENT.getMessage());
                }
            }
            
            Organization parent = getById(organizationDTO.getParentId());
            if (parent == null) {
                throw new BusinessException(ErrorMessageEnum.PARENT_ORG_NOT_EXISTS.getMessage());
            }
        }
        
        // 更新组织实体
        Organization organization = new Organization();
        BeanUtils.copyProperties(organizationDTO, organization);
        
        // 设置更新时间
        organization.setUpdateTime(new Date());
        
        // 更新组织
        return updateById(organization);
    }
    
    /**
     * 检查是否形成循环引用
     * @param orgId 组织ID
     * @param parentId 父组织ID
     * @return 是否形成循环引用
     */
    private boolean checkCircularReference(Long orgId, Long parentId) {
        // 如果父组织ID为空或0，不存在循环引用
        if (parentId == null || parentId <= 0) {
            return false;
        }
        
        // 获取当前组织的所有子组织（包括子子组织）
        List<Organization> allChildren = getAllChildren(orgId);
        
        // 检查要设置的父组织是否在当前组织的子组织中
        return allChildren.stream().anyMatch(org -> org.getId().equals(parentId));
    }
    
    /**
     * 获取组织的所有子组织
     * @param orgId 组织ID
     * @return 子组织列表
     */
    private List<Organization> getAllChildren(Long orgId) {
        List<Organization> result = new ArrayList<>();
        
        // 查询直接子组织
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getParentId, orgId);
        List<Organization> directChildren = list(queryWrapper);
        
        if (!directChildren.isEmpty()) {
            result.addAll(directChildren);
            
            // 递归查询子组织的子组织
            for (Organization child : directChildren) {
                result.addAll(getAllChildren(child.getId()));
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrganization(Long id) {
        // 检查组织是否存在
        Organization org = getById(id);
        if (org == null) {
            throw new BusinessException(ErrorMessageEnum.ORG_NOT_EXISTS.getMessage());
        }
        
        // 检查是否有子组织
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getParentId, id);
        if (count(queryWrapper) > 0) {
            throw new BusinessException(ErrorMessageEnum.ORG_HAS_CHILDREN_CANNOT_DELETE.getMessage());
        }
        
        // 删除组织
        return removeById(id);
    }
    
    @Override
    public OrganizationDTO getOrganizationDetail(Long id) {
        // 获取组织信息
        Organization org = getById(id);
        if (org == null) {
            return null;
        }
        
        // 转换为DTO
        OrganizationDTO dto = convertToDTO(org);
        
        // 如果有父组织，查询父组织名称
        if (org.getParentId() != null && org.getParentId() > 0) {
            Organization parent = getById(org.getParentId());
            if (parent != null) {
                dto.setParentName(parent.getName());
            }
        }
        
        return dto;
    }
    
    @Override
    public OrganizationDTO getOrganizationByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        
        // 查询组织
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getCode, code);
        Organization org = getOne(queryWrapper);
        
        if (org == null) {
            return null;
        }
        
        // 转换为DTO并返回
        return convertToDTO(org);
    }
    
    @Override
    public List<OrganizationDTO> getOrganizationTree(Long parentId) {
        // 查询组织列表
        List<Organization> allOrgs = list();
        
        // 转换为DTO并返回扁平列表
        return allOrgs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrganizationTreeVO> getOrganizationTreeStructure(Long parentId) {
        // 查询组织列表
        List<Organization> allOrgs = list();
        
        // 直接构建并返回树形结构
        return buildTree(allOrgs, parentId);
    }
    
    /**
     * 构建组织树
     * @param allOrgs 所有组织
     * @param parentId 父组织ID
     * @return 组织树
     */
    private List<OrganizationTreeVO> buildTree(List<Organization> allOrgs, Long parentId) {
        List<OrganizationTreeVO> result = new ArrayList<>();
        
        // 筛选出父节点下的子节点
        List<Organization> children = allOrgs.stream()
                .filter(org -> (parentId == null && (org.getParentId() == null || org.getParentId() == 0)) 
                        || (parentId != null && parentId.equals(org.getParentId())))
                .collect(Collectors.toList());
        
        // 递归构建树
        for (Organization org : children) {
            OrganizationTreeVO treeNode = new OrganizationTreeVO();
            BeanUtils.copyProperties(org, treeNode);
            
            // 设置类型名称
            if (org.getType() != null) {
                treeNode.setTypeName(getOrgTypeName(org.getType()));
            }
            
            // 设置状态名称
            if (org.getStatus() != null) {
                treeNode.setStatusName(org.getStatus() == 1 ? "Enabled" : "Disabled");
            }
            
            // 递归设置子节点
            treeNode.setChildren(buildTree(allOrgs, org.getId()));
            
            result.add(treeNode);
        }
        
        return result;
    }
    

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrganizationStatus(Long id, Integer status) {
        // 检查组织是否存在
        Organization org = getById(id);
        if (org == null) {
            throw new BusinessException(ErrorMessageEnum.ORG_NOT_EXISTS.getMessage());
        }
        
        // 更新状态
        org.setStatus(status);
        org.setUpdateTime(new Date());
        
        return updateById(org);
    }
    
    /**
     * 将实体转换为DTO
     * @param org 组织实体
     * @return 组织DTO
     */
    private OrganizationDTO convertToDTO(Organization org) {
        if (org == null) {
            return null;
        }
        
        OrganizationDTO dto = new OrganizationDTO();
        BeanUtils.copyProperties(org, dto);
        
        // 设置类型名称
        if (org.getType() != null) {
            dto.setTypeName(getOrgTypeName(org.getType()));
        }
        
        // 设置状态名称
        if (org.getStatus() != null) {
            dto.setStatusName(org.getStatus() == 1 ? "Enabled" : "Disabled");
        }
        
        return dto;
    }
    
    /**
     * 获取组织类型名称
     * @param type 组织类型
     * @return 类型名称
     */
    private String getOrgTypeName(Integer type) {
        switch (type) {
            case 1: return "Company";
            case 2: return "Department";
            case 3: return "Branch";
            case 4: return "Partner";
            default: return "Unknown";
        }
    }
}