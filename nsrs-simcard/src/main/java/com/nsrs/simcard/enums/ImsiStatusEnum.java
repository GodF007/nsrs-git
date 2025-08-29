package com.nsrs.simcard.enums;

/**
 * IMSI Status Enumeration
 */
public enum ImsiStatusEnum {
    
    /**
     * Idle
     */
    IDLE(1, "Idle"),
    
    /**
     * Bound
     */
    BOUND(2, "Bound"),
    
    /**
     * Used
     */
    USED(3, "Used"),
    
    /**
     * Locked
     */
    LOCKED(4, "Locked");
    
    private final Integer code;
    private final String description;
    
    ImsiStatusEnum(Integer code, String description) {
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
    public static ImsiStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ImsiStatusEnum status : values()) {
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
}