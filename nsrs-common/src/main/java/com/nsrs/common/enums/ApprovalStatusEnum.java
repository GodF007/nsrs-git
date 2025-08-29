
package com.nsrs.common.enums;

/**
 * 审批状态枚举
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum ApprovalStatusEnum {
    
    /**
     * 待审批
     */
    PENDING(0, "待审批", "Pending"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过", "Approved"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝", "Rejected"),
    
    /**
     * 已取消
     */
    CANCELLED(3, "已取消", "Cancelled");
    
    private final Integer code;
    private final String chineseName;
    private final String englishName;
    
    ApprovalStatusEnum(Integer code, String chineseName, String englishName) {
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
    public static ApprovalStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalStatusEnum status : values()) {
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
        ApprovalStatusEnum status = getByCode(code);
        return status != null ? status.getChineseName() : "未知";
    }
    
    /**
     * 根据状态码获取英文名称
     * 
     * @param code 状态码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        ApprovalStatusEnum status = getByCode(code);
        return status != null ? status.getEnglishName() : "Unknown";
    }
}