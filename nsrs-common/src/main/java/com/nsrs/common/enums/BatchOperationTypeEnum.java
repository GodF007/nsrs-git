package com.nsrs.common.enums;

/**
 * 批量操作类型枚举
 * 
 * @author NSRS
 * @since 2024-01-01
 */
public enum BatchOperationTypeEnum {
    
    /**
     * 创建
     */
    CREATE(1, "创建", "Create"),
    
    /**
     * 预留
     */
    RESERVE(2, "预留", "Reserve"),
    
    /**
     * 分配
     */
    ASSIGN(3, "分配", "Assign"),
    
    /**
     * 激活
     */
    ACTIVATE(4, "激活", "Activate"),
    
    /**
     * 冻结
     */
    FREEZE(5, "冻结", "Freeze"),
    
    /**
     * 解冻
     */
    UNFREEZE(6, "解冻", "Unfreeze"),
    
    /**
     * 释放
     */
    RELEASE(7, "释放", "Release"),
    
    /**
     * 回收
     */
    RECYCLE(8, "回收", "Recycle"),
    
    /**
     * 修改
     */
    MODIFY(9, "修改", "Modify"),
    
    /**
     * 删除
     */
    DELETE(10, "删除", "Delete");
    
    private final Integer code;
    private final String chineseName;
    private final String englishName;
    
    BatchOperationTypeEnum(Integer code, String chineseName, String englishName) {
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
     * 根据操作码获取枚举
     * 
     * @param code 操作码
     * @return 枚举值
     */
    public static BatchOperationTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BatchOperationTypeEnum operation : values()) {
            if (operation.getCode().equals(code)) {
                return operation;
            }
        }
        return null;
    }
    
    /**
     * 根据操作码获取中文名称
     * 
     * @param code 操作码
     * @return 中文名称
     */
    public static String getChineseNameByCode(Integer code) {
        BatchOperationTypeEnum operation = getByCode(code);
        return operation != null ? operation.getChineseName() : "未知";
    }
    
    /**
     * 根据操作码获取英文名称
     * 
     * @param code 操作码
     * @return 英文名称
     */
    public static String getEnglishNameByCode(Integer code) {
        BatchOperationTypeEnum operation = getByCode(code);
        return operation != null ? operation.getEnglishName() : "Unknown";
    }
}