package com.nsrs.busacc.service;

import com.nsrs.busacc.dto.BindingActivationRequest;
import com.nsrs.busacc.dto.BindingActivationResponse;
import com.nsrs.common.model.CommonResult;

/**
 * 绑定激活服务接口
 */
public interface BindingActivationService {
    
    /**
     * 绑定激活号码和IMSI
     *
     * @param request 绑定激活请求
     * @return 绑定激活响应
     */
    CommonResult<BindingActivationResponse> bindAndActivate(BindingActivationRequest request);
    
    /**
     * 根据号码解绑
     *
     * @param number 号码
     * @param operatorUserId 操作用户ID
     * @param remark 备注
     * @return 操作结果
     */
    CommonResult<Void> unbindByNumber(String number, Long operatorUserId, String remark);
}