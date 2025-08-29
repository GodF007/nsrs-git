package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.common.utils.SequenceService;
import com.nsrs.simcard.constants.StatusConstants;
import com.nsrs.simcard.dto.SimCardBatchOperationRequest;
import com.nsrs.simcard.dto.SimCardDetailDTO;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.entity.SimCardBatch;
import com.nsrs.simcard.entity.ImsiIccidMapping;
import com.nsrs.simcard.mapper.SimCardMapper;
import com.nsrs.simcard.model.dto.SimCardDTO;
import com.nsrs.simcard.model.dto.SimCardOperationDTO;
import com.nsrs.simcard.model.query.SimCardQuery;
import com.nsrs.simcard.service.SimCardBatchStockService;
import com.nsrs.simcard.service.SimCardOperationService;
import com.nsrs.simcard.service.SimCardService;
import com.nsrs.simcard.service.SimCardBatchService;
import com.nsrs.simcard.service.ImsiIccidMappingService;
import com.nsrs.simcard.utils.SimCardConstant;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.enums.SimCardStatusEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * SIM卡服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimCardServiceImpl extends ServiceImpl<SimCardMapper, SimCard> implements SimCardService {

    private final SimCardBatchService simCardBatchService;
    private final SimCardBatchStockService simCardBatchStockService;
    private final SimCardOperationService simCardOperationService;
    private final SequenceService sequenceService;
    private final ImsiIccidMappingService imsiIccidMappingService;

    @Override
    public PageResult<SimCard> getPage(int page, int size, Map<String, Object> params) {
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (MapUtils.isNotEmpty(params)) {
            // ICCID查询
            String iccid = MapUtils.getString(params, "iccid");
            if (StringUtils.isNotBlank(iccid)) {
                queryWrapper.like(SimCard::getIccid, iccid);
            }
            
            // IMSI查询
            String imsi = MapUtils.getString(params, "imsi");
            if (StringUtils.isNotBlank(imsi)) {
                queryWrapper.like(SimCard::getImsi, imsi);
            }
            
            // 批次ID查询
            Long batchId = MapUtils.getLong(params, "batchId");
            if (Objects.nonNull(batchId)) {
                queryWrapper.eq(SimCard::getBatchId, batchId);
            }
            
            // 状态查询
            Integer status = MapUtils.getInteger(params, "status");
            if (Objects.nonNull(status)) {
                queryWrapper.eq(SimCard::getStatus, status);
            }
            
            // 组织ID查询
            Long orgId = MapUtils.getLong(params, "orgId");
            if (Objects.nonNull(orgId)) {
                queryWrapper.eq(SimCard::getOrganizationId, orgId);
            }
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(SimCard::getCreateTime);
        
        IPage<SimCard> pageResult = this.page(new Page<>(page, size), queryWrapper);
        return new PageResult<SimCard>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }
    


    /**
     * 通过ID查询SimCard（适配分表）
     * 由于按iccid分表，仅通过ID查询可能无法定位到具体分表
     * ShardingJDBC在没有分表字段时不会自动查询所有表，需要特殊处理
     */
    @Override
    public SimCard getSimCardById(Long cardId) {
        if (cardId == null) {
            return null;
        }
        
        // 【分表查询警告】在分表环境下，仅通过ID查询存在严重问题！
        log.warn("[Sharding Query Warning] Using ID to query SIM card may result in incomplete data, cardId: {}", cardId);
        log.warn("[Sharding Query Suggestion] Strongly recommend using getSimCardByIccid method for accurate data query");
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getId, cardId);
        
        // 注意：在ShardingJDBC分表环境下，此查询可能只查询默认分表
        // 如果记录不在默认分表中，将查询不到结果
        SimCard result = this.getOne(queryWrapper);
        
        if (result == null) {
            log.error("[Sharding Query Failed] No SIM card data found by ID, possible reasons: 1.Data does not exist 2.Data is in other shards, cardId: {}", cardId);
        }
        
        return result;
    }
    
    /**
     * 通过ID和ICCID查询SimCard（分表友好）
     * 推荐在分表环境下使用此方法
     */
    private SimCard getSimCardByIdAndIccid(Long cardId, String iccid) {
        if (cardId == null || StringUtils.isBlank(iccid)) {
            return null;
        }
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getId, cardId)
                   .eq(SimCard::getIccid, iccid);
        
        return this.getOne(queryWrapper);
    }
    
    /**
     * 批量通过ID查询SimCard（适配分表）
     * 【警告】在分表环境下，仅通过ID查询存在严重问题！
     */
    private List<SimCard> getSimCardsByIds(List<Long> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 【分表查询警告】批量ID查询在分表环境下存在严重问题！
        log.warn("[Sharding Query Warning] Batch query using IDs may result in incomplete data, cardIds: {}", cardIds);
        log.warn("[Sharding Query Suggestion] Strongly recommend using getSimCardsByIccids method for batch query to ensure data accuracy");
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SimCard::getId, cardIds);
        
        // 注意：在ShardingJDBC分表环境下，此查询可能只查询默认分表
        // 如果记录分布在多个分表中，可能查询不全
        List<SimCard> result = this.list(queryWrapper);
        
        // 检查查询结果完整性
        if (result.size() < cardIds.size()) {
            log.error("[Sharding Query Exception] Batch query result incomplete! Expected: {}, Actual: {}, Possible reason: Data distributed in other shards",
                    cardIds.size(), result.size());
            log.error("[Sharding Query Exception] Missing IDs may be in other shards, recommend using ICCID for query");
        }
        
        return result;
    }
    
    /**
     * 批量通过ICCID查询SimCard（分表友好）
     * 推荐在分表环境下使用此方法
     */
    private List<SimCard> getSimCardsByIccids(List<String> iccids) {
        if (iccids == null || iccids.isEmpty()) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SimCard::getIccid, iccids);
        
        return this.list(queryWrapper);
    }
    
    // ==================== 以ICCID为驱动的CRUD接口 ====================
    

    

    

    

    
    /**
     * 批量更新SimCard（适配分表）
     * 由于分表环境下批量操作可能跨表，需要逐个更新
     * 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
     */
    private boolean updateBatchByIdForSharding(List<SimCard> simCards) {
        if (simCards == null || simCards.isEmpty()) {
            return true;
        }
        
        for (SimCard simCard : simCards) {
            // 使用ICCID进行更新，避免分表键更新问题
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, simCard.getIccid());
            
            // 设置需要更新的字段
            if (simCard.getImsi() != null) {
                updateWrapper.set(SimCard::getImsi, simCard.getImsi());
            }
            if (simCard.getStatus() != null) {
                updateWrapper.set(SimCard::getStatus, simCard.getStatus());
            }
            if (simCard.getBatchId() != null) {
                updateWrapper.set(SimCard::getBatchId, simCard.getBatchId());
            }
            if (simCard.getOrganizationId() != null) {
                updateWrapper.set(SimCard::getOrganizationId, simCard.getOrganizationId());
            }
            if (simCard.getCardTypeId() != null) {
                updateWrapper.set(SimCard::getCardTypeId, simCard.getCardTypeId());
            }
            if (simCard.getSupplierId() != null) {
                updateWrapper.set(SimCard::getSupplierId, simCard.getSupplierId());
            }
            updateWrapper.set(SimCard::getUpdateTime, new Date());
            
            if (!this.update(updateWrapper)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 批量删除SimCard（适配分表）
     * 由于分表环境下批量操作可能跨表，需要逐个删除
     */
    private boolean removeBatchByIdsForSharding(List<Long> cardIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            return true;
        }
        
        for (Long cardId : cardIds) {
            if (!this.removeById(cardId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public SimCardDetailDTO getSimCardDetail(Long cardId) {
        SimCard simCard = getSimCardById(cardId);
        if (simCard == null) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
        }
        
        SimCardDetailDTO detailDTO = new SimCardDetailDTO();
        BeanUtils.copyProperties(simCard, detailDTO);
        
        // 获取批次信息
        if (simCard.getBatchId() != null) {
            SimCardBatch batch = simCardBatchService.getById(simCard.getBatchId());
            if (batch != null) {
                detailDTO.setBatchName(batch.getBatchName());
            }
        }
        
        return detailDTO;
    }

    @Override
    public SimCard getSimCardByIccid(String iccid) {
        if (StringUtils.isBlank(iccid)) {
            return null;
        }
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getIccid, iccid);

//        String minIccid = generateMinIccidForSuffix(iccid);
//        String maxIccid = generateMaxIccidForSuffix(iccid);
//        queryWrapper.likeLeft(SimCard::getIccid, iccid);
//        queryWrapper.between(SimCard::getIccid, minIccid, maxIccid);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据ICCID后缀生成最小ICCID值
     * 用于范围查询的下边界
     *
     * @param iccidSuffix ICCID后缀
     * @return 最小ICCID值
     */
//    private String generateMinIccidForSuffix(String iccidSuffix) {
//        if (StringUtils.isBlank(iccidSuffix)) {
//            return "";
//        }
//
//        // 构造以指定后缀结尾的最小ICCID
//        // 例如：后缀为"123"，则生成"000000000000000123"
//        StringBuilder minIccid = new StringBuilder();
//
//        // ICCID通常为19-20位，这里假设20位
//        int totalLength = 20;
//        int suffixLength = iccidSuffix.length();
//
//        // 前面补0
//        for (int i = 0; i < totalLength - suffixLength; i++) {
//            minIccid.append("0");
//        }
//        minIccid.append(iccidSuffix);
//
//        return minIccid.toString();
//    }
//
//    /**
//     * 根据ICCID后缀生成最大ICCID值
//     * 用于范围查询的上边界
//     *
//     * @param iccidSuffix ICCID后缀
//     * @return 最大ICCID值
//     */
//    private String generateMaxIccidForSuffix(String iccidSuffix) {
//        if (StringUtils.isBlank(iccidSuffix)) {
//            return "zzzzzzzzzzzzzzzzzzzz"; // 返回一个很大的值
//        }
//
//        // 构造以指定后缀结尾的最大ICCID
//        // 例如：后缀为"123"，则生成"999999999999999123"
//        StringBuilder maxIccid = new StringBuilder();
//
//        // ICCID通常为19-20位，这里假设20位
//        int totalLength = 20;
//        int suffixLength = iccidSuffix.length();
//
//        // 前面补9
//        for (int i = 0; i < totalLength - suffixLength; i++) {
//            maxIccid.append("9");
//        }
//        maxIccid.append(iccidSuffix);
//
//        return maxIccid.toString();
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchOperation(SimCardBatchOperationRequest request, Long userId) {
        if (request == null || request.getSimCardIds() == null || request.getSimCardIds().isEmpty()) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_ID_LIST_EMPTY.getMessage());
        }
        
        String operationType = request.getOperationType();
        if (StringUtils.isBlank(operationType)) {
            throw new BusinessException(ErrorMessageEnum.OPERATION_TYPE_REQUIRED.getMessage());
        }
        
        switch (operationType) {
            case "ACTIVATE":
                return batchActivate(request, userId);
            case "DEACTIVATE":
                return batchDeactivate(request, userId);
            case "DELETE":
                return batchDelete(request);
            case "TRANSFER":
                return batchTransferBatch(request, userId);
            case "SET_GROUP":
                return batchSetOrg(request, userId);
            default:
                throw new BusinessException(ErrorMessageEnum.UNSUPPORTED_OPERATION_TYPE.getMessage());
        }
    }

    /**
     * 批量激活SIM卡
     */
    private boolean batchActivate(SimCardBatchOperationRequest request, Long userId) {
        List<SimCard> simCards = getSimCardsByIds(request.getSimCardIds());
        for (SimCard simCard : simCards) {
            simCard.setStatus(SimCardConstant.STATUS_ACTIVATED); // 已激活
            simCard.setUpdateTime(new Date());
        }
        return updateBatchByIdForSharding(simCards);
    }

    /**
     * 批量停用SIM卡
     */
    private boolean batchDeactivate(SimCardBatchOperationRequest request, Long userId) {
        List<SimCard> simCards = getSimCardsByIds(request.getSimCardIds());
        for (SimCard simCard : simCards) {
            simCard.setStatus(SimCardConstant.STATUS_DEACTIVATED); // 已停用
            simCard.setUpdateTime(new Date());
        }
        return updateBatchByIdForSharding(simCards);
    }

    /**
     * 批量删除SIM卡
     */
    private boolean batchDelete(SimCardBatchOperationRequest request) {
        return removeBatchByIdsForSharding(request.getSimCardIds());
    }

    /**
     * 批量转移批次
     */
    private boolean batchTransferBatch(SimCardBatchOperationRequest request, Long userId) {
        if (request.getBatchId() == null) {
            throw new BusinessException(ErrorMessageEnum.BATCH_ID_REQUIRED.getMessage());
        }
        
        // 验证批次是否存在
        SimCardBatch batch = simCardBatchService.getById(request.getBatchId());
        if (batch == null) {
            throw new BusinessException(ErrorMessageEnum.BATCH_NOT_FOUND.getMessage());
        }
        
        List<SimCard> simCards = getSimCardsByIds(request.getSimCardIds());
        for (SimCard simCard : simCards) {
            simCard.setBatchId(request.getBatchId());
            simCard.setUpdateTime(new Date());
        }
        return updateBatchByIdForSharding(simCards);
    }
    
    /**
     * 批量设置组织
     */
    private boolean batchSetOrg(SimCardBatchOperationRequest request, Long userId) {
        if (request.getOrgId() == null) {
            throw new BusinessException(ErrorMessageEnum.ORG_ID_REQUIRED.getMessage());
        }
        
        List<SimCard> simCards = getSimCardsByIds(request.getSimCardIds());
        for (SimCard simCard : simCards) {
            simCard.setOrganizationId(request.getOrgId());
            simCard.setUpdateTime(new Date());
        }
        return updateBatchByIdForSharding(simCards);
    }



    @Override
    public Map<String, Object> countByStatus(Map<String, Object> params) {
        Map<String, Object> statusCountMap = new HashMap<>();
        
        // 组织ID条件
        Long orgId = MapUtils.getLong(params, "orgId");
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(orgId)) {
            queryWrapper.eq(SimCard::getOrganizationId, orgId);
        }
        
        // 批次ID条件
        Long batchId = MapUtils.getLong(params, "batchId");
        if (Objects.nonNull(batchId)) {
            queryWrapper.eq(SimCard::getBatchId, batchId);
        }
        
        // 查询总数
        long total = this.count(queryWrapper);
        statusCountMap.put("total", total);
        
        // 按状态统计
        for (int status = StatusConstants.SIMCARD_STATUS_PUBLISHED; status <= StatusConstants.SIMCARD_STATUS_RECYCLED; status++) {
            LambdaQueryWrapper<SimCard> statusWrapper = new LambdaQueryWrapper<SimCard>();
            statusWrapper.eq(SimCard::getStatus, status);
            if (Objects.nonNull(orgId)) {
                statusWrapper.eq(SimCard::getOrganizationId, orgId);
            }
            if (Objects.nonNull(batchId)) {
                statusWrapper.eq(SimCard::getBatchId, batchId);
            }
            
            long count = this.count(statusWrapper);
            String statusKey = "status" + status;
            statusCountMap.put(statusKey, count);
        }
        
        return statusCountMap;
    }
    
    // 以下是为了适配Controller接口而添加的方法

    @Override
    public PageResult<SimCardDTO> pageCard(PageRequest<SimCardQuery> request) {
        // 参数校验
        if (request == null || request.getQuery() == null) {
            throw new IllegalArgumentException("Request and query data cannot be null");
        }
        
        SimCardQuery query = request.getQuery();
        
        // 构建查询条件
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(query.getIccid())) {
            queryWrapper.like(SimCard::getIccid, query.getIccid());
        }
        
        if (StringUtils.isNotBlank(query.getImsi())) {
            queryWrapper.like(SimCard::getImsi, query.getImsi());
        }
        
        if (query.getBatchId() != null) {
            queryWrapper.eq(SimCard::getBatchId, query.getBatchId());
        }
        
        if (query.getOrgId() != null) {
            queryWrapper.eq(SimCard::getOrganizationId, query.getOrgId());
        }
        
        if (query.getStatus() != null) {
            queryWrapper.eq(SimCard::getStatus, query.getStatus());
        }
        
        // 分页查询
        Page<SimCard> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<SimCard> pageResult = this.page(page, queryWrapper);
        
        // 转换为DTO
        List<SimCardDTO> dtoList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageResult<SimCardDTO> result = new PageResult<>();
        result.setList(dtoList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(request.getCurrent());
        result.setPageSize(request.getSize());
        result.setPages((pageResult.getTotal() + request.getSize() - 1) / request.getSize());
        
        return result;
    }
    

    
    @Override
    public SimCardDTO getCardByIccid(String iccid) {
        SimCard simCard = getSimCardByIccid(iccid);
        return simCard != null ? convertToDTO(simCard) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCard(SimCardDTO cardDTO) {
        // 验证ICCID是否重复
        SimCard existingCard = getSimCardByIccid(cardDTO.getIccid());
        if (existingCard != null) {
            throw new BusinessException(ErrorMessageEnum.ICCID_ALREADY_EXISTS.getMessage());
        }
        
        SimCard simCard = new SimCard();
        BeanUtils.copyProperties(cardDTO, simCard);
        // 使用全局序列服务生成ID
        Long globalId = sequenceService.getNextSequenceValue("sim_card_id_seq");
        simCard.setId(globalId);
        // 修正orgId -> organizationId
        simCard.setOrganizationId(cardDTO.getOrgId());
        simCard.setCreateTime(new Date());
        simCard.setUpdateTime(new Date());
        
        boolean result = this.save(simCard);
        
        if (result) {
            // 更新批次库存信息
            if (cardDTO.getBatchId() != null && cardDTO.getStatus() != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    cardDTO.getBatchId(), null, cardDTO.getStatus(), 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(simCard.getId());
            operationDTO.setIccid(simCard.getIccid());
            operationDTO.setOperationType(SimCardConstant.OPERATION_ADD);
            operationDTO.setOldStatus(null);
            operationDTO.setNewStatus(cardDTO.getStatus() != null ? cardDTO.getStatus() : SimCardConstant.STATUS_PUBLISHED);
            operationDTO.setStockInOrgId(cardDTO.getOrgId());
            operationDTO.setRemark("Add new SIM card");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }
    
    @Override
     @Transactional(rollbackFor = Exception.class)
     public boolean updateCard(SimCardDTO cardDTO) {
        SimCard simCard = getSimCardById(cardDTO.getId());
        if (simCard == null) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
        }
        
        // 保存原始状态信息用于后续操作
        Integer oldStatus = simCard.getStatus();
        Long batchId = simCard.getBatchId();
        Long cardId = simCard.getId();
        String iccid = simCard.getIccid();
        
        // 如果修改了ICCID，需要验证唯一性
        if (!simCard.getIccid().equals(cardDTO.getIccid())) {
            SimCard existingCard = getSimCardByIccid(cardDTO.getIccid());
            if (existingCard != null && !existingCard.getId().equals(cardDTO.getId())) {
                throw new BusinessException(ErrorMessageEnum.ICCID_ALREADY_EXISTS.getMessage());
            }
        }
        
        BeanUtils.copyProperties(cardDTO, simCard);
        // 修正orgId -> organizationId
        simCard.setOrganizationId(cardDTO.getOrgId());
        simCard.setUpdateTime(new Date());
        
        // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
        LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SimCard::getIccid, iccid);
        
        // 设置需要更新的字段（避免更新分表键ICCID）
        if (simCard.getImsi() != null) {
            updateWrapper.set(SimCard::getImsi, simCard.getImsi());
        }
        if (simCard.getStatus() != null) {
            updateWrapper.set(SimCard::getStatus, simCard.getStatus());
        }
        if (simCard.getBatchId() != null) {
            updateWrapper.set(SimCard::getBatchId, simCard.getBatchId());
        }
        if (simCard.getOrganizationId() != null) {
            updateWrapper.set(SimCard::getOrganizationId, simCard.getOrganizationId());
        }
        if (simCard.getCardTypeId() != null) {
            updateWrapper.set(SimCard::getCardTypeId, simCard.getCardTypeId());
        }
        if (simCard.getSupplierId() != null) {
            updateWrapper.set(SimCard::getSupplierId, simCard.getSupplierId());
        }
        updateWrapper.set(SimCard::getUpdateTime, simCard.getUpdateTime());
        
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // 如果状态发生变化，更新批次库存状态
            if (cardDTO.getStatus() != null && !cardDTO.getStatus().equals(oldStatus)) {
                if (batchId != null) {
                    simCardBatchStockService.updateStockByStatusChange(
                        batchId, oldStatus, cardDTO.getStatus(), 1);
                }
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(cardId);
            operationDTO.setIccid(iccid);
            operationDTO.setOperationType(SimCardConstant.OPERATION_UPDATE);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(cardDTO.getStatus() != null ? cardDTO.getStatus() : oldStatus);
            operationDTO.setRemark("Update SIM card");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocateCards(List<Long> cardIds, Long orgId, Long operatorUserId) {
        if (cardIds == null || cardIds.isEmpty() || orgId == null) {
            throw new BusinessException(ErrorMessageEnum.INVALID_PARAMETERS.getMessage());
        }
        
        List<SimCard> simCards = getSimCardsByIds(cardIds);
        for (SimCard simCard : simCards) {
            Integer oldStatus = simCard.getStatus();
            Long oldOrgId = simCard.getOrganizationId();
            
            simCard.setOrganizationId(orgId);
            simCard.setStatus(SimCardConstant.STATUS_ASSIGNED); // 已分配
            simCard.setUpdateTime(new Date());
            
            // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, simCard.getIccid())
                        .set(SimCard::getOrganizationId, orgId)
                        .set(SimCard::getStatus, SimCardConstant.STATUS_ASSIGNED)
                        .set(SimCard::getUpdateTime, new Date());
            
            boolean result = this.update(updateWrapper);
                
            if (result) {
                // 更新批次库存状态
                if (simCard.getBatchId() != null) {
                    simCardBatchStockService.updateStockByStatusChange(
                        simCard.getBatchId(), oldStatus, SimCardConstant.STATUS_ASSIGNED, 1);
                }
                
                // 记录操作日志
                SimCardOperationDTO operationDTO = new SimCardOperationDTO();
                operationDTO.setCardId(simCard.getId());
                operationDTO.setIccid(simCard.getIccid());
                operationDTO.setOperationType(SimCardConstant.OPERATION_ASSIGN);
                operationDTO.setOperatorUserId(operatorUserId);
                operationDTO.setOldStatus(oldStatus);
                operationDTO.setNewStatus(SimCardConstant.STATUS_ASSIGNED);
                operationDTO.setStockOutOrgId(oldOrgId);
                operationDTO.setStockInOrgId(orgId);
                operationDTO.setRemark("Assign SIM card to organization");
                simCardOperationService.addOperation(operationDTO);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recycleCards(List<Long> cardIds, Long operatorUserId) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_ID_LIST_EMPTY.getMessage());
        }
        
        List<SimCard> simCards = getSimCardsByIds(cardIds);
        for (SimCard simCard : simCards) {
            Integer oldStatus = simCard.getStatus();
            simCard.setOrganizationId(null); // 清空组织ID
            simCard.setStatus(SimCardConstant.STATUS_RECYCLED); // 已回收
            simCard.setUpdateTime(new Date());
            
            // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, simCard.getIccid())
                        .set(SimCard::getOrganizationId, (Long)null) // 清空组织ID
                        .set(SimCard::getStatus, SimCardConstant.STATUS_RECYCLED)
                        .set(SimCard::getUpdateTime, new Date());
            
            boolean result = this.update(updateWrapper);
                
            if (result) {
                // 更新批次库存状态
                if (simCard.getBatchId() != null) {
                    simCardBatchStockService.updateStockByStatusChange(
                        simCard.getBatchId(), oldStatus, SimCardConstant.STATUS_RECYCLED, 1);
                }
                
                // 记录操作日志
                SimCardOperationDTO operationDTO = new SimCardOperationDTO();
                operationDTO.setCardId(simCard.getId());
                operationDTO.setIccid(simCard.getIccid());
                operationDTO.setOperationType(SimCardConstant.OPERATION_RECYCLE);
                operationDTO.setOperatorUserId(operatorUserId);
                operationDTO.setOldStatus(oldStatus);
                operationDTO.setNewStatus(SimCardConstant.STATUS_RECYCLED);
                operationDTO.setRemark("Recycle SIM card");
                simCardOperationService.addOperation(operationDTO);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allocateCardsByIccids(List<String> iccids, Long orgId, Long operatorUserId) {
        if (iccids == null || iccids.isEmpty()) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_ID_LIST_EMPTY.getMessage());
        }
        
        // 使用分表友好的查询方法
        List<SimCard> simCards = getSimCardsByIccids(iccids);
        if (simCards.size() != iccids.size()) {
            log.error("[Sharding Query] Some SIM cards not found by iccids. Expected: {}, Found: {}", 
                     iccids.size(), simCards.size());
            throw new BusinessException("Some SIM cards not found");
        }
        
        for (SimCard simCard : simCards) {
            Integer oldStatus = simCard.getStatus();
            simCard.setOrganizationId(orgId);
            simCard.setStatus(SimCardConstant.STATUS_ASSIGNED); // 已分配
            simCard.setUpdateTime(new Date());
            
            // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, simCard.getIccid())
                        .set(SimCard::getOrganizationId, orgId)
                        .set(SimCard::getStatus, SimCardConstant.STATUS_ASSIGNED)
                        .set(SimCard::getUpdateTime, new Date());
            
            boolean result = this.update(updateWrapper);
                
            if (result) {
                // 更新批次库存状态
                if (simCard.getBatchId() != null) {
                    simCardBatchStockService.updateStockByStatusChange(
                        simCard.getBatchId(), oldStatus, SimCardConstant.STATUS_ASSIGNED, 1);
                }
                
                // 记录操作日志
                SimCardOperationDTO operationDTO = new SimCardOperationDTO();
                operationDTO.setCardId(simCard.getId());
                operationDTO.setIccid(simCard.getIccid());
                operationDTO.setOperationType(SimCardConstant.OPERATION_ASSIGN);
                operationDTO.setOperatorUserId(operatorUserId);
                operationDTO.setOldStatus(oldStatus);
                operationDTO.setNewStatus(SimCardConstant.STATUS_ASSIGNED);
                operationDTO.setRemark("Allocate SIM card to organization: " + orgId);
                simCardOperationService.addOperation(operationDTO);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recycleCardsByIccids(List<String> iccids, Long operatorUserId) {
        if (iccids == null || iccids.isEmpty()) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_ID_LIST_EMPTY.getMessage());
        }
        
        // 使用分表友好的查询方法
        List<SimCard> simCards = getSimCardsByIccids(iccids);
        if (simCards.size() != iccids.size()) {
            log.error("[Sharding Query] Some SIM cards not found by iccids. Expected: {}, Found: {}", 
                     iccids.size(), simCards.size());
            throw new BusinessException("Some SIM cards not found");
        }
        
        for (SimCard simCard : simCards) {
            Integer oldStatus = simCard.getStatus();
            simCard.setOrganizationId(null); // 清空组织ID
            simCard.setStatus(SimCardConstant.STATUS_RECYCLED); // 已回收
            simCard.setUpdateTime(new Date());
            
            // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, simCard.getIccid())
                        .set(SimCard::getOrganizationId, (Long)null) // 清空组织ID
                        .set(SimCard::getStatus, SimCardConstant.STATUS_RECYCLED)
                        .set(SimCard::getUpdateTime, new Date());
            
            boolean result = this.update(updateWrapper);
                
            if (result) {
                // 更新批次库存状态
                if (simCard.getBatchId() != null) {
                    simCardBatchStockService.updateStockByStatusChange(
                        simCard.getBatchId(), oldStatus, SimCardConstant.STATUS_RECYCLED, 1);
                }
                
                // 记录操作日志
                SimCardOperationDTO operationDTO = new SimCardOperationDTO();
                operationDTO.setCardId(simCard.getId());
                operationDTO.setIccid(simCard.getIccid());
                operationDTO.setOperationType(SimCardConstant.OPERATION_RECYCLE);
                operationDTO.setOperatorUserId(operatorUserId);
                operationDTO.setOldStatus(oldStatus);
                operationDTO.setNewStatus(SimCardConstant.STATUS_RECYCLED);
                operationDTO.setRemark("Recycle SIM card");
                simCardOperationService.addOperation(operationDTO);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSimCardByIccid(String iccid) {
        if (StringUtils.isBlank(iccid)) {
            throw new BusinessException(ErrorMessageEnum.INVALID_PARAMETER.getMessage());
        }
        
        // 先获取SIM卡信息用于后续操作
        SimCard simCard = this.getSimCardByIccid(iccid);
        if (simCard == null) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
        }
        
        Integer oldStatus = simCard.getStatus();
        Long batchId = simCard.getBatchId();
        Long cardId = simCard.getId();
        
        // 直接通过ICCID删除，适用于分表环境
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getIccid, iccid);
        boolean result = this.remove(queryWrapper);
        
        if (result) {
            // 更新批次库存状态
            if (batchId != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    batchId, oldStatus, SimCardConstant.STATUS_DELETED, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(cardId);
            operationDTO.setIccid(iccid);
            operationDTO.setOperationType(SimCardConstant.OPERATION_DELETE);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(SimCardConstant.STATUS_DELETED);
            operationDTO.setRemark("Delete SIM card by ICCID");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSimCardByIccid(String iccid, SimCard simCard) {
        if (StringUtils.isBlank(iccid)) {
            throw new BusinessException(ErrorMessageEnum.INVALID_PARAMETER.getMessage());
        }
        
        // 先获取原始信息用于后续操作
        SimCard existingCard = this.getSimCardByIccid(iccid);
        if (existingCard == null) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
        }
        
        Integer oldStatus = existingCard.getStatus();
        Long batchId = existingCard.getBatchId();
        Long cardId = existingCard.getId();
        
        // 直接通过ICCID更新，适用于分表环境
        simCard.setUpdateTime(new Date());
        LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SimCard::getIccid, iccid);
        
        // 设置需要更新的字段
        if (simCard.getImsi() != null) {
            updateWrapper.set(SimCard::getImsi, simCard.getImsi());
        }
        if (simCard.getStatus() != null) {
            updateWrapper.set(SimCard::getStatus, simCard.getStatus());
        }
        if (simCard.getCardTypeId() != null) {
            updateWrapper.set(SimCard::getCardTypeId, simCard.getCardTypeId());
        }
        if (simCard.getOrganizationId() != null) {
            updateWrapper.set(SimCard::getOrganizationId, simCard.getOrganizationId());
        }
        updateWrapper.set(SimCard::getUpdateTime, simCard.getUpdateTime());
        
        boolean result = this.update(updateWrapper);
        
        if (result && simCard.getStatus() != null && !simCard.getStatus().equals(oldStatus)) {
            // 更新批次库存状态
            if (batchId != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    batchId, oldStatus, simCard.getStatus(), 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(cardId);
            operationDTO.setIccid(iccid);
            operationDTO.setOperationType(SimCardConstant.OPERATION_UPDATE);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(simCard.getStatus());
            operationDTO.setRemark("Update SIM card by ICCID");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateCardByIccid(String iccid, Long operatorUserId) {
        if (StringUtils.isBlank(iccid)) {
            throw new BusinessException(ErrorMessageEnum.INVALID_PARAMETER.getMessage());
        }
        
        SimCard simCard = this.getSimCardByIccid(iccid);
        if (simCard == null) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
        }
        
        Integer oldStatus = simCard.getStatus();
        simCard.setStatus(SimCardConstant.STATUS_ACTIVATED); // 已激活
        simCard.setUpdateTime(new Date());
        
        // 直接通过ICCID更新状态，适用于分表环境
        LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SimCard::getIccid, iccid)
                    .set(SimCard::getStatus, SimCardConstant.STATUS_ACTIVATED)
                    .set(SimCard::getUpdateTime, new Date());
        
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // 更新批次库存状态
            if (simCard.getBatchId() != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    simCard.getBatchId(), oldStatus, SimCardConstant.STATUS_ACTIVATED, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(simCard.getId());
            operationDTO.setIccid(simCard.getIccid());
            operationDTO.setOperationType(SimCardConstant.OPERATION_ACTIVATE);
            operationDTO.setOperatorUserId(operatorUserId);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(SimCardConstant.STATUS_ACTIVATED);
            operationDTO.setRemark("Activate SIM card by ICCID");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deactivateCardByIccid(String iccid, Long operatorUserId) {
        if (StringUtils.isBlank(iccid)) {
            throw new BusinessException(ErrorMessageEnum.INVALID_PARAMETER.getMessage());
        }
        
        SimCard simCard = this.getSimCardByIccid(iccid);
        if (simCard == null) {
            throw new BusinessException(ErrorMessageEnum.SIMCARD_NOT_FOUND.getMessage());
        }
        
        Integer oldStatus = simCard.getStatus();
        simCard.setStatus(SimCardConstant.STATUS_DEACTIVATED); // 已停用
        simCard.setUpdateTime(new Date());
        
        // 直接通过ICCID更新状态，适用于分表环境
        LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SimCard::getIccid, iccid)
                    .set(SimCard::getStatus, SimCardConstant.STATUS_DEACTIVATED)
                    .set(SimCard::getUpdateTime, new Date());
        
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // 更新批次库存状态
            if (simCard.getBatchId() != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    simCard.getBatchId(), oldStatus, SimCardConstant.STATUS_DEACTIVATED, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(simCard.getId());
            operationDTO.setIccid(simCard.getIccid());
            operationDTO.setOperationType(SimCardConstant.OPERATION_DEACTIVATE);
            operationDTO.setOperatorUserId(operatorUserId);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(SimCardConstant.STATUS_DEACTIVATED);
            operationDTO.setRemark("Deactivate SIM card by ICCID");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> countByCardType(SimCardQuery query) {
        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            if (query.getStatus() != null) {
                params.put("status", query.getStatus());
            }
            if (query.getBatchId() != null) {
                params.put("batchId", query.getBatchId());
            }
            if (query.getOrgId() != null) {
                params.put("orgId", query.getOrgId());
            }
        }
        
        List<Map<String, Object>> typeStats = baseMapper.countByCardType(params);
        Map<String, Object> result = new HashMap<>();
        result.put("typeStats", typeStats);
        result.put("total", typeStats.stream().mapToLong(stat -> 
            Long.parseLong(stat.get("count").toString())).sum());
        return result;
    }
    
    @Override
    public Map<String, Object> countByOrganization(SimCardQuery query) {
        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            if (query.getStatus() != null) {
                params.put("status", query.getStatus());
            }
            if (query.getBatchId() != null) {
                params.put("batchId", query.getBatchId());
            }
            if (query.getCardType() != null) {
                params.put("cardType", query.getCardType());
            }
        }
        
        List<Map<String, Object>> orgStats = baseMapper.countByOrganization(params);
        Map<String, Object> result = new HashMap<>();
        result.put("orgStats", orgStats);
        result.put("total", orgStats.stream().mapToLong(stat -> 
            Long.parseLong(stat.get("count").toString())).sum());
        return result;
    }
    
    // 其他接口方法实现
    
    @Override
    public PageResult<SimCardDetailDTO> page(long current, long size, String iccid, String imsi, Integer status, Long batchId, Long orgId) {
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(iccid)) {
            queryWrapper.eq(SimCard::getIccid, iccid);
        }
        
        if (StringUtils.isNotBlank(imsi)) {
            queryWrapper.like(SimCard::getImsi, imsi);
        }
        
        if (status != null) {
            queryWrapper.eq(SimCard::getStatus, status);
        }
        
        if (batchId != null) {
            queryWrapper.eq(SimCard::getBatchId, batchId);
        }
        
        if (orgId != null) {
            queryWrapper.eq(SimCard::getOrganizationId, orgId);
        }
        
        queryWrapper.orderByDesc(SimCard::getCreateTime);
        
        Page<SimCard> page = new Page<>(current, size);
        IPage<SimCard> pageResult = this.page(page, queryWrapper);
        
        List<SimCardDetailDTO> dtoList = pageResult.getRecords().stream().map(this::convertToDetailDTO).collect(Collectors.toList());
        
        return new PageResult<SimCardDetailDTO>(dtoList, pageResult.getTotal(), current, size);
    }
    
    @Override
    public SimCardDetailDTO getById(Long id) {
        SimCard simCard = getSimCardById(id);
        return simCard != null ? convertToDetailDTO(simCard) : null;
    }
    
    @Override
    public SimCardDetailDTO getByIccid(String iccid) {
        if (StringUtils.isBlank(iccid)) {
            return null;
        }
        
        SimCard simCard = getSimCardByIccid(iccid);
        return simCard != null ? convertToDetailDTO(simCard) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchImport(List<SimCard> simCards, Long batchId) {
        if (simCards == null || simCards.isEmpty()) {
            return false;
        }
        
        // 批量获取全局ID
        List<Long> globalIds = sequenceService.getBatchSequenceValues("sim_card_id_seq", simCards.size());
        
        Date now = new Date();
        for (int i = 0; i < simCards.size(); i++) {
            SimCard simCard = simCards.get(i);
            simCard.setId(globalIds.get(i));
            simCard.setBatchId(batchId);
            simCard.setCreateTime(now);
            simCard.setUpdateTime(now);
        }
        
        boolean result = this.saveBatch(simCards);
        
        if (result) {
            // 创建IMSI-ICCID映射关系
            List<ImsiIccidMapping> mappings = new ArrayList<>();
            for (SimCard simCard : simCards) {
                if (StringUtils.isNotBlank(simCard.getImsi()) && StringUtils.isNotBlank(simCard.getIccid())) {
                    ImsiIccidMapping mapping = new ImsiIccidMapping();
                    mapping.setImsi(simCard.getImsi());
                    mapping.setIccid(simCard.getIccid());
                    mappings.add(mapping);
                }
            }
            
            if (!mappings.isEmpty()) {
                imsiIccidMappingService.batchCreateMappings(mappings);
            }
            
            if (batchId != null) {
                // 更新批次库存状态
                simCardBatchStockService.updateBatchStock(batchId);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        SimCard simCard = getSimCardById(id);
        if (simCard == null) {
            return false;
        }
        
        Integer oldStatus = simCard.getStatus();
        
        // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
        LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SimCard::getIccid, simCard.getIccid())
                    .set(SimCard::getStatus, status)
                    .set(SimCard::getUpdateTime, new Date());
        
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // 更新批次库存状态
            if (simCard.getBatchId() != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    simCard.getBatchId(), oldStatus, status, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(simCard.getId());
            operationDTO.setIccid(simCard.getIccid());
            operationDTO.setOperationType(SimCardConstant.OPERATION_UPDATE);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(status);
            operationDTO.setRemark("Update SIM card status");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 【分表查询警告】批量ID查询在分表环境下存在严重问题！
        log.warn("[Sharding Query Warning] batchUpdateStatus using IDs may result in incomplete data, ids: {}", ids);
        log.warn("[Sharding Query Suggestion] Strongly recommend using batchUpdateStatusByIccids method for accurate batch update");
        
        // 先获取原始状态信息
        List<SimCard> originalCards = getSimCardsByIds(ids);
        
        // 【分表友好更新】逐个通过ICCID更新，避免分表键更新问题
        boolean allSuccess = true;
        for (SimCard originalCard : originalCards) {
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, originalCard.getIccid())
                        .set(SimCard::getStatus, status)
                        .set(SimCard::getUpdateTime, new Date());
            
            boolean result = this.update(updateWrapper);
            if (!result) {
                allSuccess = false;
                log.error("[Sharding Update Failed] Failed to update SIM card status for ICCID: {}", originalCard.getIccid());
                continue;
            }
            
            // 更新批次库存状态
            if (originalCard.getBatchId() != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    originalCard.getBatchId(), originalCard.getStatus(), status, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(originalCard.getId());
            operationDTO.setIccid(originalCard.getIccid());
            operationDTO.setOperationType(SimCardConstant.OPERATION_UPDATE);
            operationDTO.setOldStatus(originalCard.getStatus());
            operationDTO.setNewStatus(status);
            operationDTO.setRemark("Batch update SIM card status");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return allSuccess;
    }
    
    /**
     * 批量更新SIM卡状态（基于ICCID，分表友好）
     * 推荐在分表环境下使用此方法
     * 
     * @param iccids ICCID列表
     * @param status 新状态
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatusByIccids(List<String> iccids, Integer status) {
        if (iccids == null || iccids.isEmpty()) {
            return false;
        }
        
        // 先获取原始状态信息
        List<SimCard> originalCards = getSimCardsByIccids(iccids);
        if (originalCards.size() != iccids.size()) {
            log.error("[Sharding Query] Some SIM cards not found by iccids. Expected: {}, Found: {}", 
                     iccids.size(), originalCards.size());
            return false;
        }
        
        // 【分表友好更新】逐个通过ICCID更新
        boolean allSuccess = true;
        for (SimCard originalCard : originalCards) {
            LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SimCard::getIccid, originalCard.getIccid())
                        .set(SimCard::getStatus, status)
                        .set(SimCard::getUpdateTime, new Date());
            
            boolean result = this.update(updateWrapper);
            if (!result) {
                allSuccess = false;
                log.error("[Sharding Update Failed] Failed to update SIM card status for ICCID: {}", originalCard.getIccid());
                continue;
            }
            
            // 更新批次库存状态
            if (originalCard.getBatchId() != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    originalCard.getBatchId(), originalCard.getStatus(), status, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(originalCard.getId());
            operationDTO.setIccid(originalCard.getIccid());
            operationDTO.setOperationType(SimCardConstant.OPERATION_UPDATE);
            operationDTO.setOldStatus(originalCard.getStatus());
            operationDTO.setNewStatus(status);
            operationDTO.setRemark("Batch update SIM card status by ICCID");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return allSuccess;
    }
    
    @Override
    public long countByOrgId(Long orgId) {
        if (orgId == null) {
            return 0;
        }
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getOrganizationId, orgId);
        
        return this.count(queryWrapper);
    }
    
    @Override
    public long countByBatchId(Long batchId) {
        if (batchId == null) {
            return 0;
        }
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getBatchId, batchId);
        
        return this.count(queryWrapper);
    }
    
    @Override
    public List<SimCardDetailDTO> listByBatchId(Long batchId) {
        if (batchId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SimCard::getBatchId, batchId);
        
        List<SimCard> simCards = this.list(queryWrapper);
        return simCards.stream().map(this::convertToDetailDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRemark(Long id, String remark) {
        SimCard simCard = getSimCardById(id);
        if (simCard == null) {
            return false;
        }
        
        // 只有需要设置remark字段，如果SimCard类中没有该字段，需要添加
        if (simCard.getClass().getDeclaredFields().length > 0 && 
            java.util.Arrays.stream(simCard.getClass().getDeclaredFields())
                           .anyMatch(field -> field.getName().equals("remark"))) {
            try {
                java.lang.reflect.Method setRemark = simCard.getClass().getMethod("setRemark", String.class);
                setRemark.invoke(simCard, remark);
            } catch (Exception e) {
                log.error("Unable to set remark field: {}", e.getMessage());
                return false;
            }
        }
        
        // 【分表友好更新】使用ICCID进行更新，避免分表键更新问题
         LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
         updateWrapper.eq(SimCard::getIccid, simCard.getIccid())
                     .set(SimCard::getUpdateTime, new Date());
         
         // 如果SimCard有remark字段，则设置remark
         if (simCard.getClass().getDeclaredFields().length > 0 && 
             java.util.Arrays.stream(simCard.getClass().getDeclaredFields())
                            .anyMatch(field -> field.getName().equals("remark"))) {
             try {
                 java.lang.reflect.Method getRemark = simCard.getClass().getMethod("getRemark");
                 Object remarkValue = getRemark.invoke(simCard);
                 if (remarkValue != null) {
                     // 使用字符串字段名而不是Lambda表达式
                     updateWrapper.setSql("remark = '" + remarkValue.toString().replace("'", "''") + "'");
                 }
             } catch (Exception e) {
                 log.error("Unable to get remark field: {}", e.getMessage());
             }
         }
         
         return this.update(updateWrapper);
    }
    
    /**
     * 将SimCard实体转换为SimCardDTO
     */
    private SimCardDTO convertToDTO(SimCard simCard) {
        if (simCard == null) {
            return null;
        }
        
        SimCardDTO dto = new SimCardDTO();
        BeanUtils.copyProperties(simCard, dto);
        
        // 修正organizationId -> orgId
        dto.setOrgId(simCard.getOrganizationId());
        
        // 获取批次名称
        if (simCard.getBatchId() != null) {
            SimCardBatch batch = simCardBatchService.getById(simCard.getBatchId());
            if (batch != null) {
                dto.setBatchName(batch.getBatchName());
            }
        }
        
        // 设置状态描述
        switch (simCard.getStatus()) {
            case SimCardConstant.STATUS_PUBLISHED:
                dto.setStatusDesc(SimCardStatusEnum.PUBLISHED.getDescription());
                break;
            case SimCardConstant.STATUS_ASSIGNED:
                dto.setStatusDesc(SimCardStatusEnum.ASSIGNED.getDescription());
                break;
            case SimCardConstant.STATUS_ACTIVATED:
                dto.setStatusDesc(SimCardStatusEnum.ACTIVATED.getDescription());
                break;
            case SimCardConstant.STATUS_DEACTIVATED:
                dto.setStatusDesc(SimCardStatusEnum.DEACTIVATED.getDescription());
                break;
            case SimCardConstant.STATUS_RECYCLED:
                dto.setStatusDesc(SimCardStatusEnum.RECYCLED.getDescription());
                break;
            default:
                dto.setStatusDesc(SimCardStatusEnum.UNKNOWN.getDescription());
        }
        
        return dto;
    }
    
    /**
     * 将SimCard实体转换为SimCardDetailDTO
     */
    private SimCardDetailDTO convertToDetailDTO(SimCard simCard) {
        if (simCard == null) {
            return null;
        }
        
        SimCardDetailDTO dto = new SimCardDetailDTO();
        BeanUtils.copyProperties(simCard, dto);
        
        // 修正organizationId -> orgId
        dto.setOrgId(simCard.getOrganizationId());
        
        // 获取批次名称
        if (simCard.getBatchId() != null) {
            SimCardBatch batch = simCardBatchService.getById(simCard.getBatchId());
            if (batch != null) {
                dto.setBatchName(batch.getBatchName());
            }
        }
        
        // 设置状态描述
        switch (simCard.getStatus()) {
            case SimCardConstant.STATUS_PUBLISHED:
                dto.setStatusDesc(SimCardStatusEnum.PUBLISHED.getDescription());
                break;
            case SimCardConstant.STATUS_ASSIGNED:
                dto.setStatusDesc(SimCardStatusEnum.ASSIGNED.getDescription());
                break;
            case SimCardConstant.STATUS_ACTIVATED:
                dto.setStatusDesc(SimCardStatusEnum.ACTIVATED.getDescription());
                break;
            case SimCardConstant.STATUS_DEACTIVATED:
                dto.setStatusDesc(SimCardStatusEnum.DEACTIVATED.getDescription());
                break;
            case SimCardConstant.STATUS_RECYCLED:
                dto.setStatusDesc(SimCardStatusEnum.RECYCLED.getDescription());
                break;
            default:
                dto.setStatusDesc(SimCardStatusEnum.UNKNOWN.getDescription());
        }
        
        return dto;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatusByIccid(String iccid, Integer status) {
        if (StringUtils.isBlank(iccid) || status == null) {
            return false;
        }
        
        // 先获取原始信息用于后续操作
        SimCard simCard = getSimCardByIccid(iccid);
        if (simCard == null) {
            log.warn("SIM card not found for ICCID: {}", iccid);
            return false;
        }
        
        Integer oldStatus = simCard.getStatus();
        Long batchId = simCard.getBatchId();
        Long cardId = simCard.getId();
        
        // 直接通过ICCID更新状态，适用于分表环境
        LambdaUpdateWrapper<SimCard> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SimCard::getIccid, iccid)
                    .set(SimCard::getStatus, status)
                    .set(SimCard::getUpdateTime, new Date());
        
        boolean result = this.update(updateWrapper);
        
        if (result) {
            // 更新批次库存状态
            if (batchId != null) {
                simCardBatchStockService.updateStockByStatusChange(
                    batchId, oldStatus, status, 1);
            }
            
            // 记录操作日志
            SimCardOperationDTO operationDTO = new SimCardOperationDTO();
            operationDTO.setCardId(cardId);
            operationDTO.setIccid(iccid);
            operationDTO.setOperationType(SimCardConstant.OPERATION_UPDATE);
            operationDTO.setOldStatus(oldStatus);
            operationDTO.setNewStatus(status);
            operationDTO.setRemark("Update SIM card status by ICCID");
            simCardOperationService.addOperation(operationDTO);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchImportFromExcel(List<SimCard> simCards, Long batchId, Long operatorUserId) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        if (CollectionUtils.isEmpty(simCards)) {
            result.put("success", false);
            result.put("message", "Import data is empty");
            return result;
        }
        
        log.info("Starting batch import of {} SIM cards for batch {}", simCards.size(), batchId);
        
        // 获取批次信息
        SimCardBatch batch = simCardBatchService.getById(batchId);
        if (batch == null) {
            result.put("success", false);
            result.put("message", "Batch not found: " + batchId);
            return result;
        }
        
        // 批量获取全局序列ID
        List<Long> globalIds = sequenceService.getBatchSequenceValues("sim_card_id_seq", simCards.size());
        
        // 批量处理SIM卡
        for (int i = 0; i < simCards.size(); i++) {
            SimCard simCard = simCards.get(i);
            try {
                // 设置全局序列ID
                simCard.setId(globalIds.get(i));
                // 设置批次信息
                simCard.setBatchId(batchId);
                // SimCardBatch没有orgId字段，暂时不设置组织ID
                simCard.setCreateUserId(operatorUserId);
                simCard.setUpdateUserId(operatorUserId);
                simCard.setCreateTime(new Date());
                simCard.setUpdateTime(new Date());
                
                // 检查ICCID是否已存在
                SimCard existingCard = getSimCardByIccid(simCard.getIccid());
                if (existingCard != null) {
                    errorMessages.add("ICCID already exists: " + simCard.getIccid());
                    failCount++;
                    continue;
                }
                
                // 保存SIM卡
                boolean saved = save(simCard);
                if (saved) {
                    successCount++;
                    
                    // 记录操作日志
                    SimCardOperationDTO operationDTO = new SimCardOperationDTO();
                    operationDTO.setCardId(simCard.getId());
                    operationDTO.setIccid(simCard.getIccid());
                    operationDTO.setOperationType(SimCardConstant.OPERATION_TYPE_IMPORT);
                    operationDTO.setOperatorUserId(operatorUserId);
                    operationDTO.setNewStatus(simCard.getStatus());
                    operationDTO.setStockInOrgId(simCard.getOrganizationId());
                    operationDTO.setRemark("Import SIM card from Excel");
                    operationDTO.setResultStatus(SimCardConstant.RESULT_SUCCESS);
                    simCardOperationService.addOperation(operationDTO);
                } else {
                    errorMessages.add("Failed to save SIM card: " + simCard.getIccid());
                    failCount++;
                }
            } catch (Exception e) {
                log.error("Error importing SIM card: {}", simCard.getIccid(), e);
                errorMessages.add("Error importing " + simCard.getIccid() + ": " + e.getMessage());
                failCount++;
            }
        }
        
        // 更新批次库存统计
        if (successCount > 0) {
            try {
                simCardBatchStockService.updateStockAfterImport(batchId, successCount);
                log.info("Updated batch stock for batch {} with {} new cards", batchId, successCount);
            } catch (Exception e) {
                log.error("Failed to update batch stock for batch {}", batchId, e);
            }
        }
        
        log.info("Batch import completed. Success: {}, Failed: {}", successCount, failCount);
        
        result.put("success", failCount == 0);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", simCards.size());
        result.put("errorMessages", errorMessages);
        result.put("message", String.format("Import completed. Success: %d, Failed: %d", successCount, failCount));
        
        return result;
    }

    @Override
    public List<SimCard> queryForExport(Map<String, Object> params) {
        LambdaQueryWrapper<SimCard> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (params.get("iccid") != null) {
            wrapper.like(SimCard::getIccid, params.get("iccid").toString());
        }
        
        if (params.get("imsi") != null) {
            wrapper.like(SimCard::getImsi, params.get("imsi").toString());
        }
        
        if (params.get("status") != null) {
            wrapper.eq(SimCard::getStatus, Integer.parseInt(params.get("status").toString()));
        }
        
        if (params.get("batchId") != null) {
            wrapper.eq(SimCard::getBatchId, Long.parseLong(params.get("batchId").toString()));
        }
        
        if (params.get("orgId") != null) {
            wrapper.eq(SimCard::getOrganizationId, Long.parseLong(params.get("orgId").toString()));
        }
        
        if (params.get("cardTypeId") != null) {
            wrapper.eq(SimCard::getCardTypeId, Long.parseLong(params.get("cardTypeId").toString()));
        }
        
        if (params.get("supplierId") != null) {
            wrapper.eq(SimCard::getSupplierId, Long.parseLong(params.get("supplierId").toString()));
        }
        
        // 添加时间范围查询
        if (params.get("beginDate") != null) {
            wrapper.ge(SimCard::getCreateTime, params.get("beginDate"));
        }
        
        if (params.get("endDate") != null) {
            wrapper.le(SimCard::getCreateTime, params.get("endDate"));
        }
        
        // 按创建时间排序
        wrapper.orderByDesc(SimCard::getCreateTime);
        
        // 限制导出数量，防止数据过大
        Integer maxExportCount = 10000; // 最大导出1万条
        if (params.get("maxCount") != null) {
            maxExportCount = Integer.parseInt(params.get("maxCount").toString());
        }
        
        wrapper.last("LIMIT " + maxExportCount);
        
        List<SimCard> result = list(wrapper);
        log.info("Query for export completed, found {} SIM cards", result.size());
        
        return result;
    }

    @Override
    public List<SimCard> queryForExport(SimCardQuery queryParams) {
        LambdaQueryWrapper<SimCard> wrapper = new LambdaQueryWrapper<>();
        
        // Add query conditions
        if (StringUtils.isNotBlank(queryParams.getIccid())) {
            wrapper.like(SimCard::getIccid, queryParams.getIccid());
        }
        
        if (StringUtils.isNotBlank(queryParams.getImsi())) {
            wrapper.like(SimCard::getImsi, queryParams.getImsi());
        }
        
        if (queryParams.getStatus() != null) {
            wrapper.eq(SimCard::getStatus, queryParams.getStatus());
        }
        
        if (queryParams.getBatchId() != null) {
            wrapper.eq(SimCard::getBatchId, queryParams.getBatchId());
        }
        
        if (queryParams.getOrgId() != null) {
            wrapper.eq(SimCard::getOrganizationId, queryParams.getOrgId());
        }
        
        if (queryParams.getCardType() != null) {
            wrapper.eq(SimCard::getCardTypeId, queryParams.getCardType());
        }
        
        // Order by creation time
        wrapper.orderByDesc(SimCard::getCreateTime);
        
        // Limit export quantity to prevent excessive data
        Integer maxExportCount = 10000; // Maximum export 10,000 records
        wrapper.last("LIMIT " + maxExportCount);
        
        List<SimCard> result = list(wrapper);
        log.info("Query for export completed, found {} SIM cards", result.size());
        
        return result;
    }
}