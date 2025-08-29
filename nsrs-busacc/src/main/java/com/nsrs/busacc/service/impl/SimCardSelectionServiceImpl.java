package com.nsrs.busacc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nsrs.busacc.config.SimCardSelectionProperties;
import com.nsrs.busacc.dto.SimCardSelectionRequest;
import com.nsrs.busacc.dto.SimCardSelectionResponse;
import com.nsrs.busacc.service.SimCardSelectionService;
import com.nsrs.busacc.utils.SimCardTableUtils;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.mapper.SimCardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * SIM卡选择服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimCardSelectionServiceImpl implements SimCardSelectionService {
    
    private final SimCardSelectionProperties properties;
    private final SimCardMapper simCardMapper;
    private final SimCardTableUtils tableUtils;
    
    @Override
    public SimCardSelectionResponse selectSimCards(SimCardSelectionRequest request) {
        SimCardSelectionResponse response = new SimCardSelectionResponse();
        
        try {
            // 验证请求参数
            if (request == null) {
                response.setSuccess(false);
                response.setMessage("Request cannot be null");
                response.setSimCards(new ArrayList<>());
                response.setTotalCount(0);
                return response;
            }
            
            Integer poolSize = request.getPoolSize();
            if (poolSize == null || poolSize <= 0) {
                poolSize = properties.getPoolSize();
            }
            
            List<SimCardSelectionResponse.SimCardInfo> simCards;
            
            if (StringUtils.isNotBlank(request.getIccidSuffix())) {
                // 根据ICCID后缀查询指定分表
                simCards = selectSimCardsBySuffix(request.getIccidSuffix(), poolSize, request);
                response.setIccidSuffix(request.getIccidSuffix());
            } else {
                // 随机获取SIM卡池
                simCards = getRandomSimCardPool(poolSize, request);
            }
            
            response.setSuccess(true);
            response.setSimCards(simCards);
            response.setTotalCount(simCards.size());
            response.setMessage(String.format("Successfully retrieved %d sim cards", simCards.size()));
            
            log.info("SimCard selection completed: {} sim cards retrieved", simCards.size());
            
        } catch (Exception e) {
            log.error("Failed to select sim cards", e);
            response.setSuccess(false);
            response.setMessage("Failed to select sim cards: " + e.getMessage());
            response.setSimCards(new ArrayList<>());
            response.setTotalCount(0);
        }
        
        return response;
    }
    
    @Override
    public SimCardSelectionResponse getRandomPool(SimCardSelectionRequest request) {
        SimCardSelectionResponse response = new SimCardSelectionResponse();
        
        try {
            Integer poolSize = request.getPoolSize();
            if (poolSize == null || poolSize <= 0) {
                poolSize = properties.getPoolSize();
            }
            
            // 调用优化后的随机SIM卡池获取方法
            List<SimCardSelectionResponse.SimCardInfo> simCards = getRandomSimCardPool(poolSize, request);
            
            // 随机打乱结果
            Collections.shuffle(simCards);
            
            response.setSuccess(true);
            response.setSimCards(simCards);
            response.setTotalCount(simCards.size());
            response.setMessage(String.format("Successfully retrieved %d sim cards from random pool", simCards.size()));
            
            log.info("Random sim card pool generated: {} sim cards for dataType: {}", simCards.size(), request.getDataType());
            
        } catch (Exception e) {
            log.error("Failed to get random sim card pool", e);
            response.setSuccess(false);
            response.setMessage("Failed to get random sim card pool: " + e.getMessage());
            response.setSimCards(new ArrayList<>());
            response.setTotalCount(0);
        }
        
        return response;
    }
    
    /**
     * 根据ICCID后缀查询指定分表的SIM卡
     */
    private List<SimCardSelectionResponse.SimCardInfo> selectSimCardsBySuffix(String iccidSuffix, Integer poolSize, SimCardSelectionRequest request) {
        // 计算表后缀
        String tableSuffix = tableUtils.calculateTableSuffix(iccidSuffix);
        String tableName = tableUtils.getTableNameBySuffix(tableSuffix);
        
        // 检查表是否存在
        if (!tableUtils.isTableExists(tableName)) {
            log.warn("Table {} does not exist for suffix {}", tableName, iccidSuffix);
            return new ArrayList<>();
        }
        
        // 构建查询条件
        QueryWrapper<SimCard> queryWrapper = new QueryWrapper<>();
        
        // ICCID后缀过滤 - 使用范围查询确保走分表路由
        // 分表策略：按ICCID后3位取模10，所以需要构造精确的范围查询
        String minIccid = generateMinIccidForSuffix(iccidSuffix);
        String maxIccid = generateMaxIccidForSuffix(iccidSuffix);
        queryWrapper.between("iccid", minIccid, maxIccid);
        
        // 添加状态过滤
        Integer[] statusFilter = request.getStatusFilter() != null ? request.getStatusFilter() : properties.getAllowedStatuses();
        if (statusFilter != null && statusFilter.length > 0) {
            queryWrapper.in("status", Arrays.asList(statusFilter));
        }
        
        // 添加数据类型过滤
        if (request.getDataType() != null) {
            queryWrapper.eq("data_type", request.getDataType());
        }
        
        // 添加供应商过滤
        if (request.getSupplierId() != null) {
            queryWrapper.eq("supplier_id", request.getSupplierId());
        }
        
        // 添加组织过滤
        if (request.getOrganizationId() != null) {
            queryWrapper.eq("org_id", request.getOrganizationId());
        }
        
        // 添加批次过滤
        if (request.getBatchId() != null) {
            queryWrapper.eq("batch_id", request.getBatchId());
        }
        
        // 随机排序并限制数量
        queryWrapper.orderByAsc("RAND()").last("LIMIT " + poolSize);
        
        log.info("Executing query for suffix {}: table {}", iccidSuffix, tableName);
        
        List<SimCard> simCards = simCardMapper.selectList(queryWrapper);
        return convertToSimCardInfoList(simCards);
    }
    
    /**
     * 随机获取SIM卡池 - 优化查询策略，按需查询表直到满足poolSize
     */
    private List<SimCardSelectionResponse.SimCardInfo> getRandomSimCardPool(Integer poolSize, SimCardSelectionRequest request) {
        List<SimCardSelectionResponse.SimCardInfo> result = new ArrayList<>();
        
        try {
            // 获取随机的表后缀列表
            List<String> randomSuffixes = tableUtils.getRandomSuffixes();
            Collections.shuffle(randomSuffixes); // 随机打乱顺序
            
            int queriedTables = 0;
            
            // 按需查询表，直到满足poolSize或查询完所有表
            for (String suffix : randomSuffixes) {
                if (result.size() >= poolSize) {
                    break;
                }
                
                // 计算本次查询需要的数量
                int needCount = poolSize - result.size();
                
                try {
                    List<SimCardSelectionResponse.SimCardInfo> simCards = selectSimCardsBySuffixInternal(suffix, needCount, request);
                    result.addAll(simCards);
                    
                    queriedTables++;
                    log.debug("Queried table with suffix {}: found {} sim cards, total: {}", 
                            suffix, simCards.size(), result.size());
                    
                } catch (Exception e) {
                    log.warn("Failed to query table for suffix {}", suffix, e);
                }
            }
            
            log.info("Random pool query completed: {} sim cards retrieved from {} tables", 
                    result.size(), queriedTables);
            
        } catch (Exception e) {
            log.error("Failed to get random sim card pool", e);
        }
        
        return result;
    }
    
    /**
     * 内部方法：根据表后缀查询SIM卡
     */
    private List<SimCardSelectionResponse.SimCardInfo> selectSimCardsBySuffixInternal(String suffix, Integer poolSize, SimCardSelectionRequest request) {
        String tableName = tableUtils.getTableNameBySuffix(suffix);
        
        // 检查表是否存在
        if (!tableUtils.isTableExists(tableName)) {
            log.warn("Table {} does not exist for suffix {}", tableName, suffix);
            return new ArrayList<>();
        }
        
        // 构建查询条件
        QueryWrapper<SimCard> queryWrapper = new QueryWrapper<>();
        
        // 添加状态过滤
        Integer[] statusFilter = request.getStatusFilter() != null ? request.getStatusFilter() : properties.getAllowedStatuses();
        if (statusFilter != null && statusFilter.length > 0) {
            queryWrapper.in("status", Arrays.asList(statusFilter));
        }
        
        // 添加数据类型过滤
        if (request.getDataType() != null) {
            queryWrapper.eq("data_type", request.getDataType());
        }
        
        // 添加供应商过滤
        if (request.getSupplierId() != null) {
            queryWrapper.eq("supplier_id", request.getSupplierId());
        }
        
        // 添加组织过滤
        if (request.getOrganizationId() != null) {
            queryWrapper.eq("org_id", request.getOrganizationId());
        }
        
        // 添加批次过滤
        if (request.getBatchId() != null) {
            queryWrapper.eq("batch_id", request.getBatchId());
        }
        
        // 随机排序并限制数量
        queryWrapper.orderByAsc("RAND()").last("LIMIT " + poolSize);
        
        List<SimCard> simCards = simCardMapper.selectList(queryWrapper);
        return convertToSimCardInfoList(simCards);
    }
    
    /**
     * 转换SimCard实体为SimCardInfo DTO
     */
    private List<SimCardSelectionResponse.SimCardInfo> convertToSimCardInfoList(List<SimCard> simCards) {
        List<SimCardSelectionResponse.SimCardInfo> result = new ArrayList<>();
        
        for (SimCard simCard : simCards) {
            SimCardSelectionResponse.SimCardInfo info = new SimCardSelectionResponse.SimCardInfo();
            info.setCardId(simCard.getId());
            info.setIccid(simCard.getIccid());
            info.setImsi(simCard.getImsi());
            info.setDataType(simCard.getDataType());
            info.setStatus(simCard.getStatus());
            info.setBatchId(simCard.getBatchId());
            info.setSupplierId(simCard.getSupplierId());
            info.setOrganizationId(simCard.getOrganizationId());
            info.setCreateTime(simCard.getCreateTime());
            info.setRemark(simCard.getRemark());
            result.add(info);
        }
        
        return result;
    }
    
    /**
     * 根据ICCID后缀生成最小ICCID值
     * 用于范围查询的下边界
     * 
     * @param iccidSuffix ICCID后缀
     * @return 最小ICCID值
     */
    private String generateMinIccidForSuffix(String iccidSuffix) {
        if (StringUtils.isBlank(iccidSuffix)) {
            return "";
        }
        
        // 构造以指定后缀结尾的最小ICCID
        // 例如：后缀为"123"，则生成"000000000000000123"
        StringBuilder minIccid = new StringBuilder();
        
        // ICCID通常为19-20位，这里假设20位
        int totalLength = 20;
        int suffixLength = iccidSuffix.length();
        
        // 前面补0
        for (int i = 0; i < totalLength - suffixLength; i++) {
            minIccid.append("0");
        }
        minIccid.append(iccidSuffix);
        
        return minIccid.toString();
    }
    
    /**
     * 根据ICCID后缀生成最大ICCID值
     * 用于范围查询的上边界
     * 
     * @param iccidSuffix ICCID后缀
     * @return 最大ICCID值
     */
    private String generateMaxIccidForSuffix(String iccidSuffix) {
        if (StringUtils.isBlank(iccidSuffix)) {
            return "zzzzzzzzzzzzzzzzzzzz"; // 返回一个很大的值
        }
        
        // 构造以指定后缀结尾的最大ICCID
        // 例如：后缀为"123"，则生成"999999999999999123"
        StringBuilder maxIccid = new StringBuilder();
        
        // ICCID通常为19-20位，这里假设20位
        int totalLength = 20;
        int suffixLength = iccidSuffix.length();
        
        // 前面补9
        for (int i = 0; i < totalLength - suffixLength; i++) {
            maxIccid.append("9");
        }
        maxIccid.append(iccidSuffix);
        
        return maxIccid.toString();
    }
}