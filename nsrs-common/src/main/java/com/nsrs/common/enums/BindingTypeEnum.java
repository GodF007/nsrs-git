package com.nsrs.common.enums;

/**
 * 绑定类型枚举
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum BindingTypeEnum {
    
    /**
     * 普通绑定
     */
    NORMAL(1, "普通绑定", "Normal"),
    
    /**
     * 批量绑定
     */
    BATCH(2, "批量绑定", "Batch"),
    
    /**
     * 测试绑定
     */
    TEST(3, "测试绑定", "Test");
    
    private final Integer code;
    private final String chineseName;
    private final String englishName;
    
    BindingTypeEnum(Integer code, String chineseName, String englishName) {
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
     * 根据类型码获取枚举
     * 
     * @param code 类型码
     * @return 枚举值
     */
    public static BindingTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BindingTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 根据类型码获取中文名称
     * 
     * @param code 类型码
     * @return 中文名称
     */
    public static String getChineseNameByCode(Integer code) {
        BindingTypeEnum type = getByCode(code);
        return type != null ? type.getChineseName() : "未知";
    }
    
    /**
     * 根据类型码获取英文名称
     * 
     * @param code 类型码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        BindingTypeEnum type = getByCode(code);
        return type != null ? type.getEnglishName() : "Unknown";
    }
}