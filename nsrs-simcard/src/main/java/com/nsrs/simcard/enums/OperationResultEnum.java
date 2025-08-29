package com.nsrs.simcard.enums;

/**
 * Operation Result Enumeration
 */
public enum OperationResultEnum {
    
    /**
     * Failed
     */
    FAILED(0, "Failed"),
    
    /**
     * Success
     */
    SUCCESS(1, "Success");
    
    private final Integer code;
    private final String description;
    
    OperationResultEnum(Integer code, String description) {
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
    public static OperationResultEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationResultEnum result : values()) {
            if (result.getCode().equals(code)) {
                return result;
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