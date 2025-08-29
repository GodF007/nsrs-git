package com.nsrs.busacc.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SIM卡分表工具类
 * 用于管理和查询按ICCID分表的SIM卡资源
 * 分表策略：按ICCID后3位取模10进行分表
 */
@Component
public class SimCardTableUtils {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    /**
     * 分表后缀Map，key为表后缀，value为对应的表名
     */
    private static final Map<String, String> SUFFIX_TABLE_MAP = new ConcurrentHashMap<>();
    
    /**
     * 默认表名
     */
    private static final String DEFAULT_TABLE = "sim_card";

    /**
     * 初始化表后缀信息
     */
    @PostConstruct
    public void init() {
        // 从配置中解析表后缀
        List<String> suffixes = getTableSuffixesFromConfig();
        
        for (String suffix : suffixes) {
            String tableName = DEFAULT_TABLE + "_" + suffix;
            SUFFIX_TABLE_MAP.put(suffix, tableName);
        }
    }

    /**
     * 从配置中获取表后缀列表
     */
    private List<String> getTableSuffixesFromConfig() {
        String actualDataNodes = environment.getProperty("spring.shardingsphere.sharding.tables.sim_card.actual-data-nodes");
        if (actualDataNodes == null || actualDataNodes.isEmpty()) {
            // 如果配置读取失败，使用默认值（0-9）
            return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        }
        
        // 解析配置字符串，提取表后缀
        // 格式：ds0.sim_card_${0..9}
        if (actualDataNodes.contains("${0..9}")) {
            return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        }
        
        // 如果是其他格式，尝试解析
        return Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    /**
     * 根据ICCID获取分表名
     * 分表策略：按ICCID后3位取模10
     *
     * @param iccid ICCID
     * @return 表名
     */
    public String getTableNameByIccid(String iccid) {
        if (StringUtils.isBlank(iccid) || iccid.length() < 3) {
            return DEFAULT_TABLE + "_0"; // 默认返回第一个表
        }
        
        // 获取ICCID后3位并取模10
        String last3Digits = iccid.substring(iccid.length() - 3);
        int suffix = Integer.parseInt(last3Digits) % 10;
        
        return SUFFIX_TABLE_MAP.getOrDefault(String.valueOf(suffix), DEFAULT_TABLE + "_0");
    }

    /**
     * 根据表后缀获取表名
     *
     * @param suffix 表后缀
     * @return 表名
     */
    public String getTableNameBySuffix(String suffix) {
        return SUFFIX_TABLE_MAP.getOrDefault(suffix, DEFAULT_TABLE + "_0");
    }

    /**
     * 获取所有分表名
     *
     * @return 所有分表名列表
     */
    public List<String> getAllTableNames() {
        return new ArrayList<>(SUFFIX_TABLE_MAP.values());
    }

    /**
     * 获取所有表后缀
     *
     * @return 所有表后缀集合
     */
    public Set<String> getAllSuffixes() {
        return new HashSet<>(SUFFIX_TABLE_MAP.keySet());
    }

    /**
     * 获取随机表后缀列表（用于随机查询）
     *
     * @return 随机排序的表后缀列表
     */
    public List<String> getRandomSuffixes() {
        List<String> suffixes = new ArrayList<>(SUFFIX_TABLE_MAP.keySet());
        Collections.shuffle(suffixes);
        return suffixes;
    }

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 是否存在
     */
    public boolean isTableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = DATABASE()";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
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

    /**
     * 根据ICCID后缀计算表后缀
     *
     * @param iccidSuffix ICCID后缀
     * @return 表后缀
     */
    public String calculateTableSuffix(String iccidSuffix) {
        if (StringUtils.isBlank(iccidSuffix)) {
            return "0";
        }
        
        try {
            int suffix = Integer.parseInt(iccidSuffix) % 10;
            return String.valueOf(suffix);
        } catch (NumberFormatException e) {
            return "0";
        }
    }
}