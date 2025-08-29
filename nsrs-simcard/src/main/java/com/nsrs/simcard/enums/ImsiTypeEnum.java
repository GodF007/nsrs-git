package com.nsrs.simcard.enums;

/**
 * IMSI Type Enumeration
 */
public enum ImsiTypeEnum {
    
    /**
     * GSM Postpaid
     */
    GSM_POSTPAID(1, "GSM Postpaid"),
    
    /**
     * GSM Prepaid
     */
    GSM_PREPAID(2, "GSM Prepaid"),
    
    /**
     * CDMA
     */
    CDMA(3, "CDMA");
    
    private final Integer code;
    private final String description;
    
    ImsiTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get enum by code
     */
    public static ImsiTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ImsiTypeEnum type : values()) {
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
}