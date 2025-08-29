package com.nsrs.simcard.enums;

/**
 * SIM Card Type Enumeration
 */
public enum SimCardTypeEnum {
    
    /**
     * Standard SIM
     */
    STANDARD(1, "Standard SIM", "标准SIM卡"),
    
    /**
     * Micro SIM
     */
    MICRO(2, "Micro SIM", "Micro SIM卡"),
    
    /**
     * Nano SIM
     */
    NANO(3, "Nano SIM", "Nano SIM卡"),
    
    /**
     * eSIM
     */
    ESIM(4, "eSIM", "嵌入式SIM卡"),
    
    /**
     * Test SIM
     */
    TEST(5, "Test SIM", "测试SIM卡");
    
    private final Integer code;
    private final String englishName;
    private final String chineseName;
    
    SimCardTypeEnum(Integer code, String englishName, String chineseName) {
        this.code = code;
        this.englishName = englishName;
        this.chineseName = chineseName;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    /**
     * Get enum by code
     */
    public static SimCardTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SimCardTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Check if code is valid
     */
    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }
    
    /**
     * Get Chinese name by code
     */
    public static String getChineseNameByCode(Integer code) {
        SimCardTypeEnum type = getByCode(code);
        return type != null ? type.getChineseName() : "未知类型";
    }
    
    /**
     * Get English name by code
     */
    public static String getEnglishNameByCode(Integer code) {
        SimCardTypeEnum type = getByCode(code);
        return type != null ? type.getEnglishName() : "Unknown Type";
    }
}