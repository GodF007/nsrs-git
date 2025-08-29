package com.nsrs.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误数据
     */
    private Object data;

    /**
     * 构造方法
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误消息
     * @param data 错误数据
     */
    public BusinessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置错误码
     * @param code 错误码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取错误消息
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取错误数据
     * @return 错误数据
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置错误数据
     * @param data 错误数据
     */
    public void setData(Object data) {
        this.data = data;
    }
} 