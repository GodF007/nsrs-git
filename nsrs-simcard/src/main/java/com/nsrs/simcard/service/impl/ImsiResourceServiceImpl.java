package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.simcard.constant.ImsiConstant;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.enums.ImsiStatusEnum;
import com.nsrs.simcard.enums.ImsiTypeEnum;
import com.nsrs.simcard.entity.ImsiGroup;
import com.nsrs.simcard.entity.ImsiResource;
import com.nsrs.simcard.entity.Supplier;
import com.nsrs.simcard.mapper.ImsiResourceMapper;
import com.nsrs.simcard.model.dto.ImsiResourceDTO;
import com.nsrs.simcard.model.query.ImsiResourceQuery;
import com.nsrs.simcard.model.request.ImsiGenerateRequest;
import com.nsrs.simcard.service.ImsiGroupService;
import com.nsrs.simcard.service.ImsiResourceService;
import com.nsrs.simcard.service.SupplierService;
import com.nsrs.common.utils.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * IMSI资源服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImsiResourceServiceImpl extends ServiceImpl<ImsiResourceMapper, ImsiResource> implements ImsiResourceService {
    
    private final ImsiGroupService imsiGroupService;
    private final SupplierService supplierService;
    private final SequenceService sequenceService;
    
    @Override
    public PageResult<ImsiResourceDTO> pageImsiResource(PageRequest<ImsiResourceQuery> request) {
        // 参数校验
        if (request == null || request.getQuery() == null) {
            throw new IllegalArgumentException("Request and query data cannot be null");
        }
        
        ImsiResourceQuery query = request.getQuery();
        
        // IMSI为必输查询条件
        if (StringUtils.isBlank(query.getImsi())) {
            throw new BusinessException(ErrorMessageEnum.IMSI_REQUIRED.getMessage());
        }
        
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        queryWrapper.eq(ImsiResource::getImsi, query.getImsi());
        
        if (query.getImsiType() != null) {
            queryWrapper.eq(ImsiResource::getImsiType, query.getImsiType());
        }
        
        if (query.getGroupId() != null) {
            queryWrapper.eq(ImsiResource::getGroupId, query.getGroupId());
        }
        
        if (query.getSupplierId() != null) {
            queryWrapper.eq(ImsiResource::getSupplierId, query.getSupplierId());
        }
        
        if (query.getStatus() != null) {
            queryWrapper.eq(ImsiResource::getStatus, query.getStatus());
        }
        
        // 分页查询
        Page<ImsiResource> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<ImsiResource> pageResult = this.page(page, queryWrapper);
        
        // 批量查询关联数据，避免N+1查询问题
        List<ImsiResource> records = pageResult.getRecords();
        Map<Long, ImsiGroup> groupMap = batchQueryGroups(records);
        Map<Long, Supplier> supplierMap = batchQuerySuppliers(records);
        
        // 转换为DTO，使用缓存的关联数据
        List<ImsiResourceDTO> dtoList = records.stream()
                .map(resource -> convertToDTOWithCache(resource, groupMap, supplierMap))
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageResult<ImsiResourceDTO> result = new PageResult<>();
        result.setList(dtoList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(request.getCurrent());
        result.setPageSize(request.getSize());
        result.setPages((pageResult.getTotal() + request.getSize() - 1) / request.getSize());
        
        return result;
    }
    
    @Override
    public ImsiResourceDTO getImsiResourceDetail(Long imsiId) {
        // 由于分表，不能直接通过ID查询，需要提供基于IMSI的查询方法
        throw new BusinessException("Please use IMSI number to query resource detail");
    }
    
    /**
     * 根据IMSI号码获取资源详情
     */
    public ImsiResourceDTO getImsiResourceDetailByImsi(String imsi) {
        if (StringUtils.isBlank(imsi)) {
            return null;
        }
        
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getImsi, imsi);
        
        ImsiResource imsiResource = this.getOne(queryWrapper);
        return imsiResource != null ? convertToDTOSingle(imsiResource) : null;
    }
    
    @Override
    public ImsiResourceDTO getImsiResourceByImsi(String imsi) {
        if (StringUtils.isBlank(imsi)) {
            return null;
        }
        
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getImsi, imsi);
        
        ImsiResource imsiResource = this.getOne(queryWrapper);
        return imsiResource != null ? convertToDTOSingle(imsiResource) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addImsiResource(ImsiResourceDTO resourceDTO) {
        // Validate if IMSI already exists
        if (isImsiExists(resourceDTO.getImsi())) {
            throw new BusinessException("IMSI already exists");
        }
        
        // Validate group ID
        if (resourceDTO.getGroupId() != null) {
            ImsiGroup imsiGroup = imsiGroupService.getById(resourceDTO.getGroupId());
            if (imsiGroup == null) {
                throw new BusinessException("IMSI group not found");
            }
        }
        
        // Validate supplier ID
        if (resourceDTO.getSupplierId() != null) {
            Supplier supplier = supplierService.getById(resourceDTO.getSupplierId());
            if (supplier == null) {
                throw new BusinessException("Supplier not found");
            }
        }
        
        ImsiResource imsiResource = new ImsiResource();
        BeanUtils.copyProperties(resourceDTO, imsiResource);
        
        // Generate ID using global sequence
        Long globalId = sequenceService.getNextSequenceValue("imsi_resource_id_seq");
        imsiResource.setImsiId(globalId);
        
        // Set default values
        if (imsiResource.getStatus() == null) {
            imsiResource.setStatus(ImsiConstant.STATUS_IDLE);
        }
        imsiResource.setCreateTime(new Date());
        imsiResource.setUpdateTime(new Date());
        
        boolean result = this.save(imsiResource);
        
        // Update IMSI group used count
        if (result && imsiResource.getGroupId() != null) {
            imsiGroupService.updateUsedCount(imsiResource.getGroupId(), 1);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateImsiResource(ImsiResourceDTO resourceDTO) {
        throw new BusinessException("Please use IMSI-based update interface");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateImsiResourceByImsi(ImsiResourceDTO resourceDTO) {
        if (StringUtils.isBlank(resourceDTO.getImsi())) {
            throw new BusinessException("IMSI number cannot be empty");
        }
        
        // Query existing resource first
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getImsi, resourceDTO.getImsi());
        ImsiResource existingResource = this.getOne(queryWrapper);
        
        if (existingResource == null) {
            throw new BusinessException("IMSI resource not found");
        }
        
        // Validate group ID
        if (resourceDTO.getGroupId() != null) {
            ImsiGroup imsiGroup = imsiGroupService.getById(resourceDTO.getGroupId());
            if (imsiGroup == null) {
                throw new BusinessException("IMSI group not found");
            }
        }
        
        // Validate supplier ID
        if (resourceDTO.getSupplierId() != null) {
            Supplier supplier = supplierService.getById(resourceDTO.getSupplierId());
            if (supplier == null) {
                throw new BusinessException("Supplier not found");
            }
        }
        
        // Record original status and group ID for inventory update
        Integer oldStatus = existingResource.getStatus();
        Long oldGroupId = existingResource.getGroupId();
        
        // Update resource information
        BeanUtils.copyProperties(resourceDTO, existingResource, "imsiId", "createTime", "createUserId");
        existingResource.setUpdateTime(new Date());
        
        // 使用基于IMSI的更新方式，避免分表问题
        LambdaQueryWrapper<ImsiResource> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(ImsiResource::getImsi, resourceDTO.getImsi());
        boolean result = this.update(existingResource, updateWrapper);
        
        // Update inventory information if status or group changed
        if (result && (oldStatus != resourceDTO.getStatus() || !java.util.Objects.equals(oldGroupId, resourceDTO.getGroupId()))) {
            // Update IMSI group used count
            if (!java.util.Objects.equals(oldGroupId, resourceDTO.getGroupId())) {
                if (oldGroupId != null) {
                    imsiGroupService.updateUsedCount(oldGroupId, -1);
                }
                if (resourceDTO.getGroupId() != null) {
                    imsiGroupService.updateUsedCount(resourceDTO.getGroupId(), 1);
                }
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteImsiResource(Long imsiId) {
        throw new BusinessException("Please use IMSI-based delete interface");
    }
    
    /**
     * Delete resource by IMSI number
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteImsiResourceByImsi(String imsi) {
        if (StringUtils.isBlank(imsi)) {
            throw new BusinessException("IMSI number cannot be empty");
        }
        
        // Query IMSI resource
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getImsi, imsi);
        ImsiResource imsiResource = this.getOne(queryWrapper);
        
        if (imsiResource == null) {
            throw new BusinessException("IMSI resource not found");
        }
        
        // Check IMSI resource status, only idle status can be deleted
        if (!Objects.equals(ImsiConstant.STATUS_IDLE, imsiResource.getStatus())) {
            throw new BusinessException("Only idle IMSI resources can be deleted");
        }
        
        boolean result = this.remove(queryWrapper);
        
        // Update IMSI group used count
        if (result && imsiResource.getGroupId() != null) {
            imsiGroupService.updateUsedCount(imsiResource.getGroupId(), -1);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateImsiStatus(Long imsiId, Integer status) {
        throw new BusinessException("Please use IMSI-based status update interface");
    }
    
    /**
     * Update status by IMSI number
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateImsiStatusByImsi(String imsi, Integer status) {
        if (StringUtils.isBlank(imsi)) {
            throw new BusinessException("IMSI number cannot be empty");
        }
        
        if (status == null || !ImsiStatusEnum.isValidCode(status)) {
            throw new BusinessException("Invalid IMSI status");
        }
        
        // Query IMSI resource
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getImsi, imsi);
        ImsiResource imsiResource = this.getOne(queryWrapper);
        
        if (imsiResource == null) {
            throw new BusinessException("IMSI resource not found");
        }
        
        Integer oldStatus = imsiResource.getStatus();
        imsiResource.setStatus(status);
        imsiResource.setUpdateTime(new Date());
        
        // 使用基于IMSI的更新方式，避免分表问题
        LambdaQueryWrapper<ImsiResource> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(ImsiResource::getImsi, imsi);
        boolean result = this.update(imsiResource, updateWrapper);
        
        // Update IMSI group inventory if status changed
        if (result && !java.util.Objects.equals(oldStatus, status)) {
            updateGroupInventoryByStatus(oldStatus, status, imsiResource.getGroupId());
        }
        
        return result;
    }
    
    /**
     * Update group inventory information based on status change
     */
    private void updateGroupInventoryByStatus(Integer oldStatus, Integer newStatus, Long groupId) {
        if (groupId == null) {
            return;
        }
        
        // If changed from non-idle to idle status, increase available count
        if (!Objects.equals(ImsiConstant.STATUS_IDLE, oldStatus) && Objects.equals(ImsiConstant.STATUS_IDLE, newStatus)) {
            imsiGroupService.updateAvailableCount(groupId, 1);
        }
        // If changed from idle to non-idle status, decrease available count
        else if (Objects.equals(ImsiConstant.STATUS_IDLE, oldStatus) && !Objects.equals(ImsiConstant.STATUS_IDLE, newStatus)) {
            imsiGroupService.updateAvailableCount(groupId, -1);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImsiResourceDTO> generateImsi(ImsiGenerateRequest request) {
        // Validate parameters
        if (request.getGroupId() == null) {
            throw new BusinessException("Group ID is required");
        }
        
        if (request.getCount() <= 0 || request.getCount() > 10000) {
            throw new BusinessException("Invalid generate count, must be between 1 and 10000");
        }
        
        // Validate IMSI group
        ImsiGroup imsiGroup = imsiGroupService.validateImsiGroup(request.getGroupId());
        
        // Validate if remaining capacity is sufficient
        int remainingCount = imsiGroup.getTotalCount() - imsiGroup.getUsedCount();
        if (remainingCount < request.getCount()) {
            throw new BusinessException("Insufficient IMSI capacity, remaining: " + remainingCount);
        }
        
        // Validate supplier
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierService.getById(request.getSupplierId());
            if (supplier == null) {
                throw new BusinessException("Supplier not found");
            }
        }
        
        // Get maximum existing IMSI in current group
        String maxImsi = baseMapper.getMaxImsiByGroupId(request.getGroupId());
        long nextImsiValue;
        
        if (StringUtils.isNotBlank(maxImsi)) {
            // Extract numeric part from maxImsi (remove prefix if exists)
            String numericPart = extractNumericPart(maxImsi, imsiGroup.getImsiPrefix());
            if (StringUtils.isNotBlank(numericPart)) {
                nextImsiValue = Long.parseLong(numericPart) + 1;
            } else {
                // If can't extract numeric part, start from range start value
                nextImsiValue = Long.parseLong(imsiGroup.getImsiStart());
            }
        } else {
            // If none exists, start from range start value
            // Extract numeric part from imsiStart (remove prefix if exists)
            String startNumericPart = extractNumericPart(imsiGroup.getImsiStart(), imsiGroup.getImsiPrefix());
            if (StringUtils.isNotBlank(startNumericPart)) {
                nextImsiValue = Long.parseLong(startNumericPart);
            } else {
                // If can't extract numeric part, use the original start value
                nextImsiValue = Long.parseLong(imsiGroup.getImsiStart());
            }
        }
        
        // Validate if exceeds range
        // Extract numeric part from imsiEnd (remove prefix if exists)
        String endNumericPart = extractNumericPart(imsiGroup.getImsiEnd(), imsiGroup.getImsiPrefix());
        long endImsiValue;
        if (StringUtils.isNotBlank(endNumericPart)) {
            endImsiValue = Long.parseLong(endNumericPart);
        } else {
            // If can't extract numeric part, use the original end value
            endImsiValue = Long.parseLong(imsiGroup.getImsiEnd());
        }
        
        if (nextImsiValue + request.getCount() - 1 > endImsiValue) {
            throw new BusinessException("Generate count exceeds IMSI range");
        }
        
        // Batch get global sequence IDs
        List<Long> globalIds = sequenceService.getBatchSequenceValues("imsi_resource_id_seq", request.getCount());
        
        // Start generating IMSI
        List<ImsiResource> imsiResources = new ArrayList<>();
        Date now = new Date();
        
        for (int i = 0; i < request.getCount(); i++) {
            String imsi = String.valueOf(nextImsiValue + i);
            
            /*// Pad with zeros if length is insufficient
            if (imsi.length() < imsiGroup.getImsiStart().length()) {
                imsi = StringUtils.leftPad(imsi, imsiGroup.getImsiStart().length(), '0');
            }*/
            
            // Add prefix if IMSI prefix exists
            if (StringUtils.isNotBlank(imsiGroup.getImsiPrefix())) {
                imsi = imsiGroup.getImsiPrefix() + imsi;
            }
            
            ImsiResource imsiResource = new ImsiResource();
            imsiResource.setImsiId(globalIds.get(i));  // Set global sequence ID
            imsiResource.setImsi(imsi);
            imsiResource.setImsiType(imsiGroup.getImsiType());
            imsiResource.setGroupId(request.getGroupId());
            imsiResource.setSupplierId(request.getSupplierId());
            imsiResource.setStatus(ImsiConstant.STATUS_IDLE);
            imsiResource.setCreateTime(now);
            imsiResource.setUpdateTime(now);
            imsiResource.setCreateUserId(request.getOperatorUserId());
            imsiResource.setUpdateUserId(request.getOperatorUserId());
            
            imsiResources.add(imsiResource);
        }
        
        // Batch save
        if (imsiResources.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Save in batches, maximum 1000 records per batch
        int batchSize = 1000;
        for (int i = 0; i < imsiResources.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, imsiResources.size());
            List<ImsiResource> batch = imsiResources.subList(i, toIndex);
            baseMapper.batchInsert(batch);
        }
        
        // Update IMSI group usage count
        imsiGroupService.updateUsedCount(request.getGroupId(), request.getCount());
        
        // Convert to DTO and return，使用批量查询避免N+1问题
        Map<Long, ImsiGroup> groupMap = batchQueryGroups(imsiResources);
        Map<Long, Supplier> supplierMap = batchQuerySuppliers(imsiResources);
        
        return imsiResources.stream()
                .map(resource -> convertToDTOWithCache(resource, groupMap, supplierMap))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateImsiStatus(List<Long> imsiIds, Integer status) {
        throw new BusinessException("Please use IMSI-based batch status update interface");
    }
    
    /**
     * Batch update status by IMSI numbers - Optimized version
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateImsiStatusByImsi(List<String> imsiList, Integer status) {
        if (imsiList == null || imsiList.isEmpty()) {
            return false;
        }
        
        if (status == null || !ImsiStatusEnum.isValidCode(status)) {
            throw new BusinessException("Invalid IMSI status");
        }
        
        // Batch query existing IMSI resources
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ImsiResource::getImsi, imsiList);
        List<ImsiResource> existingResources = this.list(queryWrapper);
        
        if (existingResources.isEmpty()) {
            log.warn("No IMSI resources found for batch update: {}", imsiList);
            return false;
        }
        
        // Group by groupId and track status changes for inventory update
        Map<Long, Integer> groupInventoryChanges = new HashMap<>();
        Date now = new Date();
        List<ImsiResource> resourcesToUpdate = new ArrayList<>();
        
        for (ImsiResource resource : existingResources) {
            Integer oldStatus = resource.getStatus();
            
            // Only update if status actually changed
            if (!Objects.equals(oldStatus, status)) {
                resource.setStatus(status);
                resource.setUpdateTime(now);
                resourcesToUpdate.add(resource);
                
                // Track inventory changes by group
                Long groupId = resource.getGroupId();
                if (groupId != null) {
                    int inventoryChange = 0;
                    // If changed from non-idle to idle status, increase available count
                    if (!Objects.equals(ImsiConstant.STATUS_IDLE, oldStatus) && Objects.equals(ImsiConstant.STATUS_IDLE, status)) {
                        inventoryChange = 1;
                    }
                    // If changed from idle to non-idle status, decrease available count
                    else if (Objects.equals(ImsiConstant.STATUS_IDLE, oldStatus) && !Objects.equals(ImsiConstant.STATUS_IDLE, status)) {
                        inventoryChange = -1;
                    }
                    
                    if (inventoryChange != 0) {
                        groupInventoryChanges.merge(groupId, inventoryChange, Integer::sum);
                    }
                }
            }
        }
        
        if (resourcesToUpdate.isEmpty()) {
            log.info("No IMSI resources need status update");
            return false;
        }
        
        // Batch update resources using IMSI-based approach to avoid sharding issues
        boolean updateResult = true;
        for (ImsiResource resource : resourcesToUpdate) {
            LambdaQueryWrapper<ImsiResource> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(ImsiResource::getImsi, resource.getImsi());
            if (!this.update(resource, updateWrapper)) {
                updateResult = false;
                log.error("Failed to update IMSI resource: {}", resource.getImsi());
                break;
            }
        }
        
        // Update group inventory if batch update successful
        if (updateResult && !groupInventoryChanges.isEmpty()) {
            for (Map.Entry<Long, Integer> entry : groupInventoryChanges.entrySet()) {
                try {
                    imsiGroupService.updateAvailableCount(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    log.error("Failed to update group inventory: groupId={}, change={}", 
                            entry.getKey(), entry.getValue(), e);
                }
            }
        }
        
        log.info("Batch updated {} IMSI resources status to {}", resourcesToUpdate.size(), status);
        return updateResult;
    }
    
    @Override
    public List<ImsiResourceDTO> listImsiByGroupId(Long groupId) {
        if (groupId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getGroupId, groupId);
        
        List<ImsiResource> resources = this.list(queryWrapper);
        
        // 批量查询关联数据，避免N+1查询问题
        Map<Long, ImsiGroup> groupMap = batchQueryGroups(resources);
        Map<Long, Supplier> supplierMap = batchQuerySuppliers(resources);
        
        return resources.stream()
                .map(resource -> convertToDTOWithCache(resource, groupMap, supplierMap))
                .collect(Collectors.toList());
    }
    
    @Override
    public int countAvailableImsiByGroupId(Long groupId) {
        if (groupId == null) {
            return 0;
        }
        
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getGroupId, groupId);
        queryWrapper.eq(ImsiResource::getStatus, ImsiConstant.STATUS_IDLE);
        
        return Math.toIntExact(this.count(queryWrapper));
    }
    
    /**
     * Validate if IMSI already exists
     */
    private boolean isImsiExists(String imsi) {
        if (StringUtils.isBlank(imsi)) {
            return false;
        }
        
        LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImsiResource::getImsi, imsi);
        
        return this.count(queryWrapper) > 0;
    }
    
    /**
     * 从完整IMSI中提取数字部分（去除前缀）
     * 
     * @param fullImsi 完整的IMSI号码
     * @param prefix IMSI前缀
     * @return 数字部分
     */
    private String extractNumericPart(String fullImsi, String prefix) {
        if (StringUtils.isBlank(fullImsi)) {
            return null;
        }
        
        // 如果有前缀，去除前缀
        if (StringUtils.isNotBlank(prefix) && fullImsi.startsWith(prefix)) {
            return fullImsi.substring(prefix.length());
        }
        
        // 如果没有前缀或者不以前缀开头，直接返回原字符串
        return fullImsi;
    }
    

    
    /**
     * Batch query groups to avoid N+1 query problem
     */
    private Map<Long, ImsiGroup> batchQueryGroups(List<ImsiResource> resources) {
        List<Long> groupIds = resources.stream()
                .map(ImsiResource::getGroupId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (groupIds.isEmpty()) {
            return new HashMap<>();
        }
        
        return imsiGroupService.listByIds(groupIds).stream()
                .collect(Collectors.toMap(ImsiGroup::getGroupId, group -> group));
    }
    
    /**
     * Batch query suppliers to avoid N+1 query problem
     */
    private Map<Long, Supplier> batchQuerySuppliers(List<ImsiResource> resources) {
        List<Long> supplierIds = resources.stream()
                .map(ImsiResource::getSupplierId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (supplierIds.isEmpty()) {
            return new HashMap<>();
        }
        
        return supplierService.listByIds(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getSupplierId, supplier -> supplier));
    }
    
    /**
     * Convert entity to DTO with cached data
     */
    private ImsiResourceDTO convertToDTOWithCache(ImsiResource imsiResource, 
                                                  Map<Long, ImsiGroup> groupMap, 
                                                  Map<Long, Supplier> supplierMap) {
        if (imsiResource == null) {
            return null;
        }
        
        ImsiResourceDTO dto = new ImsiResourceDTO();
        BeanUtils.copyProperties(imsiResource, dto);
        
        // Set IMSI type description
        ImsiTypeEnum imsiTypeEnum = ImsiTypeEnum.getByCode(imsiResource.getImsiType());
        dto.setImsiTypeDesc(imsiTypeEnum != null ? imsiTypeEnum.getDescription() : "Unknown");
        
        // Set status description
        ImsiStatusEnum imsiStatusEnum = ImsiStatusEnum.getByCode(imsiResource.getStatus());
        dto.setStatusDesc(imsiStatusEnum != null ? imsiStatusEnum.getDescription() : "Unknown");
        
        // Get group name from cache
        if (imsiResource.getGroupId() != null) {
            ImsiGroup group = groupMap.get(imsiResource.getGroupId());
            if (group != null) {
                dto.setGroupName(group.getGroupName());
            }
        }
        
        // Get supplier name from cache
        if (imsiResource.getSupplierId() != null) {
            Supplier supplier = supplierMap.get(imsiResource.getSupplierId());
            if (supplier != null) {
                dto.setSupplierName(supplier.getSupplierName());
            }
        }
        
        return dto;
    }
    
    /**
     * Convert entity to DTO for single query (with individual database queries)
     */
    private ImsiResourceDTO convertToDTOSingle(ImsiResource imsiResource) {
        if (imsiResource == null) {
            return null;
        }
        
        ImsiResourceDTO dto = new ImsiResourceDTO();
        BeanUtils.copyProperties(imsiResource, dto);
        
        // Set IMSI type description
        ImsiTypeEnum imsiTypeEnum = ImsiTypeEnum.getByCode(imsiResource.getImsiType());
        dto.setImsiTypeDesc(imsiTypeEnum != null ? imsiTypeEnum.getDescription() : "Unknown");
        
        // Set status description
        ImsiStatusEnum imsiStatusEnum = ImsiStatusEnum.getByCode(imsiResource.getStatus());
        dto.setStatusDesc(imsiStatusEnum != null ? imsiStatusEnum.getDescription() : "Unknown");
        
        // Get group name
        if (imsiResource.getGroupId() != null) {
            ImsiGroup group = imsiGroupService.getById(imsiResource.getGroupId());
            if (group != null) {
                dto.setGroupName(group.getGroupName());
            }
        }
        
        // Get supplier name
        if (imsiResource.getSupplierId() != null) {
            Supplier supplier = supplierService.getById(imsiResource.getSupplierId());
            if (supplier != null) {
                dto.setSupplierName(supplier.getSupplierName());
            }
        }
        
        return dto;
    }
    
    /**
     * Convert entity to DTO (deprecated, use convertToDTOWithCache for batch operations)
     * @deprecated Use convertToDTOWithCache for batch operations to avoid N+1 query problem
     */
    @Deprecated
    private ImsiResourceDTO convertToDTO(ImsiResource imsiResource) {
        return convertToDTOSingle(imsiResource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchImportImsiResource(List<ImsiResource> dataList) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                return false;
            }

            // 数据校验和预处理
            List<ImsiResource> validDataList = new ArrayList<>(); 
            Map<Long, Integer> groupCountMap = new HashMap<>(); // 统计每个组导入的数量
            Date now = new Date();
            
            for (ImsiResource imsiResource : dataList) {
                // 校验必填字段
                if (StringUtils.isBlank(imsiResource.getImsi())) {
                    log.warn("IMSI number cannot be empty, skip this record");
                    continue;
                }
                
                // 检查IMSI是否已存在
                if (isImsiExists(imsiResource.getImsi())) {
                    log.warn("IMSI number already exists: {}", imsiResource.getImsi());
                    continue;
                }
                
                // 验证group ID
                if (imsiResource.getGroupId() != null) {
                    ImsiGroup imsiGroup = imsiGroupService.getById(imsiResource.getGroupId());
                    if (imsiGroup == null) {
                        log.warn("IMSI group not found for groupId: {}, skip this record", imsiResource.getGroupId());
                        continue;
                    }
                }
                
                // 验证supplier ID
                if (imsiResource.getSupplierId() != null) {
                    Supplier supplier = supplierService.getById(imsiResource.getSupplierId());
                    if (supplier == null) {
                        log.warn("Supplier not found for supplierId: {}, skip this record", imsiResource.getSupplierId());
                        continue;
                    }
                }
                
                // 设置默认值
                if (imsiResource.getImsiId() == null) {
                    imsiResource.setImsiId(sequenceService.getNextSequenceValue("imsi_resource_id_seq"));
                }
                if (imsiResource.getStatus() == null) {
                    imsiResource.setStatus(ImsiStatusEnum.IDLE.getCode());
                }
                if (imsiResource.getCreateTime() == null) {
                    imsiResource.setCreateTime(now);
                }
                if (imsiResource.getUpdateTime() == null) {
                    imsiResource.setUpdateTime(now);
                }
                
                // 统计每个组的导入数量
                if (imsiResource.getGroupId() != null) {
                    groupCountMap.put(imsiResource.getGroupId(), 
                        groupCountMap.getOrDefault(imsiResource.getGroupId(), 0) + 1);
                }
                
                validDataList.add(imsiResource);
            }
            
            if (validDataList.isEmpty()) {
                log.warn("No valid data to import");
                return false;
            }
            
            // 批量插入
            boolean result = this.saveBatch(validDataList);
            
            // 更新IMSI组使用计数
            if (result && !groupCountMap.isEmpty()) {
                for (Map.Entry<Long, Integer> entry : groupCountMap.entrySet()) {
                    imsiGroupService.updateUsedCount(entry.getKey(), entry.getValue());
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Batch import IMSI resources failed", e);
            throw new BusinessException("Batch import IMSI resources failed: " + e.getMessage());
        }
    }

    @Override
    public List<ImsiResource> queryImsiResourceForExport(ImsiResourceQuery queryParams) {
        try {
            LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
            
            // 构建查询条件
            if (queryParams != null) {
                if (StringUtils.isNotBlank(queryParams.getImsi())) {
                    queryWrapper.like(ImsiResource::getImsi, queryParams.getImsi());
                }
                if (queryParams.getImsiType() != null) {
                    queryWrapper.eq(ImsiResource::getImsiType, queryParams.getImsiType());
                }
                if (queryParams.getGroupId() != null) {
                    queryWrapper.eq(ImsiResource::getGroupId, queryParams.getGroupId());
                }
                if (queryParams.getSupplierId() != null) {
                    queryWrapper.eq(ImsiResource::getSupplierId, queryParams.getSupplierId());
                }
                if (queryParams.getStatus() != null) {
                    queryWrapper.eq(ImsiResource::getStatus, queryParams.getStatus());
                }
                // 时间查询条件暂时注释，ImsiResourceQuery中没有时间字段
                // if (queryParams.getCreateTimeStart() != null) {
                //     queryWrapper.ge(ImsiResource::getCreateTime, queryParams.getCreateTimeStart());
                // }
                // if (queryParams.getCreateTimeEnd() != null) {
                //     queryWrapper.le(ImsiResource::getCreateTime, queryParams.getCreateTimeEnd());
                // }
            }
            
            // 按创建时间倒序排列
            queryWrapper.orderByDesc(ImsiResource::getCreateTime);
            
            return this.list(queryWrapper);
            
        } catch (Exception e) {
            log.error("Query export data failed", e);
            throw new BusinessException("Query export data failed: " + e.getMessage());
        }
    }
    
}