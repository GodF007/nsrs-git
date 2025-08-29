package com.nsrs.msisdn.utils;

import com.nsrs.common.exception.BusinessException;
import com.nsrs.common.utils.MessageUtils;
import com.nsrs.msisdn.enums.error.MsisdnErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 号码业务异常工具类
 */
@Component
public class MsisdnExceptionUtils {

    @Autowired
    private MessageUtils messageUtils;

    /**
     * 创建业务异常
     *
     * @param errorEnum 错误枚举
     * @return 业务异常
     */
    public BusinessException createException(MsisdnErrorEnum errorEnum) {
        String message = messageUtils.getMessage(errorEnum.getMessageKey(), null, errorEnum.getDefaultMessage());
        return new BusinessException(errorEnum.getCodeString(), message);
    }

    /**
     * 创建业务异常
     *
     * @param errorEnum 错误枚举
     * @param detail 错误详情
     * @return 业务异常
     */
    public BusinessException createException(MsisdnErrorEnum errorEnum, String detail) {
        String message = messageUtils.getMessage(errorEnum.getMessageKey(), null, errorEnum.getDefaultMessage());
        return new BusinessException(errorEnum.getCodeString(), message, detail);
    }

    /**
     * 创建业务异常
     *
     * @param errorEnum 错误枚举
     * @param detail 错误详情
     * @param cause 错误原因
     * @return 业务异常
     */
    public BusinessException createException(MsisdnErrorEnum errorEnum, String detail, Throwable cause) {
        String message = messageUtils.getMessage(errorEnum.getMessageKey(), null, errorEnum.getDefaultMessage());
        String detailWithCause = detail + " [Cause: " + cause.getMessage() + "]";
        return new BusinessException(errorEnum.getCodeString(), message, detailWithCause);
    }
} 