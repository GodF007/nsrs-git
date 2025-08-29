package com.nsrs.common.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 * @param <T> 数据类型
 */
@Data
public class CommonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 私有构造函数
     */
    private CommonResult() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回结果
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> success() {
        return success(null);
    }

    /**
     * 成功返回结果
     * @param data 数据
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功返回结果
     * @param data 数据
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> success(T data, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(200);
        result.setData(data);
        result.setMessage(message);
        result.setSuccess(true);
        return result;
    }

    /**
     * 失败返回结果
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> failed() {
        return failed("操作失败");
    }

    /**
     * 失败返回结果
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> failed(String message) {
        return failed(500, message);
    }

    /**
     * 失败返回结果
     * @param code 状态码
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> failed(Integer code, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 参数验证失败返回结果
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> validateFailed() {
        return validateFailed("参数验证失败");
    }

    /**
     * 参数验证失败返回结果
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return failed(400, message);
    }

    /**
     * 未授权返回结果
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> unauthorized() {
        return unauthorized("暂未登录或token已经过期");
    }

    /**
     * 未授权返回结果
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> unauthorized(String message) {
        return failed(401, message);
    }

    /**
     * 禁止访问返回结果
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> forbidden() {
        return forbidden("没有相关权限");
    }

    /**
     * 禁止访问返回结果
     * @param message 消息
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> CommonResult<T> forbidden(String message) {
        return failed(403, message);
    }
} 