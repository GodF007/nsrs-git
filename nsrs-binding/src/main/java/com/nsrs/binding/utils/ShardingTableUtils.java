package com.nsrs.binding.utils;

import org.springframework.util.StringUtils;

/**
 * 分表工具类
 * 用于处理绑定关系表的分表逻辑
 */
public class ShardingTableUtils {
    
    /**
     * 基础表名
     */
    private static final String BASE_TABLE_NAME = "number_imsi_binding";
    
    /**
     * 根据号码获取分表名
     * 分表策略：按号码前3位分表
     * 
     * @param number 号码
     * @return 分表名
     */
    public static String getTableNameByNumber(String number) {
        if (!StringUtils.hasText(number) || number.length() < 3) {
            throw new IllegalArgumentException("Number must be at least 3 digits long");
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
    public static String getTableNameByPrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            throw new IllegalArgumentException("Prefix cannot be empty");
        }
        
        return BASE_TABLE_NAME + "_" + prefix;
    }
    
    /**
     * 获取基础表名
     * 
     * @return 基础表名
     */
    public static String getBaseTableName() {
        return BASE_TABLE_NAME;
    }
}