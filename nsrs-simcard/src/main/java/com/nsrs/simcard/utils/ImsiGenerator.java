package com.nsrs.simcard.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * IMSI生成工具类
 */
public class ImsiGenerator {
    
    /**
     * 生成下一个IMSI号码
     *
     * @param currentImsi 当前IMSI号码
     * @return 下一个IMSI号码
     */
    public static String generateNextImsi(String currentImsi) {
        if (StringUtils.isBlank(currentImsi)) {
            return null;
        }
        
        // 找到数字部分的起始位置
        int digitStartPos = 0;
        for (int i = 0; i < currentImsi.length(); i++) {
            if (Character.isDigit(currentImsi.charAt(i))) {
                digitStartPos = i;
                break;
            }
        }
        
        // 如果没有数字部分，则无法生成下一个IMSI
        if (digitStartPos >= currentImsi.length()) {
            return null;
        }
        
        // 分离前缀和数字部分
        String prefix = currentImsi.substring(0, digitStartPos);
        String digits = currentImsi.substring(digitStartPos);
        
        // 数字部分加1
        long nextDigit = Long.parseLong(digits) + 1;
        
        // 构建下一个IMSI，保持数字部分的长度
        return prefix + StringUtils.leftPad(String.valueOf(nextDigit), digits.length(), '0');
    }
    
    /**
     * 验证IMSI格式
     *
     * @param imsi IMSI号码
     * @return 是否有效
     */
    public static boolean isValidImsi(String imsi) {
        if (StringUtils.isBlank(imsi)) {
            return false;
        }
        
        // IMSI号码一般为15位数字，但根据不同国家和运营商可能有所不同
        // 这里简单验证是否包含数字
        return imsi.matches(".*\\d+.*");
    }
    
    /**
     * 获取IMSI的分表索引
     *
     * @param imsi IMSI号码
     * @param tableShardsCount 分表数量
     * @return 分表索引
     */
    public static int getTableShardingIndex(String imsi, int tableShardsCount) {
        if (StringUtils.isBlank(imsi) || tableShardsCount <= 0) {
            return 0;
        }
        
        // 根据IMSI的最后一位或两位决定分表索引
        String lastDigit = imsi.substring(imsi.length() - 1);
        return Integer.parseInt(lastDigit) % tableShardsCount;
    }
    
    /**
     * 比较两个IMSI号码的大小
     *
     * @param imsi1 第一个IMSI号码
     * @param imsi2 第二个IMSI号码
     * @return 比较结果：小于0表示imsi1小于imsi2，等于0表示相等，大于0表示imsi1大于imsi2
     */
    public static int compareImsi(String imsi1, String imsi2) {
        if (imsi1 == null && imsi2 == null) {
            return 0;
        }
        
        if (imsi1 == null) {
            return -1;
        }
        
        if (imsi2 == null) {
            return 1;
        }
        
        // 如果格式不同，先尝试去除所有非数字字符后比较
        String digitImsi1 = imsi1.replaceAll("\\D", "");
        String digitImsi2 = imsi2.replaceAll("\\D", "");
        
        if (digitImsi1.length() != digitImsi2.length()) {
            return digitImsi1.length() - digitImsi2.length();
        }
        
        return digitImsi1.compareTo(digitImsi2);
    }
} 