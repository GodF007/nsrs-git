package com.nsrs.common.enums;

/**
 * 号码状态枚举
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum NumberStatusEnum {
    
    /**
     * 空闲
     */
    IDLE(1, "空闲", "Idle"),
    
    /**
     * 预留
     */
    RESERVED(2, "预留", "Reserved"),
    
    /**
     * 已分配
     */
    ASSIGNED(3, "已分配", "Assigned"),
    
    /**
     * 已激活
     */
    ACTIVATED(4, "已激活", "Activated"),
    
    /**
     * 已使用
     */
    IN_USE(5, "已使用", "In Use"),
    
    /**
     * 已冻结
     */
    FROZEN(6, "已冻结", "Frozen"),
    
    /**
     * 已锁定
     */
    LOCKED(7, "已锁定", "Locked");
    
    private final Integer code;
    private final String chineseName;
    private final String englishName;
    
    NumberStatusEnum(Integer code, String chineseName, String englishName) {
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
    public static NumberStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NumberStatusEnum status : values()) {
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
        NumberStatusEnum status = getByCode(code);
        return status != null ? status.getChineseName() : "未知";
    }
    
    /**
     * 根据状态码获取英文名称
     * 
     * @param code 状态码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        NumberStatusEnum status = getByCode(code);
        return status != null ? status.getEnglishName() : "Unknown";
    }
}