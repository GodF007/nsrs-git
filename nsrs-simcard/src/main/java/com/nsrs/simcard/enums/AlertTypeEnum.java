package com.nsrs.simcard.enums;

/**
 * Alert Type Enumeration
 */
public enum AlertTypeEnum {
    
    /**
     * Low Inventory Alert
     */
    LOW(1, "Low Inventory Alert"),
    
    /**
     * High Inventory Alert
     */
    HIGH(2, "High Inventory Alert");
    
    private final Integer code;
    private final String description;
    
    AlertTypeEnum(Integer code, String description) {
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
    public static AlertTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AlertTypeEnum type : values()) {
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