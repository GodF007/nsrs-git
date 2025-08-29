package com.nsrs.busacc.service.impl;

import com.nsrs.binding.entity.NumberImsiBinding;
import com.nsrs.binding.service.NumberImsiBindingService;
import com.nsrs.busacc.dto.BindingActivationRequest;
import com.nsrs.busacc.dto.BindingActivationResponse;
import com.nsrs.busacc.service.BindingActivationService;
import com.nsrs.common.model.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 绑定激活服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BindingActivationServiceImpl implements BindingActivationService {
    
    private final NumberImsiBindingService numberImsiBindingService;
    
    @Override
    public CommonResult<BindingActivationResponse> bindAndActivate(BindingActivationRequest request) {
        log.info("Start binding and activation for number: {}, imsi: {}, iccid: {}", 
                request.getNumber(), request.getImsi(), request.getIccid());
        
        try {
            // 调用绑定模块的绑定接口
            CommonResult<Void> bindResult = numberImsiBindingService.bind(
                    request.getNumber(),
                    request.getImsi(),
                    request.getIccid(),
                    request.getOrderId(),
                    request.getBindingType(),
                    request.getOperatorUserId(),
                    request.getRemark()
            );
            
            BindingActivationResponse response = new BindingActivationResponse();
            response.setNumber(request.getNumber());
            response.setImsi(request.getImsi());
            response.setIccid(request.getIccid());
            response.setBindingTime(new Date());
            
            if (bindResult.getSuccess()) {
                log.info("Binding and activation successful for number: {}", request.getNumber());
                
                // 查询绑定信息获取绑定ID
                NumberImsiBinding binding = numberImsiBindingService.getByNumber(request.getNumber());
                if (binding != null) {
                    response.setBindingId(binding.getBindingId());
                    response.setBindingStatus(binding.getBindingStatus());
                }
                
                response.setSuccess(true);
                response.setMessage("Binding and activation successful");
                
                return CommonResult.success(response);
            } else {
                log.error("Binding and activation failed for number: {}, error: {}", 
                        request.getNumber(), bindResult.getMessage());
                
                response.setSuccess(false);
                response.setMessage(bindResult.getMessage());
                
                return CommonResult.failed(bindResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Exception occurred during binding and activation for number: {}", 
                    request.getNumber(), e);
            
            BindingActivationResponse response = new BindingActivationResponse();
            response.setNumber(request.getNumber());
            response.setImsi(request.getImsi());
            response.setIccid(request.getIccid());
            response.setSuccess(false);
            response.setMessage("Binding and activation failed: " + e.getMessage());
            
            return CommonResult.failed("Binding and activation failed: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResult<Void> unbindByNumber(String number, Long operatorUserId, String remark) {
        log.info("Start unbinding for number: {}", number);
        
        try {
            // 调用绑定模块的基于号码的解绑接口
            CommonResult<Void> unbindResult = numberImsiBindingService.unbindByNumber(number, operatorUserId, remark);
            
            if (unbindResult.getSuccess()) {
                log.info("Unbinding successful for number: {}", number);
                return CommonResult.success();
            } else {
                log.error("Unbinding failed for number: {}, error: {}", number, unbindResult.getMessage());
                return CommonResult.failed(unbindResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Exception occurred during unbinding for number: {}", number, e);
            return CommonResult.failed("Unbinding failed: " + e.getMessage());
        }
    }
}