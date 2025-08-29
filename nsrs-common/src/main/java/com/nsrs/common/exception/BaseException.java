package com.nsrs.common.exception;

import lombok.Getter;

/**
 * 基础异常类
 */
@Getter
public class BaseException extends RuntimeException {
    
    private final String code;
    private final String message;
    
    public BaseException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
} 