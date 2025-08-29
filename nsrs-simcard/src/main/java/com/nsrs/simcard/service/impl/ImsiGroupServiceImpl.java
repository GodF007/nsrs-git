package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.simcard.entity.ImsiGroup;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.mapper.ImsiGroupMapper;
import com.nsrs.simcard.model.dto.ImsiGroupDTO;
import com.nsrs.simcard.model.query.ImsiGroupQuery;
import com.nsrs.simcard.service.ImsiGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * IMSI组服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImsiGroupServiceImpl extends ServiceImpl<ImsiGroupMapper, ImsiGroup> implements ImsiGroupService {
    
    @Override
    public PageResult<ImsiGroupDTO> pageImsiGroup(PageRequest<ImsiGroupQuery> request) {
        // 参数校验
        if (request == null || request.getQuery() == null) {
            throw new IllegalArgumentException("Request and query data cannot be null");
        }
        
        ImsiGroupQuery query = request.getQuery();
        
        LambdaQueryWrapper<ImsiGroup> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.isNotBlank(query.getGroupName())) {
            queryWrapper.like(ImsiGroup::getGroupName, query.getGroupName());
        }
        
        if (StringUtils.isNotBlank(query.getImsiPrefix())) {
            queryWrapper.like(ImsiGroup::getImsiPrefix, query.getImsiPrefix());
        }
        
        if (query.getImsiType() != null) {
            queryWrapper.eq(ImsiGroup::getImsiType, query.getImsiType());
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(ImsiGroup::getCreateTime);
        
        // 分页查询
        Page<ImsiGroup> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<ImsiGroup> pageResult = this.page(page, queryWrapper);
        
        // 转换为DTO
        List<ImsiGroupDTO> dtoList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageResult<ImsiGroupDTO> result = new PageResult<>();
        result.setList(dtoList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(request.getCurrent());
        result.setPageSize(request.getSize());
        result.setPages((pageResult.getTotal() + request.getSize() - 1) / request.getSize());
        
        return result;
    }
    
    @Override
    public ImsiGroupDTO getImsiGroupDetail(Long groupId) {
        ImsiGroup imsiGroup = this.getById(groupId);
        return imsiGroup != null ? convertToDTO(imsiGroup) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addImsiGroup(ImsiGroupDTO groupDTO) {
        // 验证组名称是否重复
        LambdaQueryWrapper<ImsiGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiGroup::getGroupName, groupDTO.getGroupName());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorMessageEnum.GROUP_NAME_ALREADY_EXISTS.getMessage());
        }
        
        // 验证IMSI范围
        if (StringUtils.isNotBlank(groupDTO.getImsiStart()) && StringUtils.isNotBlank(groupDTO.getImsiEnd())) {
            long start = Long.parseLong(groupDTO.getImsiStart());
            long end = Long.parseLong(groupDTO.getImsiEnd());
            if (start > end) {
                throw new BusinessException(ErrorMessageEnum.IMSI_START_GREATER_THAN_END.getMessage());
            }
            
            // 计算总数量
            int totalCount = (int) (end - start + 1);
            groupDTO.setTotalCount(totalCount);
        }
        
        ImsiGroup imsiGroup = new ImsiGroup();
        BeanUtils.copyProperties(groupDTO, imsiGroup);
        
        // 设置默认值
        imsiGroup.setUsedCount(0);
        imsiGroup.setCreateTime(new Date());
        imsiGroup.setUpdateTime(new Date());
        
        return this.save(imsiGroup);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateImsiGroup(ImsiGroupDTO groupDTO) {
        ImsiGroup existingGroup = this.getById(groupDTO.getGroupId());
        if (existingGroup == null) {
            throw new BusinessException(ErrorMessageEnum.IMSI_GROUP_NOT_EXISTS.getMessage());
        }
        
        // 验证组名称是否重复（排除自身）
        if (!existingGroup.getGroupName().equals(groupDTO.getGroupName())) {
            LambdaQueryWrapper<ImsiGroup> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImsiGroup::getGroupName, groupDTO.getGroupName());
            queryWrapper.ne(ImsiGroup::getGroupId, groupDTO.getGroupId());
            if (this.count(queryWrapper) > 0) {
                throw new BusinessException(ErrorMessageEnum.GROUP_NAME_ALREADY_EXISTS.getMessage());
            }
        }
        
        // 验证IMSI范围
        if (StringUtils.isNotBlank(groupDTO.getImsiStart()) && StringUtils.isNotBlank(groupDTO.getImsiEnd())) {
            long start = Long.parseLong(groupDTO.getImsiStart());
            long end = Long.parseLong(groupDTO.getImsiEnd());
            if (start > end) {
                throw new BusinessException(ErrorMessageEnum.IMSI_START_GREATER_THAN_END.getMessage());
            }
            
            // 计算总数量
            int totalCount = (int) (end - start + 1);
            groupDTO.setTotalCount(totalCount);
        }
        
        BeanUtils.copyProperties(groupDTO, existingGroup);
        existingGroup.setUpdateTime(new Date());
        
        return this.updateById(existingGroup);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteImsiGroup(Long groupId) {
        // TODO: 验证是否有关联的IMSI资源，有则不允许删除
        return this.removeById(groupId);
    }
    
    @Override
    public List<ImsiGroupDTO> listAllImsiGroup() {
        List<ImsiGroup> list = this.list();
        return list.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public ImsiGroup validateImsiGroup(Long groupId) {
        ImsiGroup imsiGroup = this.getById(groupId);
        if (imsiGroup == null) {
            throw new BusinessException(ErrorMessageEnum.IMSI_GROUP_NOT_EXISTS.getMessage());
        }
        
        // 验证IMSI范围
        if (StringUtils.isBlank(imsiGroup.getImsiStart()) || StringUtils.isBlank(imsiGroup.getImsiEnd())) {
            throw new BusinessException(ErrorMessageEnum.IMSI_RANGE_INCOMPLETE.getMessage());
        }
        
        // 验证是否还有可用容量
        if (imsiGroup.getUsedCount() >= imsiGroup.getTotalCount()) {
            throw new BusinessException(ErrorMessageEnum.IMSI_GROUP_EXHAUSTED.getMessage());
        }
        
        return imsiGroup;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUsedCount(Long groupId, int increment) {
        if (groupId == null) {
            return false;
        }
        
        ImsiGroup imsiGroup = this.getById(groupId);
        if (imsiGroup == null) {
            return false;
        }
        
        int newUsedCount = imsiGroup.getUsedCount() + increment;
        if (newUsedCount < 0) {
            newUsedCount = 0;
        }
        
        imsiGroup.setUsedCount(newUsedCount);
        imsiGroup.setUpdateTime(new Date());
        
        return this.updateById(imsiGroup);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAvailableCount(Long groupId, int increment) {
        if (groupId == null) {
            return false;
        }
        
        ImsiGroup imsiGroup = this.getById(groupId);
        if (imsiGroup == null) {
            return false;
        }
        
        // 计算新的可用数量（总数量 - 已使用数量）
        int currentAvailable = imsiGroup.getTotalCount() - imsiGroup.getUsedCount();
        int newAvailable = currentAvailable + increment;
        
        // 确保可用数量不为负数，且不超过总数量
        if (newAvailable < 0) {
            newAvailable = 0;
        } else if (newAvailable > imsiGroup.getTotalCount()) {
            newAvailable = imsiGroup.getTotalCount();
        }
        
        // 反推已使用数量
        int newUsedCount = imsiGroup.getTotalCount() - newAvailable;
        
        imsiGroup.setUsedCount(newUsedCount);
        imsiGroup.setUpdateTime(new Date());
        
        return this.updateById(imsiGroup);
    }
    
    /**
     * 将实体转换为DTO
     */
    private ImsiGroupDTO convertToDTO(ImsiGroup imsiGroup) {
        if (imsiGroup == null) {
            return null;
        }
        
        ImsiGroupDTO dto = new ImsiGroupDTO();
        BeanUtils.copyProperties(imsiGroup, dto);
        
        // 设置IMSI类型描述
        switch (imsiGroup.getImsiType()) {
            case 1:
                dto.setImsiTypeDesc("GSM Postpaid");
                break;
            case 2:
                dto.setImsiTypeDesc("GSM Prepaid");
                break;
            case 3:
                dto.setImsiTypeDesc("CDMA");
                break;
            default:
                dto.setImsiTypeDesc("Unknown");
        }
        
        // 计算剩余数量
        int remainingCount = imsiGroup.getTotalCount() - imsiGroup.getUsedCount();
        dto.setRemainingCount(remainingCount);
        
        // 计算使用率
        if (imsiGroup.getTotalCount() > 0) {
            double usageRate = (double) imsiGroup.getUsedCount() / imsiGroup.getTotalCount() * 100;
            DecimalFormat df = new DecimalFormat("0.00");
            dto.setUsageRate(df.format(usageRate) + "%");
        } else {
            dto.setUsageRate("0.00%");
        }
        
        return dto;
    }
}