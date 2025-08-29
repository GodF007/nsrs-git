package com.nsrs.common.enums;

/**
 * 数据类型枚举（SIM卡类型）
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum DataTypeEnum {
    
    /**
     * 流量卡
     */
    DATA_CARD(1, "流量卡", "Data Card"),
    
    /**
     * 语音卡
     */
    VOICE_CARD(2, "语音卡", "Voice Card"),
    
    /**
     * 双模卡
     */
    DUAL_MODE_CARD(3, "双模卡", "Dual Mode Card"),
    
    /**
     * 物联网卡
     */
    IOT_CARD(4, "物联网卡", "IoT Card");
    
    private final Integer code;
    private final String chineseName;
    private final String englishName;
    
    DataTypeEnum(Integer code, String chineseName, String englishName) {
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
    public static DataTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataTypeEnum type : values()) {
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
        DataTypeEnum type = getByCode(code);
        return type != null ? type.getChineseName() : "未知";
    }
    
    /**
     * 根据类型码获取英文名称
     * 
     * @param code 类型码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        DataTypeEnum type = getByCode(code);
        return type != null ? type.getEnglishName() : "Unknown";
    }
}