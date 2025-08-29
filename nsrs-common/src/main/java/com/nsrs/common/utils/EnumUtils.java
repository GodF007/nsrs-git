package com.nsrs.common.utils;

import com.nsrs.common.i18n.I18nEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 枚举工具类
 */
@Component
public class EnumUtils {

    @Autowired
    private MessageUtils messageUtils;

    /**
     * 获取枚举的国际化消息
     *
     * @param e 枚举
     * @return 国际化消息
     */
    public String getMessage(I18nEnum e) {
        return messageUtils.getMessage(e.getMessageKey(), null, e.getDefaultMessage());
    }

    /**
     * 根据code获取枚举的国际化消息
     *
     * @param enumClass 枚举类
     * @param code 编码
     * @return 国际化消息
     */
    public <T extends Enum<T> & I18nEnum> String getMessageByCode(Class<T> enumClass, Integer code) {
        T[] enums = enumClass.getEnumConstants();
        for (T e : enums) {
            if (e.getCode().equals(code)) {
                return getMessage(e);
            }
        }
        return null;
    }
} 