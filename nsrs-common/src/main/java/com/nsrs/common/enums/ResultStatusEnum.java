package com.nsrs.common.enums;

/**
 * 操作结果状态枚举
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum ResultStatusEnum {
    
    /**
     * 失败
     */
    FAILED(0, "失败", "Failed"),
    
    /**
     * 成功
     */
    SUCCESS(1, "成功", "Success");
    
    private final Integer code;
    private final String chineseName;
    private final String englishName;
    
    ResultStatusEnum(Integer code, String chineseName, String englishName) {
        this.code = code;
        this.chineseName = chineseName;
        this.englishName = englishName;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    /**
     * 根据状态码获取枚举
     * 
     * @param code 状态码
     * @return 枚举值
     */
    public static ResultStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 根据状态码获取中文名称
     * 
     * @param code 状态码
     * @return 中文名称
     */
    public static String getChineseNameByCode(Integer code) {
        ResultStatusEnum status = getByCode(code);
        return status != null ? status.getChineseName() : "未知";
    }
    
    /**
     * 根据状态码获取英文名称
     * 
     * @param code 状态码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        ResultStatusEnum status = getByCode(code);
        return status != null ? status.getEnglishName() : "Unknown";
    }
}