package com.nsrs.framework.sharding;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
/**
 * MSISDN prefix sharding algorithm for number resource table
 * Shards directly by the first N digits (default 3 digits) of the number,
 * e.g., numbers with prefix 139 are assigned to number_resource_139 table
 * Supports prefix matching query optimization
 */
@Slf4j
@Component
public class MsisdnPrefixShardingAlgorithm implements PreciseShardingAlgorithm<String>, RangeShardingAlgorithm<String> {

    private final int defaultPrefixLen = 3;

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
        String number = shardingValue.getValue();

        // 处理号码为空或长度不足的情况
        if (number == null || number.length() < defaultPrefixLen) {
            log.warn("Number [{}] is empty or insufficient length (< {}), using default table", number, defaultPrefixLen);
            return availableTargetNames.iterator().next();
        }

        // 获取号码前缀
        String prefix = number.substring(0, defaultPrefixLen);

        // 构造目标表名
        String targetTable = "number_resource_" + prefix;

        // 检查目标表是否在可用表列表中
        for (String availableTable : availableTargetNames) {
            if (availableTable.equals(targetTable)) {
                log.debug("Number [{}] assigned to table [{}]", number, availableTable);
                return availableTable;
            }
        }

        // If no matching table found, use default table
        log.warn("No matching table found for number [{}] target table [{}], using default table", number, targetTable);
        return availableTargetNames.iterator().next();
    }

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
        Set<String> result = new LinkedHashSet<>();
        
        // 尝试从范围查询中提取前缀信息进行优化
        String prefix = extractPrefixFromRange(shardingValue);
        if (prefix != null) {
            // 如果前缀长度等于默认前缀长度（3位），直接匹配单个表
            if (prefix.length() == defaultPrefixLen) {
                String targetTable = "number_resource_" + prefix;
                
                // 如果目标表存在，只返回该表
                for (String availableTable : availableTargetNames) {
                    if (availableTable.equals(targetTable)) {
                        result.add(availableTable);
                        log.info("Range sharding optimized for exact prefix [{}], using table [{}]", prefix, availableTable);
                        return result;
                    }
                }
            } else if (prefix.length() < defaultPrefixLen) {
                // 如果前缀长度小于默认长度（如"17"），匹配所有以该前缀开头的表
                for (String availableTable : availableTargetNames) {
                    if (availableTable.startsWith("number_resource_" + prefix)) {
                        result.add(availableTable);
                    }
                }
                
                if (!result.isEmpty()) {
                    log.info("Range sharding optimized for short prefix [{}], using tables: {}", prefix, result);
                    return result;
                }
            } else {
                // 如果前缀长度大于默认长度（如"1772"），取前3位匹配对应的表
                String tablePrefix = prefix.substring(0, defaultPrefixLen);
                String targetTable = "number_resource_" + tablePrefix;
                
                // 如果目标表存在，只返回该表
                for (String availableTable : availableTargetNames) {
                    if (availableTable.equals(targetTable)) {
                        result.add(availableTable);
                        log.info("Range sharding optimized for long prefix [{}] -> table prefix [{}], using table [{}]", prefix, tablePrefix, availableTable);
                        return result;
                    }
                }
            }
        }
        
        // 如果无法优化，返回所有可用的分表以确保查询覆盖所有可能的数据
        log.info("Range sharding for column [{}], returning all available tables: {}", 
                 shardingValue.getColumnName(), availableTargetNames);
        
        result.addAll(availableTargetNames);
        return result;
    }
    
    /**
     * 从范围查询中提取前缀信息
     * 支持多种LIKE查询模式的优化，包括短前缀（如"17"）
     */
    private String extractPrefixFromRange(RangeShardingValue<String> shardingValue) {
        // 检查下界和上界
        if (shardingValue.getValueRange().hasLowerBound() && shardingValue.getValueRange().hasUpperBound()) {
            String lowerBound = shardingValue.getValueRange().lowerEndpoint();
            String upperBound = shardingValue.getValueRange().upperEndpoint();
            
            if (lowerBound != null && upperBound != null) {
                // 找到公共前缀
                String commonPrefix = findCommonPrefix(lowerBound, upperBound);
                if (commonPrefix != null && !commonPrefix.isEmpty()) {
                    // 验证这是一个有效的前缀查询模式
                    if (isValidPrefixQuery(lowerBound, upperBound, commonPrefix)) {
                        log.debug("Detected prefix query with common prefix: [{}]", commonPrefix);
                        return commonPrefix;
                    }
                }
            }
        }
        
        // 单边界检查（保持向后兼容）
        if (shardingValue.getValueRange().hasLowerBound()) {
            String lowerBound = shardingValue.getValueRange().lowerEndpoint();
            if (lowerBound != null && lowerBound.length() >= defaultPrefixLen) {
                return lowerBound.substring(0, defaultPrefixLen);
            }
        }
        
        if (shardingValue.getValueRange().hasUpperBound()) {
            String upperBound = shardingValue.getValueRange().upperEndpoint();
            if (upperBound != null && upperBound.length() >= defaultPrefixLen) {
                return upperBound.substring(0, defaultPrefixLen);
            }
        }
        
        return null;
    }
    
    /**
     * 找到两个字符串的公共前缀
     */
    private String findCommonPrefix(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return null;
        }
        
        int minLength = Math.min(str1.length(), str2.length());
        int i = 0;
        while (i < minLength && str1.charAt(i) == str2.charAt(i)) {
            i++;
        }
        
        return i > 0 ? str1.substring(0, i) : null;
    }
    
    /**
     * 验证是否为有效的前缀查询模式
     * 例如：lowerBound="17000000000", upperBound="17999999999", prefix="17"
     */
    private boolean isValidPrefixQuery(String lowerBound, String upperBound, String prefix) {
        if (prefix.length() >= lowerBound.length() || prefix.length() >= upperBound.length()) {
            return false;
        }
        
        // 检查下界是否为前缀+0填充
        String expectedLower = prefix + repeatChar('0', lowerBound.length() - prefix.length());
        
        // 检查上界是否为前缀+9填充
        String expectedUpper = prefix + repeatChar('9', upperBound.length() - prefix.length());
        
        return lowerBound.equals(expectedLower) && upperBound.equals(expectedUpper);
    }
    
    /**
     * 重复字符（Java 8兼容版本）
     */
    private String repeatChar(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

}