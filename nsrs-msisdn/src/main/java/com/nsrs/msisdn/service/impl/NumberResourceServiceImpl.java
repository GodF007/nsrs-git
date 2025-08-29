package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.msisdn.dto.NumberResourceDTO;
import com.nsrs.msisdn.entity.HlrSwitch;
import com.nsrs.msisdn.entity.NumberLevel;
import com.nsrs.msisdn.entity.NumberPattern;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.entity.NumberSegment;
import com.nsrs.msisdn.mapper.HlrSwitchMapper;
import com.nsrs.msisdn.mapper.NumberLevelMapper;
import com.nsrs.msisdn.mapper.NumberPatternMapper;
import com.nsrs.msisdn.mapper.NumberResourceMapper;
import com.nsrs.msisdn.mapper.NumberSegmentMapper;
import com.nsrs.msisdn.service.NumberOperationLogService;
import com.nsrs.msisdn.service.NumberResourceService;
import com.nsrs.msisdn.service.NumberSegmentService;

import com.nsrs.msisdn.vo.NumberResourceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nsrs.common.enums.NumberStatusEnum;
import com.nsrs.common.enums.OperationTypeEnum;
import com.nsrs.common.enums.ResultStatusEnum;
import com.nsrs.common.enums.BatchOperationTypeEnum;
import com.nsrs.common.utils.SequenceService;

/**
 * 号码资源服务实现类
 */
@Slf4j
@Service
public class NumberResourceServiceImpl extends ServiceImpl<NumberResourceMapper, NumberResource> implements NumberResourceService {


    
    @Autowired
    private SequenceService sequenceService;
    
    @Autowired
    private NumberSegmentMapper segmentMapper;
    
    @Autowired
    private NumberLevelMapper levelMapper;
    
    @Autowired
    private NumberPatternMapper patternMapper;
    
    @Autowired
    private HlrSwitchMapper hlrMapper;
    
    @Autowired
    private NumberOperationLogService operationLogService;
    
    @Autowired
    private NumberSegmentService segmentService;

    @Override
    public IPage<NumberResourceVO> pageQuery(IPage<NumberResource> page, NumberResourceDTO dto) {
        // 构建查询条件，ShardingSphere会自动处理分表路由
        LambdaQueryWrapper<NumberResource> queryWrapper = buildQueryWrapper(dto);
        
        // 执行分页查询
        IPage<NumberResource> resultPage = this.page(page, queryWrapper);
        
        // 转换为VO
        return convertToVOPage(resultPage);
    }

    @Override
    public NumberResourceVO getByNumber(String number) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 构建查询条件
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getNumber, number);
        
        // 查询号码
        NumberResource resource = this.getOne(queryWrapper);
        if (resource == null) {
            return null;
        }
        
        // 转换为VO
        return convertToVO(resource);
    }
    
    /**
     * 判断号码是否存在
     * @param number 号码
     * @return 是否存在
     */
    private boolean isNumberExists(String number) {
        if (StringUtils.isBlank(number)) {
            return false;
        }
        
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getNumber, number);
        queryWrapper.select(NumberResource::getNumberId); // 只查询ID字段，提高性能
        
        return this.getOne(queryWrapper) != null;
    }

    /**
     * 获取基本号码资源信息（不包含关联信息，性能优化）
     * @param number 号码
     * @return 号码资源基本信息
     */
    private NumberResource getBasicNumberResource(String number) {
        if (StringUtils.isBlank(number)) {
            return null;
        }
        
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getNumber, number);
        
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(NumberResourceDTO dto) {
        if (dto == null || StringUtils.isBlank(dto.getNumber())) {
            throw new BusinessException("400", "Number cannot be empty");
        }

        // 检查号码是否已存在
        if (isNumberExists(dto.getNumber())) {
            throw new BusinessException("400", "Number already exists");
        }
        
        // 转换DTO为实体
        NumberResource resource = new NumberResource();
        BeanUtils.copyProperties(dto, resource);
        // 设置全局序列ID
        resource.setNumberId(sequenceService.getNextNumberResourceId());
        resource.setCreateTime(new Date());
        resource.setUpdateTime(new Date());
        
        // 设置默认状态为空闲
        if (resource.getStatus() == null) {
            resource.setStatus(NumberStatusEnum.IDLE.getCode());
        }
        
        boolean result = this.save(resource);
        
        if (result) {
            // 记录操作日志
            operationLogService.recordLog(
                resource.getNumberId(),
                resource.getNumber(),
                resource.getNumberType(),
                1, // 操作类型：1-创建
                null, // 原状态为空
                resource.getStatus(),
                null, // 操作用户ID，可从上下文获取
                null, // 费用
                null, // 组织名称
                1, // 成功
                "Create number resource"
            );
            
            // 更新号段库存统计
            if (resource.getSegmentId() != null) {
                segmentService.incrementalUpdateStatistics(resource.getSegmentId(), null, resource.getStatus());
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(String number, NumberResourceDTO dto) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询现有资源
        NumberResourceVO existingVO = getByNumber(number);
        if (existingVO == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 构建查询条件
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getNumber, number);
        
        // 创建更新实体
        NumberResource updateResource = new NumberResource();
        BeanUtils.copyProperties(dto, updateResource, "numberId", "number", "createTime");
        updateResource.setUpdateTime(new Date());
        
        boolean result = this.update(updateResource, queryWrapper);
        
        if (result) {
            // 记录操作日志
            operationLogService.recordLog(
                existingVO.getNumberId(),
                existingVO.getNumber(),
                existingVO.getNumberType(),
                OperationTypeEnum.MODIFY.getCode(), // 操作类型：2-修改
                existingVO.getStatus(),
                dto.getStatus() != null ? dto.getStatus() : existingVO.getStatus(),
                null, // 操作用户ID，可从上下文获取
                null, // 费用
                null, // 组织名称
                ResultStatusEnum.SUCCESS.getCode(), // 成功
                "Update number resource"
            );
            
            // 如果状态发生变化，更新号段库存统计
            if (dto.getStatus() != null && !dto.getStatus().equals(existingVO.getStatus()) && existingVO.getSegmentId() != null) {
                segmentService.incrementalUpdateStatistics(existingVO.getSegmentId(), existingVO.getStatus(), dto.getStatus());
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String number) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态，只有空闲状态的号码才能删除
        if (!NumberStatusEnum.IDLE.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only idle numbers can be deleted");
        }
        
        // 构建删除条件
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getNumber, number);
        
        boolean result = this.remove(queryWrapper);
        
        if (result) {
            // 记录操作日志
            operationLogService.recordLog(
                resource.getNumberId(),
                resource.getNumber(),
                resource.getNumberType(),
                OperationTypeEnum.DELETE.getCode(),
                resource.getStatus(),
                null, // 删除后无状态
                null, // 操作用户ID，可从上下文获取
                null, // 费用
                null, // 组织名称
                ResultStatusEnum.SUCCESS.getCode(), // 成功
                "Delete number resource"
            );
            
            // 更新号段库存统计
            if (resource.getSegmentId() != null) {
                segmentService.incrementalUpdateStatistics(resource.getSegmentId(), resource.getStatus(), null);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String number, Integer status) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        if (status == null) {
            throw new BusinessException("400", "Status cannot be empty");
        }

        // 先获取原有状态
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getNumber, number);
        NumberResource existingResource = this.getOne(queryWrapper);
        
        if (existingResource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        Integer oldStatus = existingResource.getStatus();
        
        // 更新状态
        NumberResource resource = new NumberResource();
        resource.setStatus(status);
        resource.setUpdateTime(new Date());
        
        boolean result = this.update(resource, queryWrapper);
        
        if (result) {
            // Record operation log
            operationLogService.recordLog(existingResource.getNumberId(), existingResource.getNumber(), 
                existingResource.getNumberType(), BatchOperationTypeEnum.MODIFY.getCode(), oldStatus, status, null, null, existingResource.getAttributiveOrg(), ResultStatusEnum.SUCCESS.getCode(), 
                "Status updated from " + oldStatus + " to " + status);
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(existingResource.getSegmentId(), oldStatus, status);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateNumberStatusByNumber(List<String> numbers, Integer status) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("400", "Number list cannot be empty");
        }
        
        if (status == null) {
            throw new BusinessException("400", "Status cannot be empty");
        }
        
        // 批量查询号码资源
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(NumberResource::getNumber, numbers);
        List<NumberResource> existingResources = this.list(queryWrapper);
        
        if (existingResources.isEmpty()) {
            log.warn("No number resources found for batch update");
            return false;
        }
        
        // 收集号段统计变更信息
        Map<Long, Map<Integer, Integer>> segmentStatusChanges = new HashMap<>();
        
        // 批量更新状态
        List<NumberResource> updateList = new ArrayList<>();
        for (NumberResource existingResource : existingResources) {
            Integer oldStatus = existingResource.getStatus();
            
            // 创建更新对象
            NumberResource updateResource = new NumberResource();
            updateResource.setNumberId(existingResource.getNumberId());
            updateResource.setStatus(status);
            updateResource.setUpdateTime(new Date());
            updateList.add(updateResource);
            
            // 记录操作日志
            operationLogService.recordLog(existingResource.getNumberId(), existingResource.getNumber(), 
                existingResource.getNumberType(), BatchOperationTypeEnum.MODIFY.getCode(), oldStatus, status, null, null, existingResource.getAttributiveOrg(), ResultStatusEnum.SUCCESS.getCode(), 
                "Batch status updated from " + oldStatus + " to " + status);
            
            // 收集号段统计变更
            if (existingResource.getSegmentId() != null && !oldStatus.equals(status)) {
                segmentStatusChanges.computeIfAbsent(existingResource.getSegmentId(), k -> new HashMap<>());
                Map<Integer, Integer> statusChanges = segmentStatusChanges.get(existingResource.getSegmentId());
                
                // 减少旧状态计数
                statusChanges.put(oldStatus, statusChanges.getOrDefault(oldStatus, 0) - 1);
                // 增加新状态计数
                statusChanges.put(status, statusChanges.getOrDefault(status, 0) + 1);
            }
        }
        
        // 执行批量更新
        boolean result = this.updateBatchById(updateList);
        
        if (result) {
            // 批量更新号段统计
            for (Map.Entry<Long, Map<Integer, Integer>> entry : segmentStatusChanges.entrySet()) {
                segmentService.batchIncrementalUpdateStatistics(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }

    @Override
    public IPage<NumberResourceVO> crossTableQuery(IPage<NumberResource> page, NumberResourceDTO dto) {
        // 使用ShardingSphere进行分表查询，构建查询条件
        LambdaQueryWrapper<NumberResource> queryWrapper = buildQueryWrapper(dto);
        
        // 执行分页查询，ShardingSphere会自动处理分表路由
        IPage<NumberResource> result = this.page(page, queryWrapper);
        
        // 转换为VO
        return convertToVOPage(result);
    }
    
    @Override
    public IPage<NumberResourceVO> prefixQuery(IPage<NumberResource> page, NumberResourceDTO dto) {
        // 构建优化的前缀查询条件
        LambdaQueryWrapper<NumberResource> queryWrapper = buildPrefixQueryWrapper(dto);
        
        // 执行分页查询，利用分表算法进行路由优化
        IPage<NumberResource> result = this.page(page, queryWrapper);
        
        // 转换为VO
        return convertToVOPage(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTable(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            throw new BusinessException("400", "Number prefix cannot be empty");
        }
        
        // ShardingSphere会自动管理分表，无需手动创建
        log.info("Table creation is managed by ShardingSphere for prefix: {}", prefix);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoClassify(String number) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码段是否存在
        if (resource.getSegmentId() == null) {
            throw new BusinessException("400", "Number is not associated with a segment");
        }
        
        // 获取号码段信息
        NumberSegment segment = segmentMapper.selectById(resource.getSegmentId());
        if (segment == null) {
            throw new BusinessException("404", "Number segment does not exist");
        }
        
        // 检查号码是否符合号码段的起始和结束范围
        if (StringUtils.isNotBlank(segment.getStartNumber()) && 
            number.compareTo(segment.getStartNumber()) < 0) {
            throw new BusinessException("400", "Number is less than segment start range");
        }
        
        if (StringUtils.isNotBlank(segment.getEndNumber()) && 
            number.compareTo(segment.getEndNumber()) > 0) {
            throw new BusinessException("400", "Number is greater than segment end range");
        }
        
        // 自动关联号码级别和模式
        boolean updated = false;
        
        // 如果号码模式为空，根据号码特征查找匹配的模式
        if (resource.getPatternId() == null) {
            // 查询所有号码模式
            List<NumberPattern> patterns = patternMapper.selectList(null);
            
            for (NumberPattern pattern : patterns) {
                if (StringUtils.isNotBlank(pattern.getExpression())) {
                    // 尝试用正则表达式匹配号码
                    if (number.matches(pattern.getExpression())) {
                        resource.setPatternId(pattern.getPatternId());
                        updated = true;
                        break;
                    }
                }
            }
        }
        
        // 如果HLR/交换机为空，设置为号码段默认HLR
        if (resource.getHlrId() == null && segment.getHlrSwitchId() != null) {
            resource.setHlrId(segment.getHlrSwitchId());
            updated = true;
        }
        
        // 更新号码资源
        if (updated) {
            resource.setUpdateTime(new Date());
            return this.updateById(resource);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAutoClassify(Long segmentId) {
        if (segmentId == null) {
            throw new BusinessException("400", "Segment ID cannot be empty");
        }
        
        // 获取指定号码段的所有号码
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        
        List<NumberResource> resources = this.list(queryWrapper);
        if (resources.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        
        // 逐个自动分类
        for (NumberResource resource : resources) {
            try {
                boolean success = autoClassify(resource.getNumber());
                if (success) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Auto classification failed for number: {}, reason={}", resource.getNumber(), e.getMessage());
            }
        }
        
        return successCount;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchOperation(List<String> numbers, Integer operationType, String remark) {
        if (numbers == null || numbers.isEmpty()) {
            throw new BusinessException("400", "Number list cannot be empty");
        }
        
        if (operationType == null) {
            throw new BusinessException("400", "Operation type cannot be empty");
        }
        
        int successCount = 0;
        Set<Long> affectedSegmentIds = new HashSet<>();
        
        // 直接使用号码进行操作，避免分表查询问题
        for (String number : numbers) {
            try {
                boolean success = false;
                
                // 先获取号码资源信息用于记录日志和更新统计
                NumberResourceVO resourceVO = getByNumber(number);
                if (resourceVO == null) {
                    log.warn("Number resource not found for number: {}", number);
                    continue;
                }
                
                // 执行对应的操作
                if (OperationTypeEnum.RESERVE.getCode().equals(operationType)) {
                    success = reserve(number, remark);
                } else if (OperationTypeEnum.ASSIGN.getCode().equals(operationType)) {
                    success = assign(number, "Batch Assignment", remark);
                } else if (OperationTypeEnum.ACTIVATE.getCode().equals(operationType)) {
                    success = activate(number, null, remark);
                } else if (OperationTypeEnum.FREEZE.getCode().equals(operationType)) {
                    success = freeze(number, remark);
                } else if (OperationTypeEnum.UNFREEZE.getCode().equals(operationType)) {
                    success = unfreeze(number, remark);
                } else if (OperationTypeEnum.RELEASE.getCode().equals(operationType)) {
                    success = release(number, remark);
                } else if (OperationTypeEnum.RECYCLE.getCode().equals(operationType)) {
                    success = recycle(number, remark);
                } else if (OperationTypeEnum.DELETE.getCode().equals(operationType)) {
                    success = delete(number);
                } else {
                    throw new BusinessException("400", "Unsupported operation type: " + operationType);
                }
                
                if (success) {
                    successCount++;
                    affectedSegmentIds.add(resourceVO.getSegmentId());
                }
            } catch (Exception e) {
                log.error("Batch operation failed for number: {}, operation type={}, reason={}", 
                         number, operationType, e.getMessage());
            }
        }
        
        // 记录批量操作日志

        /*if (successCount > 0) {
            String operationName = getOperationName(operationType);
            operationLogService.recordLog(null, null, null, operationType, null, null,
                null, null, null, ResultStatusEnum.SUCCESS.getCode(), 
                String.format("Batch %s operation completed, successfully processed %d numbers, remark: %s", 
                    operationName, successCount, remark != null ? remark : "N/A"), null);
        }*/
        
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reserve(String number, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为空闲
        if (!NumberStatusEnum.IDLE.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only idle numbers can be reserved");
        }
        
        // 更新号码状态
        resource.setStatus(NumberStatusEnum.RESERVED.getCode()); // 预留
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.RESERVE.getCode(), NumberStatusEnum.IDLE.getCode(), NumberStatusEnum.RESERVED.getCode(), null, null, null, 1, 
                "Reserve number, reason: " + (remark != null ? remark : "N/A"));
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), NumberStatusEnum.IDLE.getCode(), NumberStatusEnum.RESERVED.getCode());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assign(String number, String attributiveOrg, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为空闲或预留
        if (!NumberStatusEnum.IDLE.getCode().equals(resource.getStatus()) && !NumberStatusEnum.RESERVED.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only idle or reserved numbers can be assigned");
        }
        
        Integer oldStatus = resource.getStatus();
        
        // 更新号码状态
        resource.setStatus(NumberStatusEnum.ASSIGNED.getCode()); // 已分配
        resource.setAttributiveOrg(attributiveOrg);
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getAttributiveOrg, resource.getAttributiveOrg())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.ASSIGN.getCode(), oldStatus, NumberStatusEnum.ASSIGNED.getCode(), null, null, attributiveOrg, 1, 
                "Assign number to organization: " + attributiveOrg);
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), oldStatus, NumberStatusEnum.ASSIGNED.getCode());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activate(String number, String iccid, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为已分配
        if (!NumberStatusEnum.ASSIGNED.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only assigned numbers can be activated");
        }
        
        // 更新号码状态
        resource.setStatus(NumberStatusEnum.ACTIVATED.getCode()); // 已激活
        resource.setIccid(iccid);
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getIccid, resource.getIccid())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.ACTIVATE.getCode(), NumberStatusEnum.ASSIGNED.getCode(), NumberStatusEnum.ACTIVATED.getCode(), null, null, resource.getAttributiveOrg(), 1, 
                "Activate number, ICCID: " + (iccid != null ? iccid : "N/A"));
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), NumberStatusEnum.ASSIGNED.getCode(), NumberStatusEnum.ACTIVATED.getCode());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean freeze(String number, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为已激活或已使用
        if (!NumberStatusEnum.ACTIVATED.getCode().equals(resource.getStatus()) && !NumberStatusEnum.IN_USE.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only activated or in-use numbers can be frozen");
        }
        
        Integer oldStatus = resource.getStatus();
        
        // 更新号码状态
        resource.setStatus(NumberStatusEnum.FROZEN.getCode()); // 已冻结
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);

        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.FREEZE.getCode(), oldStatus, NumberStatusEnum.FROZEN.getCode(), null, null, resource.getAttributiveOrg(), 1, 
                "Freeze number, reason: " + (remark != null ? remark : "N/A"));
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), oldStatus, NumberStatusEnum.FROZEN.getCode());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreeze(String number, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为已冻结
        if (!NumberStatusEnum.FROZEN.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only frozen numbers can be unfrozen");
        }
        
        // 更新号码状态，恢复为原来的状态（默认为已激活）
        resource.setStatus(NumberStatusEnum.ACTIVATED.getCode()); // 已激活
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);

        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.UNFREEZE.getCode(), NumberStatusEnum.FROZEN.getCode(), NumberStatusEnum.ACTIVATED.getCode(), null, null, resource.getAttributiveOrg(), 1, 
                "Unfreeze number, reason: " + (remark != null ? remark : "N/A"));
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), NumberStatusEnum.FROZEN.getCode(), NumberStatusEnum.ACTIVATED.getCode());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean release(String number, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为已分配、已激活、已使用或已冻结
        if (!NumberStatusEnum.ASSIGNED.getCode().equals(resource.getStatus()) && 
            !NumberStatusEnum.ACTIVATED.getCode().equals(resource.getStatus()) &&
            !NumberStatusEnum.IN_USE.getCode().equals(resource.getStatus()) &&
            !NumberStatusEnum.FROZEN.getCode().equals(resource.getStatus())) {
            throw new BusinessException("400", "Only assigned, activated, in-use or frozen numbers can be released");
        }
        
        Integer oldStatus = resource.getStatus();
        String oldAttributiveOrg = resource.getAttributiveOrg();
        
        // 更新号码状态
        resource.setStatus(NumberStatusEnum.IDLE.getCode()); // 空闲
        resource.setAttributiveOrg(null);
        resource.setIccid(null);
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getAttributiveOrg, resource.getAttributiveOrg())
                    .set(NumberResource::getIccid, resource.getIccid())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);

        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.RELEASE.getCode(), oldStatus, NumberStatusEnum.IDLE.getCode(), null, null, oldAttributiveOrg, 1, 
                "Release number, reason: " + (remark != null ? remark : "N/A"));
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), oldStatus, NumberStatusEnum.IDLE.getCode());
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recycle(String number, String remark) {
        if (StringUtils.isBlank(number)) {
            throw new BusinessException("400", "Number cannot be empty");
        }
        
        // 根据号码查询资源（使用基本查询，避免不必要的关联查询）
        NumberResource resource = getBasicNumberResource(number);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        Integer oldStatus = resource.getStatus();
        String oldAttributiveOrg = resource.getAttributiveOrg();
        
        // 更新号码状态
        resource.setStatus(NumberStatusEnum.IDLE.getCode()); // 空闲
        resource.setAttributiveOrg(null);
        resource.setIccid(null);
        resource.setRemark(remark);
        resource.setUpdateTime(new Date());
        
        // 使用号码作为分表键进行更新，避免分表键更新错误
        LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NumberResource::getNumber, resource.getNumber())
                    .set(NumberResource::getStatus, resource.getStatus())
                    .set(NumberResource::getAttributiveOrg, resource.getAttributiveOrg())
                    .set(NumberResource::getIccid, resource.getIccid())
                    .set(NumberResource::getRemark, resource.getRemark())
                    .set(NumberResource::getUpdateTime, resource.getUpdateTime());
        boolean result = this.update(updateWrapper);

        if (result) {
            // Record operation log
            operationLogService.recordLog(resource.getNumberId(), resource.getNumber(), 
                resource.getNumberType(), OperationTypeEnum.RECYCLE.getCode(), oldStatus, NumberStatusEnum.IDLE.getCode(), null, null, oldAttributiveOrg, 1, 
                "Recycle number, reason: " + (remark != null ? remark : "N/A"));
            
            // Update segment statistics incrementally
            segmentService.incrementalUpdateStatistics(resource.getSegmentId(), oldStatus, NumberStatusEnum.IDLE.getCode());
        }
        
        return result;
    }
    
    /**
     * 获取操作名称
     */
    private String getOperationName(Integer operationType) {
        if (OperationTypeEnum.CREATE.getCode().equals(operationType)) {
            return "create";
        } else if (OperationTypeEnum.RESERVE.getCode().equals(operationType)) {
            return "reserve";
        } else if (OperationTypeEnum.ASSIGN.getCode().equals(operationType)) {
            return "assign";
        } else if (OperationTypeEnum.ACTIVATE.getCode().equals(operationType)) {
            return "activate";
        } else if (OperationTypeEnum.FREEZE.getCode().equals(operationType)) {
            return "freeze";
        } else if (OperationTypeEnum.UNFREEZE.getCode().equals(operationType)) {
            return "unfreeze";
        } else if (OperationTypeEnum.RELEASE.getCode().equals(operationType)) {
            return "release";
        } else if (OperationTypeEnum.RECYCLE.getCode().equals(operationType)) {
            return "recycle";
        } else if (OperationTypeEnum.MODIFY.getCode().equals(operationType)) {
            return "modify";
        } else if (OperationTypeEnum.DELETE.getCode().equals(operationType)) {
            return "delete";
        } else {
            return "unknown operation";
        }
    }

    @Override
    public NumberResource getBasicInfo(String number) {
        if (StringUtils.isBlank(number)) {
            return null;
        }
        
        return getBasicNumberResource(number);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 基于NumberSegment表统计，避免跨分片查询
        NumberSegmentMapper segmentMapper = this.segmentMapper;
        
        // 查询总号码数
        Integer totalCount = segmentMapper.sumTotalQty();
        result.put("totalCount", totalCount != null ? totalCount : 0);
        
        // 查询各状态号码数量（基于NumberSegment表的统计字段）
        Integer idleCount = segmentMapper.sumIdleQty();
        result.put("statusCount" + NumberStatusEnum.IDLE.getCode(), idleCount != null ? idleCount : 0);
        
        Integer reservedCount = segmentMapper.sumReservedQty();
        result.put("statusCount" + NumberStatusEnum.RESERVED.getCode(), reservedCount != null ? reservedCount : 0);
        
        Integer activatedCount = segmentMapper.sumActivatedQty();
        result.put("statusCount" + NumberStatusEnum.ACTIVATED.getCode(), activatedCount != null ? activatedCount : 0);
        
        Integer frozenCount = segmentMapper.sumFrozenQty();
        result.put("statusCount" + NumberStatusEnum.FROZEN.getCode(), frozenCount != null ? frozenCount : 0);
        
        Integer blockedCount = segmentMapper.sumBlockedQty();
        result.put("statusCount" + NumberStatusEnum.LOCKED.getCode(), blockedCount != null ? blockedCount : 0);
        
        // 释放后的号码状态为空闲，已包含在idleCount中，无需单独统计
        
//        // 对于ASSIGNED和IN_USE状态，由于NumberSegment表中没有对应字段，暂时返回0
//        result.put("statusCount" + NumberStatusEnum.ASSIGNED.getCode(), 0);
//        result.put("statusCount" + NumberStatusEnum.IN_USE.getCode(), 0);

        
        // 查询各号码类型数量（这个需要查询NumberResource表，但可以通过分段查询优化）
        /*for (int i = 1; i <= 5; i++) {
            // 暂时保持原有逻辑，后续可以考虑在NumberSegment表中增加类型统计字段
            LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NumberResource::getNumberType, i);
            long typeCount = this.count(queryWrapper);
            result.put("typeCount" + i, typeCount);
        }*/
        
        return result;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<NumberResource> buildQueryWrapper(NumberResourceDTO dto) {
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        
        if (dto != null) {
            // 设置查询条件
            if (StringUtils.isNotBlank(dto.getNumber())) {
                // 如果号码长度小于11位，使用前缀模糊查询
                if (dto.getNumber().length() < 11) {
                    queryWrapper.likeRight(NumberResource::getNumber, dto.getNumber());
                } else {
                    // 完整号码使用精确查询
                    queryWrapper.eq(NumberResource::getNumber, dto.getNumber());
                }
            }
            
            if (dto.getNumberType() != null) {
                queryWrapper.eq(NumberResource::getNumberType, dto.getNumberType());
            }
            
            if (dto.getSegmentId() != null) {
                queryWrapper.eq(NumberResource::getSegmentId, dto.getSegmentId());
            }
            
            if (dto.getLevelId() != null) {
                queryWrapper.eq(NumberResource::getLevelId, dto.getLevelId());
            }
            
            if (dto.getPatternId() != null) {
                queryWrapper.eq(NumberResource::getPatternId, dto.getPatternId());
            }
            
            if (dto.getHlrId() != null) {
                queryWrapper.eq(NumberResource::getHlrId, dto.getHlrId());
            }
            
            if (StringUtils.isNotBlank(dto.getAttributiveOrg())) {
                queryWrapper.like(NumberResource::getAttributiveOrg, dto.getAttributiveOrg());
            }
            
            if (dto.getStatus() != null) {
                queryWrapper.eq(NumberResource::getStatus, dto.getStatus());
            }
        }
        
        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(NumberResource::getCreateTime);
        
        return queryWrapper;
    }
    
    /**
     * 构建前缀查询条件（优化版本，利用分表算法）
     */
    private LambdaQueryWrapper<NumberResource> buildPrefixQueryWrapper(NumberResourceDTO dto) {
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        
        if (dto != null) {
            // 设置查询条件
            if (StringUtils.isNotBlank(dto.getNumber())) {
                // 对于前缀查询，使用范围查询来触发分表算法的优化逻辑
                String prefix = dto.getNumber();
                if (prefix.length() < 11) {
                    // 构造范围查询，让分表算法能够识别前缀并路由到指定分表
                    // rangeStart: 前缀 + 补0到11位
                    String rangeStart = prefix + "00000000000".substring(0, 11 - prefix.length());
                    // rangeEnd: 前缀 + 补9到11位
                    String rangeEnd = prefix + "99999999999".substring(0, 11 - prefix.length());
                    queryWrapper.between(NumberResource::getNumber, rangeStart, rangeEnd);
                } else {
                    // 完整号码使用精确查询
                    queryWrapper.eq(NumberResource::getNumber, dto.getNumber());
                }
            }
            
            if (dto.getNumberType() != null) {
                queryWrapper.eq(NumberResource::getNumberType, dto.getNumberType());
            }
            
            if (dto.getSegmentId() != null) {
                queryWrapper.eq(NumberResource::getSegmentId, dto.getSegmentId());
            }
            
            if (dto.getLevelId() != null) {
                queryWrapper.eq(NumberResource::getLevelId, dto.getLevelId());
            }
            
            if (dto.getPatternId() != null) {
                queryWrapper.eq(NumberResource::getPatternId, dto.getPatternId());
            }
            
            if (dto.getHlrId() != null) {
                queryWrapper.eq(NumberResource::getHlrId, dto.getHlrId());
            }
            
            if (StringUtils.isNotBlank(dto.getAttributiveOrg())) {
                queryWrapper.like(NumberResource::getAttributiveOrg, dto.getAttributiveOrg());
            }
            
            if (dto.getStatus() != null) {
                queryWrapper.eq(NumberResource::getStatus, dto.getStatus());
            }
        }
        
        // 默认按创建时间降序排序
        queryWrapper.orderByDesc(NumberResource::getCreateTime);
        
        return queryWrapper;
    }

    /**
     * 转换为VO
     */
    private NumberResourceVO convertToVO(NumberResource resource) {
        if (resource == null) {
            return null;
        }
        
        NumberResourceVO vo = new NumberResourceVO();
        BeanUtils.copyProperties(resource, vo);
        
        // 获取关联信息
        setRelatedInfo(vo);
        
        return vo;
    }

    /**
     * 转换分页结果为VO分页（优化版本，避免N+1查询）
     */
    private IPage<NumberResourceVO> convertToVOPage(IPage<NumberResource> page) {
        List<NumberResourceVO> voList = new ArrayList<>();
        List<NumberResource> records = page.getRecords();
        
        if (records.isEmpty()) {
            IPage<NumberResourceVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
            voPage.setRecords(voList);
            return voPage;
        }
        
        // 批量获取关联信息，避免N+1查询
        Map<Long, NumberSegment> segmentMap = batchGetSegments(records);
        Map<Long, NumberLevel> levelMap = batchGetLevels(records);
        Map<Long, NumberPattern> patternMap = batchGetPatterns(records);
        Map<Long, HlrSwitch> hlrMap = batchGetHlrSwitches(records);
        
        // 转换为VO并设置关联信息
        for (NumberResource resource : records) {
            NumberResourceVO vo = new NumberResourceVO();
            BeanUtils.copyProperties(resource, vo);
            
            // 设置关联信息（使用批量查询结果）
            setRelatedInfoBatch(vo, segmentMap, levelMap, patternMap, hlrMap);
            
            voList.add(vo);
        }
        
        IPage<NumberResourceVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 批量获取号码段信息
     */
    private Map<Long, NumberSegment> batchGetSegments(List<NumberResource> records) {
        Set<Long> segmentIds = records.stream()
                .map(NumberResource::getSegmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (segmentIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<NumberSegment> segments = segmentMapper.selectBatchIds(segmentIds);
        return segments.stream().collect(Collectors.toMap(NumberSegment::getSegmentId, segment -> segment));
    }
    
    /**
     * 批量获取号码级别信息
     */
    private Map<Long, NumberLevel> batchGetLevels(List<NumberResource> records) {
        Set<Long> levelIds = records.stream()
                .map(NumberResource::getLevelId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (levelIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<NumberLevel> levels = levelMapper.selectBatchIds(levelIds);
        return levels.stream().collect(Collectors.toMap(NumberLevel::getLevelId, level -> level));
    }
    
    /**
     * 批量获取号码模式信息
     */
    private Map<Long, NumberPattern> batchGetPatterns(List<NumberResource> records) {
        Set<Long> patternIds = records.stream()
                .map(NumberResource::getPatternId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (patternIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<NumberPattern> patterns = patternMapper.selectBatchIds(patternIds);
        return patterns.stream().collect(Collectors.toMap(NumberPattern::getPatternId, pattern -> pattern));
    }
    
    /**
     * 批量获取HLR交换机信息
     */
    private Map<Long, HlrSwitch> batchGetHlrSwitches(List<NumberResource> records) {
        Set<Long> hlrIds = records.stream()
                .map(NumberResource::getHlrId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (hlrIds.isEmpty()) {
            return new HashMap<>();
        }
        
        List<HlrSwitch> hlrSwitches = hlrMapper.selectBatchIds(hlrIds);
        return hlrSwitches.stream().collect(Collectors.toMap(HlrSwitch::getHlrId, hlr -> hlr));
    }
    
    /**
     * 设置关联信息（批量优化版本）
     */
    private void setRelatedInfoBatch(NumberResourceVO vo, Map<Long, NumberSegment> segmentMap, 
                                   Map<Long, NumberLevel> levelMap, Map<Long, NumberPattern> patternMap, 
                                   Map<Long, HlrSwitch> hlrMap) {
        // 设置号码类型名称
        if (vo.getNumberType() != null) {
            switch (vo.getNumberType()) {
                case 1:
                    vo.setNumberTypeName("PSTN Number");
                    break;
                case 2:
                    vo.setNumberTypeName("Mobile Number");
                    break;
                case 3:
                    vo.setNumberTypeName("FTTH Number");
                    break;
                case 4:
                    vo.setNumberTypeName("SIP");
                    break;
                case 5:
                    vo.setNumberTypeName("VSAT");
                    break;
                default:
                    vo.setNumberTypeName("Unknown");
            }
        }
        
        // 设置状态名称
        if (vo.getStatus() != null) {
            String statusName = NumberStatusEnum.getEnglishNameByCode(vo.getStatus());
            vo.setStatusName(statusName != null ? statusName : "Unknown");
        }
        
        // 设置号码段名称（使用批量查询结果）
        if (vo.getSegmentId() != null) {
            NumberSegment segment = segmentMap.get(vo.getSegmentId());
            if (segment != null) {
                vo.setSegmentCode(segment.getSegmentCode());
            }
        }
        
        // 设置号码级别名称（使用批量查询结果）
        if (vo.getLevelId() != null) {
            NumberLevel level = levelMap.get(vo.getLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
            }
        }
        
        // 设置号码模式名称（使用批量查询结果）
        if (vo.getPatternId() != null) {
            NumberPattern pattern = patternMap.get(vo.getPatternId());
            if (pattern != null) {
                vo.setPatternName(pattern.getPatternName());
            }
        }
        
        // 设置HLR/交换机名称（使用批量查询结果）
        if (vo.getHlrId() != null) {
            HlrSwitch hlr = hlrMap.get(vo.getHlrId());
            if (hlr != null) {
                vo.setHlrName(hlr.getHlrName());
            }
        }
    }
    
    /**
     * 设置关联信息（原版本，保留用于单个对象转换）
     */
    private void setRelatedInfo(NumberResourceVO vo) {
        // 设置号码类型名称
        if (vo.getNumberType() != null) {
            switch (vo.getNumberType()) {
                case 1:
                    vo.setNumberTypeName("PSTN Number");
                    break;
                case 2:
                    vo.setNumberTypeName("Mobile Number");
                    break;
                case 3:
                    vo.setNumberTypeName("FTTH Number");
                    break;
                case 4:
                    vo.setNumberTypeName("SIP");
                    break;
                case 5:
                    vo.setNumberTypeName("VSAT");
                    break;
                default:
                    vo.setNumberTypeName("Unknown");
            }
        }
        
        // 设置状态名称
        if (vo.getStatus() != null) {
            String statusName = NumberStatusEnum.getEnglishNameByCode(vo.getStatus());
            vo.setStatusName(statusName != null ? statusName : "Unknown");
        }
        
        // 设置号码段名称
        if (vo.getSegmentId() != null) {
            NumberSegment segment = segmentMapper.selectById(vo.getSegmentId());
            if (segment != null) {
                vo.setSegmentCode(segment.getSegmentCode());
            }
        }
        
        // 设置号码级别名称
        if (vo.getLevelId() != null) {
            NumberLevel level = levelMapper.selectById(vo.getLevelId());
            if (level != null) {
                vo.setLevelName(level.getLevelName());
            }
        }
        
        // 设置号码模式名称
        if (vo.getPatternId() != null) {
            NumberPattern pattern = patternMapper.selectById(vo.getPatternId());
            if (pattern != null) {
                vo.setPatternName(pattern.getPatternName());
            }
        }
        
        // 设置HLR/交换机名称
        if (vo.getHlrId() != null) {
            HlrSwitch hlr = hlrMapper.selectById(vo.getHlrId());
            if (hlr != null) {
                vo.setHlrName(hlr.getHlrName());
            }
        }
    }
    
    /**
     * 获取操作类型代码
     *
     * @param operationType 操作类型
     * @return 操作类型代码
     */
    private Integer getOperationTypeCode(String operationType) {
        switch (operationType.toUpperCase()) {
            case "CREATE":
                return OperationTypeEnum.CREATE.getCode();
            case "RESERVE":
                return OperationTypeEnum.RESERVE.getCode();
            case "ASSIGN":
                return OperationTypeEnum.ASSIGN.getCode();
            case "ACTIVATE":
                return OperationTypeEnum.ACTIVATE.getCode();
            case "FREEZE":
                return OperationTypeEnum.FREEZE.getCode();
            case "UNFREEZE":
                return OperationTypeEnum.UNFREEZE.getCode();
            case "RELEASE":
                return OperationTypeEnum.RELEASE.getCode();
            case "RECYCLE":
                return OperationTypeEnum.RECYCLE.getCode();
            case "MODIFY":
                return OperationTypeEnum.MODIFY.getCode();
            case "DELETE":
                return OperationTypeEnum.DELETE.getCode();
            default:
                return null; // Unknown operation
        }
    }
    
    /**
     * 根据操作类型获取新状态
     *
     * @param operationType 操作类型
     * @return 新状态
     */
    private Integer getNewStatusByOperation(String operationType) {
        switch (operationType.toUpperCase()) {
            case "CREATE":
                return NumberStatusEnum.IDLE.getCode(); // Available
            case "RESERVE":
                return NumberStatusEnum.RESERVED.getCode(); // Reserved
            case "ASSIGN":
                return NumberStatusEnum.ASSIGNED.getCode(); // Assigned
            case "ACTIVATE":
                return NumberStatusEnum.ACTIVATED.getCode(); // Active
            case "FREEZE":
                return NumberStatusEnum.FROZEN.getCode(); // Frozen
            case "UNFREEZE":
                return NumberStatusEnum.ACTIVATED.getCode(); // Active
//            case "RELEASE":
//                return NumberStatusEnum.RELEASED.getCode(); // Released
            case "RECYCLE":
                return NumberStatusEnum.IDLE.getCode(); // Available
            case "MODIFY":
                return null; // Status unchanged for modify operation
            case "DELETE":
                return null; // No status for deleted records
            default:
                return null;
        }
    }
    
    @Override
    public List<NumberResource> queryForExport(NumberResourceDTO queryDTO) {
        try {
            // 构建查询条件
            NumberResourceDTO dto = queryDTO != null ? queryDTO : new NumberResourceDTO();
            
            // 查询数据
            LambdaQueryWrapper<NumberResource> queryWrapper = buildQueryWrapper(dto);
            List<NumberResource> list = this.list(queryWrapper);
            
            log.info("Query for export completed, found {} records", list.size());
            return list;
            
        } catch (Exception e) {
            log.error("Query for export failed", e);
            throw new BusinessException("Query for export failed: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchImport(List<NumberResource> dataList) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                throw new BusinessException("Import data list is empty");
            }
            
            // 验证数据并转换为DTO
            List<NumberResourceDTO> dtoList = new ArrayList<>();
            for (NumberResource resource : dataList) {
                if (StringUtils.isBlank(resource.getNumber())) {
                    throw new BusinessException("Number cannot be empty");
                }
                
                NumberResourceDTO dto = new NumberResourceDTO();
                BeanUtils.copyProperties(resource, dto);
                dtoList.add(dto);
            }
            
            // 调用现有的批量导入方法（DTO版本）
            boolean result = this.batchImportDTOs(dtoList);
            
            log.info("Batch import completed, processed {} records, result: {}", dtoList.size(), result);
            return result;
            
        } catch (Exception e) {
            log.error("Batch import failed", e);
            throw new BusinessException("Batch import failed: " + e.getMessage());
        }
    }
    
    /**
     * 批量导入DTO数据（重命名原有方法避免冲突）
     */
    private boolean batchImportDTOs(List<NumberResourceDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new BusinessException("Batch import data cannot be empty");
        }
        
        try {
            // 统计每个号段的状态变化
            Map<Long, Map<Integer, Integer>> segmentStatusChanges = new HashMap<>();
            
            // 分批处理，每批1000条
            int batchSize = 1000;
            int totalSize = dtoList.size();
            
            for (int i = 0; i < totalSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, totalSize);
                List<NumberResourceDTO> batchList = dtoList.subList(i, endIndex);
                
                // 验证数据
                for (NumberResourceDTO dto : batchList) {
                    if (StringUtils.isBlank(dto.getNumber())) {
                        throw new BusinessException("Number cannot be empty");
                    }
                }
                
                // 转换为实体并保存
                List<NumberResource> entityList = new ArrayList<>();
                for (NumberResourceDTO dto : batchList) {
                    NumberResource entity = new NumberResource();
                    BeanUtils.copyProperties(dto, entity);
                    
                    // 设置默认值
                    if (entity.getStatus() == null) {
                        entity.setStatus(NumberStatusEnum.IDLE.getCode());
                    }
                    if (entity.getCreateTime() == null) {
                        entity.setCreateTime(new Date());
                    }
                    if (entity.getUpdateTime() == null) {
                        entity.setUpdateTime(new Date());
                    }
                    
                    entityList.add(entity);
                    
                    // 统计号段状态变化
                    Long segmentId = entity.getSegmentId();
                    Integer status = entity.getStatus();
                    if (segmentId != null && status != null) {
                        segmentStatusChanges.computeIfAbsent(segmentId, k -> new HashMap<>())
                                .merge(status, NumberStatusEnum.IDLE.getCode(), Integer::sum);
                    }
                }
                
                // 批量保存
                this.saveBatch(entityList);
                
                // 为每个号码资源记录操作日志
                for (NumberResource entity : entityList) {
                    operationLogService.recordLog(entity.getNumberId(), entity.getNumber(), 
                        entity.getNumberType(), BatchOperationTypeEnum.CREATE.getCode(), 
                        null, entity.getStatus(), null, null, entity.getAttributiveOrg(), 
                        ResultStatusEnum.SUCCESS.getCode(), "Number resource imported");
                }
            }
            
            // 更新号段库存统计信息
            for (Map.Entry<Long, Map<Integer, Integer>> entry : segmentStatusChanges.entrySet()) {
                Long segmentId = entry.getKey();
                Map<Integer, Integer> statusChanges = entry.getValue();
                
                // 调用号段服务的批量增量更新统计方法
                segmentService.batchIncrementalUpdateStatistics(segmentId, statusChanges);
            }
            
            // 记录批量导入操作日志
//            operationLogService.recordLog(null, null, null, BatchOperationTypeEnum.CREATE.getCode(),
//                null, null, null, null, null, ResultStatusEnum.SUCCESS.getCode(),
//                String.format("Batch import completed, successfully processed %d number resources", totalSize), null);
            
            return true;
            
        } catch (Exception e) {
            log.error("Batch import DTOs failed", e);
            throw new BusinessException("Batch import failed: " + e.getMessage());
        }

    }

}