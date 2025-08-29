package com.nsrs.simcard.enums;

/**
 * Batch Status Enumeration
 */
public enum BatchStatusEnum {
    
    /**
     * Normal/Active
     */
    NORMAL(0, "Normal", "正常"),
    
    /**
     * Disabled/Inactive
     */
    DISABLED(1, "Disabled", "停用"),
    
    /**
     * Draft
     */
    DRAFT(2, "Draft", "草稿"),
    
    /**
     * Processing
     */
    PROCESSING(3, "Processing", "处理中"),
    
    /**
     * Completed
     */
    COMPLETED(4, "Completed", "已完成"),
    
    /**
     * Failed
     */
    FAILED(5, "Failed", "失败");
    
    private final Integer code;
    private final String englishName;
    private final String chineseName;
    
    BatchStatusEnum(Integer code, String englishName, String chineseName) {
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
    public static BatchStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BatchStatusEnum status : values()) {
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
     * Check if status is active
     */
    public static boolean isActive(Integer code) {
        return NORMAL.getCode().equals(code);
    }
    
    /**
     * Get Chinese name by code
     */
    public static String getChineseNameByCode(Integer code) {
        BatchStatusEnum status = getByCode(code);
        return status != null ? status.getChineseName() : "未知状态";
    }
    
    /**
     * Get English name by code
     */
    public static String getEnglishNameByCode(Integer code) {
        BatchStatusEnum status = getByCode(code);
        return status != null ? status.getEnglishName() : "Unknown Status";
    }
}