package com.nsrs.msisdn.enums.error;

import com.nsrs.common.i18n.I18nEnum;
import lombok.Getter;

/**
 * 号码业务错误枚举
 */
@Getter
public enum MsisdnErrorEnum implements I18nEnum {
    
    NUMBER_NOT_FOUND("A0001", "error.number.not.found", "号码不存在"),
    NUMBER_STATUS_INVALID("A0002", "error.number.status.invalid", "号码状态无效"),
    NUMBER_ALREADY_EXISTS("A0003", "error.number.already.exists", "号码已存在"),
    NUMBER_SEGMENT_NOT_FOUND("A0004", "error.number.segment.not.found", "号码段不存在"),
    NUMBER_LEVEL_NOT_FOUND("A0005", "error.number.level.not.found", "号码级别不存在"),
    NUMBER_PATTERN_NOT_FOUND("A0006", "error.number.pattern.not.found", "号码模式不存在"),
    NUMBER_APPROVAL_NOT_FOUND("A0007", "error.number.approval.not.found", "号码审批记录不存在"),
    HLR_NOT_FOUND("A0008", "error.hlr.not.found", "HLR/交换机不存在"),
    REGION_NOT_FOUND("A0009", "error.region.not.found", "区域不存在");
    
    private final String code;
    private final String messageKey;
    private final String defaultMessage;
    
    MsisdnErrorEnum(String code, String messageKey, String defaultMessage) {
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
    }
    
    /**
     * 获取枚举编码（兼容I18nEnum接口）
     * @return 枚举编码
     */
    @Override
    public Integer getCode() {
        return Integer.valueOf(this.code);
    }
    
    /**
     * 获取原始字符串编码
     * @return 原始字符串编码
     */
    public String getCodeString() {
        return this.code;
    }
    
    /**
     * 根据code获取枚举
     */
    public static MsisdnErrorEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MsisdnErrorEnum item : values()) {
            if (item.getCodeString().equals(code)) {
                return item;
            }
        }
        return null;
    }
} 