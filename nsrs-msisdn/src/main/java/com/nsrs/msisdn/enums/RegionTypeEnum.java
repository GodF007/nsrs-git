package com.nsrs.msisdn.enums;

import com.nsrs.common.i18n.I18nEnum;
import lombok.Getter;

/**
 * 区域类型枚举
 */
@Getter
public enum RegionTypeEnum implements I18nEnum {
    
    COUNTRY(1, "region.type.country", "国家"),
    PROVINCE(2, "region.type.province", "省份"),
    CITY(3, "region.type.city", "城市"),
    DISTRICT(4, "region.type.district", "区县");
    
    private final Integer code;
    private final String messageKey;
    private final String defaultMessage;
    
    RegionTypeEnum(Integer code, String messageKey, String defaultMessage) {
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
    }
    
    /**
     * 根据code获取枚举
     */
    public static RegionTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RegionTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
} 