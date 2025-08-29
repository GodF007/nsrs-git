package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.entity.SimCardOperation;
import com.nsrs.simcard.utils.SimCardConstant;
import com.nsrs.simcard.mapper.SimCardMapper;
import com.nsrs.simcard.mapper.SimCardOperationMapper;
import com.nsrs.simcard.model.dto.SimCardOperationDTO;
import com.nsrs.simcard.model.query.SimCardOperationQuery;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.simcard.service.SimCardOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SIM卡操作记录服务实现
 */
@Slf4j
@Service
public class SimCardOperationServiceImpl extends ServiceImpl<SimCardOperationMapper, SimCardOperation> implements SimCardOperationService {

    @Autowired
    private SimCardMapper simCardMapper;
    
    @Override
    public PageResult<SimCardOperationDTO> pageOperation(PageRequest<SimCardOperationQuery> request) {
        // 参数校验
        if (request == null || request.getQuery() == null) {
            throw new IllegalArgumentException("Request and query data cannot be null");
        }
        
        SimCardOperationQuery query = request.getQuery();
        
        // 构建查询条件
        LambdaQueryWrapper<SimCardOperation> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (query.getCardId() != null) {
            queryWrapper.eq(SimCardOperation::getCardId, query.getCardId());
        }
        
        if (StringUtils.hasText(query.getIccid())) {
            queryWrapper.like(SimCardOperation::getIccid, query.getIccid());
        }
        
        if (query.getOperationType() != null) {
            queryWrapper.eq(SimCardOperation::getOperationType, query.getOperationType());
        }
        
        if (query.getOperatorUserId() != null) {
            queryWrapper.eq(SimCardOperation::getOperatorUserId, query.getOperatorUserId());
        }
        
        if (query.getStockOutOrgId() != null) {
            queryWrapper.eq(SimCardOperation::getStockOutOrgId, query.getStockOutOrgId());
        }
        
        if (query.getStockInOrgId() != null) {
            queryWrapper.eq(SimCardOperation::getStockInOrgId, query.getStockInOrgId());
        }
        
        if (query.getResultStatus() != null) {
            queryWrapper.eq(SimCardOperation::getResultStatus, query.getResultStatus());
        }
        
        // 处理日期范围查询
        if (StringUtils.hasText(query.getBeginDate())) {
            queryWrapper.ge(SimCardOperation::getOperationTime, query.getBeginDate());
        }
        
        if (StringUtils.hasText(query.getEndDate())) {
            queryWrapper.le(SimCardOperation::getOperationTime, query.getEndDate());
        }
        
        // 按操作时间降序排序
        queryWrapper.orderByDesc(SimCardOperation::getOperationTime);
        
        // 执行分页查询
        Page<SimCardOperation> page = new Page<>(request.getCurrent(), request.getSize());
        Page<SimCardOperation> pageResult = page(page, queryWrapper);
        
        // 转换为DTO
        List<SimCardOperationDTO> operationDTOList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 返回分页结果
        return new PageResult<>(
            operationDTOList, 
            pageResult.getTotal(), 
            pageResult.getCurrent(), 
            pageResult.getSize()
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOperation(SimCardOperationDTO operationDTO) {
        // 【数据完整性验证】由于分表环境下外键约束已被移除，需要在代码层面确保数据完整性
        // 【删除操作特殊处理】对于删除操作，跳过SIM卡存在性验证，因为SIM卡已在同一事务中被删除
        boolean isDeleteOperation = SimCardConstant.OPERATION_DELETE == operationDTO.getOperationType();
        
        // 【分表兼容性修复】优先使用ICCID验证卡片存在性（删除操作除外）
        if (StringUtils.hasText(operationDTO.getIccid()) && !isDeleteOperation) {
            // 使用ICCID查询（分表友好）- 直接使用Mapper避免循环依赖
            LambdaQueryWrapper<SimCard> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SimCard::getIccid, operationDTO.getIccid());
            SimCard card = simCardMapper.selectOne(wrapper);
            if (card == null) {
                log.error("[Data Integrity Check] SIM card not found with ICCID: {}", operationDTO.getIccid());
                throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
            }
            
            // 如果cardId为空，从查询结果中获取
            if (operationDTO.getCardId() == null) {
                operationDTO.setCardId(card.getId());
                log.debug("[Data Integrity] Auto-filled cardId: {} for ICCID: {}", card.getId(), operationDTO.getIccid());
            } else if (!operationDTO.getCardId().equals(card.getId())) {
                // 验证提供的cardId与ICCID是否匹配
                log.error("[Data Integrity Check] CardId {} does not match ICCID {}, expected cardId: {}", 
                    operationDTO.getCardId(), operationDTO.getIccid(), card.getId());
                throw new BusinessException("CardId and ICCID do not match");
            }
        } else if (operationDTO.getCardId() != null && !isDeleteOperation) {
            // 【分表查询警告】仅通过ID查询在分表环境下可能存在问题
             log.warn("[Sharding Query Warning] Using cardId without ICCID may result in incomplete data, cardId: {}", operationDTO.getCardId());
             log.warn("[Sharding Query Suggestion] Strongly recommend providing ICCID for accurate query");
            
            // 尝试通过ID查询（可能无法找到分表中的数据）- 直接使用Mapper避免循环依赖
            SimCard card = simCardMapper.selectById(operationDTO.getCardId());
            if (card == null) {
                log.error("[Data Integrity Check] SIM card not found with cardId: {}", operationDTO.getCardId());
                throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
            }
            
            // 从查询结果中获取ICCID
            if (!StringUtils.hasText(operationDTO.getIccid()) && StringUtils.hasText(card.getIccid())) {
                operationDTO.setIccid(card.getIccid());
                log.debug("[Data Integrity] Auto-filled ICCID: {} for cardId: {}", card.getIccid(), operationDTO.getCardId());
            }
        } else if (!isDeleteOperation) {
            // 既没有ICCID也没有cardId（删除操作除外）
            log.error("[Data Integrity Check] Both cardId and ICCID are missing in operation request");
            throw new BusinessException("Either cardId or ICCID must be provided");
        } else {
            log.debug("[Delete Operation] Skipping cardId/ICCID validation for delete operation");
        }
        
        // 创建操作记录实体并设置属性
        SimCardOperation operation = new SimCardOperation();
        BeanUtils.copyProperties(operationDTO, operation);
        
        // 设置默认值
        if (operation.getOperationTime() == null) {
            operation.setOperationTime(new Date());
        }
        
        if (operation.getResultStatus() == null) {
            operation.setResultStatus(1); // Default success
        }
        
        // 设置创建和更新时间
        Date now = new Date();
        operation.setCreateTime(now);
        operation.setUpdateTime(now);
        
        // 保存操作记录
        return save(operation);
    }
    
    @Override
    public SimCardOperationDTO getOperationDetail(Long id) {
        // 获取操作记录信息
        SimCardOperation operation = getById(id);
        if (operation == null) {
            return null;
        }
        
        // 转换为DTO并返回
        return convertToDTO(operation);
    }
    
    @Override
    public List<SimCardOperationDTO> getOperationsByCardId(Long cardId) {
        // 查询指定卡ID的操作记录
        List<SimCardOperation> operationList = list(new LambdaQueryWrapper<SimCardOperation>()
                .eq(SimCardOperation::getCardId, cardId)
                .orderByDesc(SimCardOperation::getOperationTime));
        
        // 转换为DTO列表
        return operationList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SimCardOperationDTO> getOperationsByIccid(String iccid) {
        // 查询指定ICCID的操作记录
        List<SimCardOperation> operationList = list(new LambdaQueryWrapper<SimCardOperation>()
                .eq(SimCardOperation::getIccid, iccid)
                .orderByDesc(SimCardOperation::getOperationTime));
        
        // 转换为DTO列表
        return operationList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SimCardOperationDTO> getRecentOperations(int limit) {
        // 获取最近的操作记录
        List<SimCardOperation> operationList = list(new LambdaQueryWrapper<SimCardOperation>()
                .orderByDesc(SimCardOperation::getOperationTime)
                .last("limit " + limit));
        
        // 转换为DTO列表
        return operationList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Object> countByOperationType(String beginDate, String endDate) {
        LambdaQueryWrapper<SimCardOperation> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加日期范围查询条件
        if (StringUtils.hasText(beginDate)) {
            queryWrapper.ge(SimCardOperation::getOperationTime, beginDate);
        }
        
        if (StringUtils.hasText(endDate)) {
            queryWrapper.le(SimCardOperation::getOperationTime, endDate);
        }
        
        // 查询所有符合条件的操作记录
        List<SimCardOperation> operationList = list(queryWrapper);
        
        // 按操作类型分组统计
        Map<Integer, Long> countMap = operationList.stream()
                .collect(Collectors.groupingBy(SimCardOperation::getOperationType, Collectors.counting()));
        
        // 转换为结果列表
        List<Object> result = new ArrayList<>();
        countMap.forEach((type, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("type", type);
            item.put("typeName", getOperationTypeName(type));
            item.put("count", count);
            result.add(item);
        });
        
        return result;
    }
    
    @Override
    public Map<String, Object> countByOperationType(SimCardOperationQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<SimCardOperation> queryWrapper = buildQueryWrapper(query);
        
        // 查询所有符合条件的操作记录
        List<SimCardOperation> operationList = list(queryWrapper);
        
        // 按操作类型分组统计
        Map<Integer, Long> countMap = operationList.stream()
                .collect(Collectors.groupingBy(SimCardOperation::getOperationType, Collectors.counting()));
        
        // 转换为结果Map
        Map<String, Object> result = new HashMap<>();
        countMap.forEach((type, count) -> {
            result.put(getOperationTypeName(type), count);
        });
        
        return result;
    }
    
    @Override
    public Map<String, Object> countByResultStatus(SimCardOperationQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<SimCardOperation> queryWrapper = buildQueryWrapper(query);
        
        // 查询所有符合条件的操作记录
        List<SimCardOperation> operationList = list(queryWrapper);
        
        // 按结果状态分组统计
        Map<Integer, Long> countMap = operationList.stream()
                .collect(Collectors.groupingBy(SimCardOperation::getResultStatus, Collectors.counting()));
        
        // 转换为结果Map
        Map<String, Object> result = new HashMap<>();
        countMap.forEach((status, count) -> {
            result.put(status == 1 ? "Success" : "Failed", count);
        });
        
        return result;
    }
    
    /**
     * 构建查询条件
     * @param query 查询条件
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<SimCardOperation> buildQueryWrapper(SimCardOperationQuery query) {
        LambdaQueryWrapper<SimCardOperation> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query.getCardId() != null) {
            queryWrapper.eq(SimCardOperation::getCardId, query.getCardId());
        }
        
        if (StringUtils.hasText(query.getIccid())) {
            queryWrapper.like(SimCardOperation::getIccid, query.getIccid());
        }
        
        if (query.getOperationType() != null) {
            queryWrapper.eq(SimCardOperation::getOperationType, query.getOperationType());
        }
        
        if (query.getOperatorUserId() != null) {
            queryWrapper.eq(SimCardOperation::getOperatorUserId, query.getOperatorUserId());
        }
        
        if (query.getStockOutOrgId() != null) {
            queryWrapper.eq(SimCardOperation::getStockOutOrgId, query.getStockOutOrgId());
        }
        
        if (query.getStockInOrgId() != null) {
            queryWrapper.eq(SimCardOperation::getStockInOrgId, query.getStockInOrgId());
        }
        
        if (query.getResultStatus() != null) {
            queryWrapper.eq(SimCardOperation::getResultStatus, query.getResultStatus());
        }
        
        // 处理日期范围查询
        if (StringUtils.hasText(query.getBeginDate())) {
            queryWrapper.ge(SimCardOperation::getOperationTime, query.getBeginDate());
        }
        
        if (StringUtils.hasText(query.getEndDate())) {
            queryWrapper.le(SimCardOperation::getOperationTime, query.getEndDate());
        }
        
        return queryWrapper;
    }
    
    /**
     * 将实体转换为DTO
     * @param operation 操作记录实体
     * @return 操作记录DTO
     */
    private SimCardOperationDTO convertToDTO(SimCardOperation operation) {
        if (operation == null) {
            return null;
        }
        
        SimCardOperationDTO dto = new SimCardOperationDTO();
        BeanUtils.copyProperties(operation, dto);
        
        // 设置操作类型名称
        if (operation.getOperationType() != null) {
            dto.setOperationTypeName(getOperationTypeName(operation.getOperationType()));
        }
        
        // 设置状态名称
        if (operation.getOldStatus() != null) {
            dto.setOldStatusName(getCardStatusName(operation.getOldStatus()));
        }
        
        if (operation.getNewStatus() != null) {
            dto.setNewStatusName(getCardStatusName(operation.getNewStatus()));
        }
        
        // 设置操作结果状态名称
        if (operation.getResultStatus() != null) {
            dto.setResultStatusName(operation.getResultStatus() == 1 ? "Success" : "Failed");
        }
        
        // 这里可以添加关联数据的查询，如操作用户名称、组织名称等
        // 这需要依赖其他服务，在实际开发中需要添加相应的依赖注入和调用
        
        return dto;
    }
    
    /**
     * 获取操作类型名称
     * @param operationType 操作类型
     * @return 操作类型名称
     */
    private String getOperationTypeName(Integer operationType) {
        if (operationType == null) {
            return "";
        }
        
        switch (operationType) {
            case 1: return "Allocate";
            case 2: return "Recycle";
            case 3: return "Activate";
            case 4: return "Deactivate";
            case 5: return "Update";
            case 6: return "Delete";
            case 7: return "Add";
            case 8: return "Import";
            default: return "Unknown";
        }
    }
    
    /**
     * 获取卡状态名称
     * @param status 状态
     * @return 状态名称
     */
    private String getCardStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        
        switch (status) {
            case 1: return "Published";
            case 2: return "Allocated";
            case 3: return "Activated";
            case 4: return "Deactivated";
            case 5: return "Recycled";
            default: return "Unknown";
        }
    }
}