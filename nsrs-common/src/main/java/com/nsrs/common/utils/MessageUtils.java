package com.nsrs.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;

/**
 * 国际化工具类
 */
@Component
public class MessageUtils {

    @Resource
    private MessageSource messageSource;

    /**
     * 获取国际化消息
     * 
     * @param code 消息编码
     * @return 国际化消息
     */
    public String getMessage(String code) {
        return getMessage(code, null);
    }

    /**
     * 获取国际化消息
     *
     * @param code 消息编码
     * @param args 参数
     * @return 国际化消息
     */
    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, code);
    }

    /**
     * 获取国际化消息
     *
     * @param code 消息编码
     * @param args 参数
     * @param defaultMessage 默认消息
     * @return 国际化消息
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
} 