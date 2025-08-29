package com.nsrs.framework.handler;

import com.nsrs.common.exception.BusinessException;
import com.nsrs.common.model.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public CommonResult<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("Business exception occurred, URI: {}, error message: {}", request.getRequestURI(), e.getMessage(), e);
        return CommonResult.failed(e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("Parameter validation exception, URI: {}, error message: {}", request.getRequestURI(), e.getMessage());
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = "Parameter verification failed";
        if (!fieldErrors.isEmpty()) {
            message = fieldErrors.get(0).getDefaultMessage();
        }
        return CommonResult.failed(400, message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<Object> handleBindException(BindException e, HttpServletRequest request) {
        log.error("Parameter binding exception, URI: {}, error message: {}", request.getRequestURI(), e.getMessage());
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = "Parameter binding failed";
        if (!fieldErrors.isEmpty()) {
            message = fieldErrors.get(0).getDefaultMessage();
        }
        return CommonResult.failed(400, message);
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.error("Parameter type mismatch exception, URI: {}, error message: {}", request.getRequestURI(), e.getMessage());
        String message = "Parameter type mismatch";
        return CommonResult.failed(400, message);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<Object> handleException(Exception e, HttpServletRequest request) {
        log.error("System exception occurred, URI: {}, error message: {}", request.getRequestURI(), e.getMessage(), e);
        return CommonResult.failed(500, e.getMessage());
    }
    
}