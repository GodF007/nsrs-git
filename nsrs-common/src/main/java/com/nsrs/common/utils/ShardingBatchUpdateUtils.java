package com.nsrs.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分表批量操作工具类
 * 解决MyBatis-Plus在分表环境下批量操作性能问题
 */
@Slf4j
@Component
public class ShardingBatchUpdateUtils {

    /**
     * 按分表键分组数据
     *
     * @param dataList 数据列表
     * @param shardingKeyExtractor 分表键提取函数
     * @param shardingStrategy 分表策略函数
     * @param <T> 数据类型
     * @return 按分表分组的数据Map，key为分表名，value为该分表的数据列表
     *
     *  Map<String, List<String>> shardingGroups = shardingUtils.groupByShardingTable(
     *             imsiList,
     *             imsi -> imsi,
     *             shardingUtils::getImsiResourceTableName
     *         );
     */
    public <T> Map<String, List<T>> groupByShardingTable(List<T> dataList,
                                                          Function<T, String> shardingKeyExtractor,
                                                          Function<String, String> shardingStrategy) {
        if (dataList == null || dataList.isEmpty()) {
            return new HashMap<>();
        }

        return dataList.stream()
                .collect(Collectors.groupingBy(
                        data -> {
                            String shardingKey = shardingKeyExtractor.apply(data);
                            return shardingStrategy.apply(shardingKey);
                        }
                ));
    }

    /**
     * 号码资源分表策略
     * 根据号码前3位确定分表名
     */
    public String getNumberResourceTableName(String number) {
        if (number == null || number.length() < 3) {
            throw new IllegalArgumentException("Invalid number for sharding: " + number);
        }
        String prefix = number.substring(0, 3);
        return "number_resource_" + prefix;
    }

    /**
     * IMSI资源分表策略
     * 根据IMSI后2位取模确定分表名
     */
    public String getImsiResourceTableName(String imsi) {
        if (imsi == null || imsi.length() < 2) {
            throw new IllegalArgumentException("Invalid IMSI for sharding: " + imsi);
        }
        String suffix = imsi.substring(imsi.length() - 2);
        int tableIndex = Integer.parseInt(suffix) % 10;
        return "imsi_resource_" + tableIndex;
    }

    /**
     * SIM卡分表策略
     * 根据ICCID后3位取模确定分表名
     */
    public String getSimCardTableName(String iccid) {
        if (iccid == null || iccid.length() < 3) {
            throw new IllegalArgumentException("Invalid ICCID for sharding: " + iccid);
        }
        String suffix = iccid.substring(iccid.length() - 3);
        int tableIndex = Integer.parseInt(suffix) % 10;
        return "sim_card_" + tableIndex;
    }

    /**
     * 绑定关系分表策略
     * 根据号码前3位确定分表名
     */
    public String getNumberImsiBindingTableName(String number) {
        if (number == null || number.length() < 3) {
            throw new IllegalArgumentException("Invalid number for sharding: " + number);
        }
        String prefix = number.substring(0, 3);
        return "number_imsi_binding_" + prefix;
    }

    /**
     * 生成批量UPDATE SQL语句
     *
     * @param tableName 表名
     * @param updateFields 要更新的字段列表
     * @param whereField WHERE条件字段
     * @param dataSize 数据条数
     * @return 批量UPDATE SQL
     */
    public String generateBatchUpdateSql(String tableName, List<String> updateFields, String whereField, int dataSize) {
        StringBuilder sql = new StringBuilder();
        
        for (int i = 0; i < dataSize; i++) {
            if (i > 0) {
                sql.append("; ");
            }
            
            sql.append("UPDATE ").append(tableName).append(" SET ");
            
            for (int j = 0; j < updateFields.size(); j++) {
                if (j > 0) {
                    sql.append(", ");
                }
                sql.append(updateFields.get(j)).append(" = ?");
            }
            
            sql.append(" WHERE ").append(whereField).append(" = ?");
        }
        
        return sql.toString();
    }

    /**
     * 生成批量UPDATE SQL语句（使用CASE WHEN）
     * 这种方式在某些情况下性能更好
     *
     * @param tableName 表名
     * @param updateFields 要更新的字段列表
     * @param whereField WHERE条件字段
     * @param dataSize 数据条数
     * @return 批量UPDATE SQL
     */
    public String generateBatchUpdateSqlWithCaseWhen(String tableName, List<String> updateFields, String whereField, int dataSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName).append(" SET ");
        
        for (int i = 0; i < updateFields.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            
            String field = updateFields.get(i);
            sql.append(field).append(" = CASE ").append(whereField);
            
            for (int j = 0; j < dataSize; j++) {
                sql.append(" WHEN ? THEN ?");
            }
            
            sql.append(" ELSE ").append(field).append(" END");
        }
        
        sql.append(" WHERE ").append(whereField).append(" IN (");
        for (int i = 0; i < dataSize; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");
        
        return sql.toString();
    }

    /**
     * 记录分表批量操作日志
     */
    public void logShardingBatchOperation(String operation, Map<String, Integer> tableDataCounts, long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        int totalCount = tableDataCounts.values().stream().mapToInt(Integer::intValue).sum();
        
        log.info("Sharding batch {} completed: {} records across {} tables in {}ms", 
                operation, totalCount, tableDataCounts.size(), duration);
        
        if (log.isDebugEnabled()) {
            tableDataCounts.forEach((table, count) -> 
                log.debug("  - {}: {} records", table, count));
        }
    }

    /**
     * 验证分表键的有效性
     */
    public void validateShardingKey(String shardingKey, String keyType) {
        if (shardingKey == null || shardingKey.trim().isEmpty()) {
            throw new IllegalArgumentException(keyType + " cannot be null or empty for sharding");
        }
    }

    /**
     * 获取支持的号码前缀列表
     */
    public static final List<String> SUPPORTED_NUMBER_PREFIXES = Arrays.asList(
            "139", "177", "138", "136", "135", "134", "150", "151", "152", "153",
            "155", "156", "157", "158", "159", "180", "181", "182", "183", "184",
            "185", "186", "187", "188", "189"
    );

    /**
     * 验证号码前缀是否支持
     */
    public boolean isNumberPrefixSupported(String number) {
        if (number == null || number.length() < 3) {
            return false;
        }
        String prefix = number.substring(0, 3);
        return SUPPORTED_NUMBER_PREFIXES.contains(prefix);
    }
}