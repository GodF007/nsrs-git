package com.nsrs.busacc.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 号码资源分表工具类
 * 用于管理和查询按号码前缀分表的资源
 */
@Slf4j
@Component
public class NumberResourceTableUtils {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Spring环境变量，用于读取配置
     */
    @Autowired
    private Environment environment;

    /**
     * 分表前缀Map，key为号码前缀，value为对应的表名
     */
    private static final Map<String, String> PREFIX_TABLE_MAP = new ConcurrentHashMap<>();
    
    /**
     * 默认表名
     */
    private static final String DEFAULT_TABLE = "number_resource";

    /**
     * 初始化表前缀信息
     */
    @PostConstruct
    public void init() {
        // 从配置中解析表前缀
        List<String> prefixes = getTablePrefixesFromConfig();
        
        for (String prefix : prefixes) {
            String tableName = DEFAULT_TABLE + "_" + prefix;
            PREFIX_TABLE_MAP.put(prefix, tableName);
        }
    }

    /**
     * 从配置中获取表前缀列表
     */
    private List<String> getTablePrefixesFromConfig() {
        String actualDataNodes = environment.getProperty("spring.shardingsphere.sharding.tables.number_resource.actual-data-nodes", "");
        if (actualDataNodes == null || actualDataNodes.isEmpty()) {
            // 如果配置读取失败，使用默认值
            return Arrays.asList(
                "139", "177", "138", "136", "135", "134", 
                "150", "151", "152", "153", "155", "156", 
                "157", "158", "159", "180", "181", "182", 
                "183", "184", "185", "186", "187", "188", "189"
            );
        }
        
        try {
            // 解析配置字符串，提取表前缀
            // 支持多种格式：
            // 1. ds0.number_resource_${['139','177',...]} (ShardingSphere数组语法)
            // 2. ds0.number_resource_139,ds0.number_resource_177,... (逗号分隔)
            // 3. 其他可能的格式
            
            if (actualDataNodes.contains("['")) {
                // 处理ShardingSphere数组语法：ds0.number_resource_${['139','177',...]} 
                int startIndex = actualDataNodes.indexOf("['") + 2;
                int endIndex = actualDataNodes.lastIndexOf("']");
                if (startIndex > 1 && endIndex > startIndex) {
                    String prefixPart = actualDataNodes.substring(startIndex, endIndex);
                    return Arrays.stream(prefixPart.split("','"))
                            .map(String::trim)
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList());
                }
            } else if (actualDataNodes.contains(",")) {
                // 处理逗号分隔格式：ds0.number_resource_139,ds0.number_resource_177,...
                return Arrays.stream(actualDataNodes.split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .map(tableName -> {
                            // 提取表名后缀作为前缀
                            int lastUnderscoreIndex = tableName.lastIndexOf("_");
                            if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < tableName.length() - 1) {
                                return tableName.substring(lastUnderscoreIndex + 1);
                            }
                            return null;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            } else {
                // 单个表名格式，尝试提取后缀
                int lastUnderscoreIndex = actualDataNodes.lastIndexOf("_");
                if (lastUnderscoreIndex > 0 && lastUnderscoreIndex < actualDataNodes.length() - 1) {
                    String suffix = actualDataNodes.substring(lastUnderscoreIndex + 1);
                    return Arrays.asList(suffix);
                }
                // 如果无法提取后缀，返回空列表
                return new ArrayList<>();
            }
        } catch (Exception e) {
            // 解析失败时记录日志并使用默认值
            log.error("Failed to parse actualDataNodes configuration: {}, error: {}", actualDataNodes, e.getMessage());
        }
        
        // 解析失败时使用默认值
        return Arrays.asList(
            "139", "177", "138", "136", "135", "134", 
            "150", "151", "152", "153", "155", "156", 
            "157", "158", "159", "180", "181", "182", 
            "183", "184", "185", "186", "187", "188", "189"
        );
    }

    /**
     * 根据号码获取分表名
     * 分表策略：按号码前3位分表
     *
     * @param number 号码
     * @return 分表名
     */
    public String getTableNameByNumber(String number) {
        if (StringUtils.isBlank(number) || number.length() < 3) {
            return DEFAULT_TABLE;
        }
        
        String prefix = number.substring(0, 3);
        return getTableNameByPrefix(prefix);
    }

    /**
     * 根据号码前缀获取分表名
     *
     * @param prefix 号码前缀（3位）
     * @return 分表名
     */
    public String getTableNameByPrefix(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return DEFAULT_TABLE;
        }
        return PREFIX_TABLE_MAP.getOrDefault(prefix, DEFAULT_TABLE);
    }

    /**
     * 获取所有分表表名
     *
     * @return 表名列表
     */
    public List<String> getAllTableNames() {
        List<String> tableNames = new ArrayList<>(PREFIX_TABLE_MAP.values());
        if (!tableNames.contains(DEFAULT_TABLE)) {
            tableNames.add(DEFAULT_TABLE);
        }
        return tableNames;
    }

    /**
     * 获取当前所有的号码前缀
     * 
     * @return 号码前缀集合
     */
    public Set<String> getAllPrefixes() {
        return new HashSet<>(PREFIX_TABLE_MAP.keySet());
    }

    /**
     * 获取随机的表前缀列表（用于随机号码池查询）
     * 
     * @return 随机排序的前缀列表
     */
    public List<String> getRandomPrefixes() {
        List<String> prefixes = new ArrayList<>(PREFIX_TABLE_MAP.keySet());
        Collections.shuffle(prefixes);
        return prefixes;
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 是否存在
     */
    public boolean isTableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
            int count = jdbcTemplate.queryForObject(sql, Integer.class, tableName.toUpperCase());
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取基础表名
     * 
     * @return 基础表名
     */
    public String getBaseTableName() {
        return DEFAULT_TABLE;
    }
}