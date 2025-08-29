package com.nsrs.simcard.enums;

/**
 * SIM Card Status Enumeration
 */
public enum SimCardStatusEnum {
    
    /**
     * Published
     */
    PUBLISHED(1, "Published"),
    
    /**
     * Assigned
     */
    ASSIGNED(2, "Assigned"),
    
    /**
     * Activated
     */
    ACTIVATED(3, "Activated"),
    
    /**
     * Deactivated
     */
    DEACTIVATED(4, "Deactivated"),
    
    /**
     * Recycled
     */
    RECYCLED(5, "Recycled"),
    
    /**
     * Unknown
     */
    UNKNOWN(-1, "Unknown");
    
    private final Integer code;
    private final String description;
    
    SimCardStatusEnum(Integer code, String description) {
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
    public static SimCardStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SimCardStatusEnum status : values()) {
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