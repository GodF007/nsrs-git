package com.nsrs.busacc.service.impl;

import com.nsrs.busacc.config.NumberSelectionProperties;
import com.nsrs.busacc.dto.NumberSelectionRequest;
import com.nsrs.busacc.dto.NumberSelectionResponse;
import com.nsrs.busacc.service.NumberSelectionService;
import com.nsrs.busacc.utils.NumberResourceTableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 号码选择服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NumberSelectionServiceImpl implements NumberSelectionService {
    
    private final NumberSelectionProperties properties;
    private final JdbcTemplate jdbcTemplate;
    private final NumberResourceTableUtils tableUtils;
    
    @Override
    public NumberSelectionResponse selectNumbers(NumberSelectionRequest request) {
        NumberSelectionResponse response = new NumberSelectionResponse();
        
        try {
            // 验证请求参数
            if (request == null) {
                response.setSuccess(false);
                response.setMessage("Request cannot be null");
                response.setNumbers(new ArrayList<>());
                response.setTotalCount(0);
                return response;
            }
            
            Integer poolSize = request.getPoolSize();
            if (poolSize == null || poolSize <= 0) {
                poolSize = properties.getPoolSize();
            }
            
            List<NumberSelectionResponse.NumberInfo> numbers;
            
            if (StringUtils.hasText(request.getNumberPrefix())) {
                // 根据号段前缀查询
                numbers = selectNumbersByPrefix(request.getNumberPrefix(), poolSize, request);
            } else {
                // 随机获取号码池
                numbers = getRandomNumberPool(poolSize, request);
            }
            
            response.setSuccess(true);
            response.setNumbers(numbers);
            response.setTotalCount(numbers.size());
            response.setMessage(String.format("Successfully retrieved %d numbers", numbers.size()));
            
            log.info("Number selection completed: {} numbers retrieved", numbers.size());
            
        } catch (Exception e) {
            log.error("Failed to select numbers", e);
            response.setSuccess(false);
            response.setMessage("Failed to select numbers: " + e.getMessage());
            response.setNumbers(new ArrayList<>());
            response.setTotalCount(0);
        }
        
        return response;
    }
    

    
    @Override
    public NumberSelectionResponse getRandomPool(NumberSelectionRequest request) {
        NumberSelectionResponse response = new NumberSelectionResponse();
        
        try {
            Integer poolSize = request.getPoolSize();
            if (poolSize == null || poolSize <= 0) {
                poolSize = properties.getPoolSize();
            }
            
            // 调用优化后的随机号码池获取方法
            List<NumberSelectionResponse.NumberInfo> numbers = getRandomNumberPool(poolSize, request);
            
            // 随机打乱结果
            Collections.shuffle(numbers);
            
            response.setSuccess(true);
            response.setNumbers(numbers);
            response.setTotalCount(numbers.size());
            response.setMessage(String.format("Successfully retrieved %d numbers from random pool", numbers.size()));
            
            log.info("Random number pool generated: {} numbers for numberType: {}", numbers.size(), request.getNumberType());
            
        } catch (Exception e) {
            log.error("Failed to get random number pool", e);
            response.setSuccess(false);
            response.setMessage("Failed to get random number pool: " + e.getMessage());
            response.setNumbers(new ArrayList<>());
            response.setTotalCount(0);
        }
        
        return response;
    }
    
    /**
     * 根据号段前缀查询号码
     */
    private List<NumberSelectionResponse.NumberInfo> selectNumbersByPrefix(String prefix, Integer poolSize, NumberSelectionRequest request) {
        // 使用工具类获取表名
        String tableName = tableUtils.getTableNameByPrefix(prefix);
        
        // 检查表是否存在
        if (!tableUtils.isTableExists(tableName)) {
            log.warn("Table {} does not exist for prefix {}", tableName, prefix);
            return new ArrayList<>();
        }
        
        // 构建查询SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT number_id, number, number_type, status, charge, segment_id, level_id ");
        sql.append("FROM ").append(tableName).append(" ");
        sql.append("WHERE number LIKE ? ");
        
        List<Object> params = new ArrayList<>();
        params.add(prefix + "%");
        
        // 添加状态过滤
        Integer[] statusFilter = request.getStatusFilter() != null ? request.getStatusFilter() : properties.getAllowedStatuses();
        if (statusFilter != null && statusFilter.length > 0) {
            sql.append("AND status IN (");
            for (int i = 0; i < statusFilter.length; i++) {
                if (i > 0) sql.append(",");
                sql.append("?");
                params.add(statusFilter[i]);
            }
            sql.append(") ");
        }
        
        // 添加号码类型过滤
        if (request.getNumberType() != null) {
            sql.append("AND number_type = ? ");
            params.add(request.getNumberType());
        }
        
        sql.append("ORDER BY RAND() LIMIT ?");
        params.add(poolSize);
        
        log.info("Executing query for prefix {}: {}", prefix, sql.toString());
        
        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            NumberSelectionResponse.NumberInfo info = new NumberSelectionResponse.NumberInfo();
            info.setNumberId(rs.getLong("number_id"));
            info.setNumber(rs.getString("number"));
            info.setNumberType(rs.getInt("number_type"));
            info.setStatus(rs.getInt("status"));
            info.setCharge(rs.getBigDecimal("charge"));
            info.setSegmentId(rs.getLong("segment_id"));
            info.setLevelId(rs.getLong("level_id"));
            return info;
        });
    }
    
    /**
     * 随机获取号码池 - 优化查询策略，按需查询表直到满足poolSize
     */
    private List<NumberSelectionResponse.NumberInfo> getRandomNumberPool(Integer poolSize, NumberSelectionRequest request) {
        List<NumberSelectionResponse.NumberInfo> result = new ArrayList<>();
        
        try {
            // 获取随机的表前缀列表
            List<String> randomPrefixes = tableUtils.getRandomPrefixes();
            Collections.shuffle(randomPrefixes); // 随机打乱顺序
            
            int queriedTables = 0;
            
            // 按需查询表，直到满足poolSize或查询完所有表
            for (String prefix : randomPrefixes) {
                if (result.size() >= poolSize) {
                    break;
                }
                
                // 计算本次查询需要的数量
                int needCount = poolSize - result.size();
                
                try {
                    List<NumberSelectionResponse.NumberInfo> numbers = selectNumbersByPrefix(prefix, needCount, request);
                    result.addAll(numbers);
                    
                    queriedTables++;
                    log.debug("Queried table with prefix {}: found {} numbers, total: {}", 
                            prefix, numbers.size(), result.size());
                    
                } catch (Exception e) {
                    log.warn("Failed to query table for prefix {}", prefix, e);
                }
            }
            
            log.info("Random pool query completed: {} numbers retrieved from {} tables", 
                    result.size(), queriedTables);
            
        } catch (Exception e) {
            log.error("Failed to get random number pool", e);
        }
        
        return result;
    }
    

    

}