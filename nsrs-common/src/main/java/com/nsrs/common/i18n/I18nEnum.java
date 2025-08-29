package com.nsrs.common.i18n;

/**
 * 国际化枚举接口
 */
public interface I18nEnum {
    
    /**
     * 获取编码
     * 
     * @return 编码
     */
    Integer getCode();
    
    /**
     * 获取消息键
     * 
     * @return 消息键
     */
    String getMessageKey();
    
    /**
     * 获取默认消息
     * 
     * @return 默认消息
     */
    String getDefaultMessage();
} 