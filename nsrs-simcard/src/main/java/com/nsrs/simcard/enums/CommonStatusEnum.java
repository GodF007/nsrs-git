package com.nsrs.simcard.enums;

/**
 * Common Status Enumeration
 * Used for general enable/disable status
 */
public enum CommonStatusEnum {
    
    /**
     * Enabled/Active
     */
    ENABLED(1, "Enabled", "启用"),
    
    /**
     * Disabled/Inactive
     */
    DISABLED(0, "Disabled", "禁用");
    
    private final Integer code;
    private final String englishName;
    private final String chineseName;
    
    CommonStatusEnum(Integer code, String englishName, String chineseName) {
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
    public static CommonStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommonStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
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
     * Check if status is enabled
     */
    public static boolean isEnabled(Integer code) {
        return ENABLED.getCode().equals(code);
    }
    
    /**
     * Check if status is disabled
     */
    public static boolean isDisabled(Integer code) {
        return DISABLED.getCode().equals(code);
    }
    
    /**
     * Get Chinese name by code
     */
    public static String getChineseNameByCode(Integer code) {
        CommonStatusEnum status = getByCode(code);
        return status != null ? status.getChineseName() : "未知状态";
    }
    
    /**
     * Get English name by code
     */
    public static String getEnglishNameByCode(Integer code) {
        CommonStatusEnum status = getByCode(code);
        return status != null ? status.getEnglishName() : "Unknown Status";
    }
}