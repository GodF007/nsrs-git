package com.nsrs.common.exception;

import java.io.Serializable;

/**
 * 授权异常类
 * 当用户无权访问某个资源或API时抛出此异常
 */
public class AuthorizationException extends RuntimeException implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 构造函数
     */
    public AuthorizationException() {
        super("没有访问权限");
        this.code = 403;
        this.message = "没有访问权限";
    }
    
    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public AuthorizationException(String message) {
        super(message);
        this.code = 403;
        this.message = message;
    }
    
    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public AuthorizationException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 异常原因
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
        this.code = 403;
        this.message = message;
    }
    
    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public AuthorizationException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
} 