package com.nsrs.common.enums;

/**
 * 号码类型枚举
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum NumberTypeEnum {
    
    /**
     * PSTN Number 公共交换电话网号码（固话/传真）
     */
    PSTN(1, "PSTN", "公共交换电话网号码"),
    
    /**
     * Mobile Number 移动电话号码
     */
    MOBILE(2, "Mobile", "移动电话号码"),
    
    /**
     * FTTH Number 光纤到户终端标识号
     */
    FTTH(3, "FTTH", "光纤到户终端标识号"),
    
    /**
     * SIP 基于SIP协议的网络电话标识
     */
    SIP(4, "SIP", "基于SIP协议的网络电话标识"),
    
    /**
     * VSAT 卫星通信终端编号
     */
    VSAT(5, "VSAT", "卫星通信终端编号");
    
    private final Integer code;
    private final String englishName;
    private final String chineseName;
    
    NumberTypeEnum(Integer code, String englishName, String chineseName) {
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
     * 根据类型码获取枚举
     * 
     * @param code 类型码
     * @return 枚举值
     */
    public static NumberTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NumberTypeEnum type : values()) {
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
        NumberTypeEnum type = getByCode(code);
        return type != null ? type.getChineseName() : "未知";
    }
    
    /**
     * 根据类型码获取英文名称
     * 
     * @param code 类型码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        NumberTypeEnum type = getByCode(code);
        return type != null ? type.getEnglishName() : "Unknown";
    }
}