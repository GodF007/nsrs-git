package com.nsrs.busacc.controller;

import com.nsrs.busacc.dto.BindingActivationRequest;
import com.nsrs.busacc.dto.BindingActivationResponse;
import com.nsrs.busacc.service.BindingActivationService;
import com.nsrs.common.model.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 绑定激活控制器
 */
@Slf4j
@Tag(name = "绑定激活管理", description = "号码与IMSI绑定激活功能接口")
@RestController
@RequestMapping("/busacc/binding-activation")
@RequiredArgsConstructor
public class BindingActivationController {
    
    private final BindingActivationService bindingActivationService;
    
    /**
     * 绑定激活号码和IMSI
     */
    @Operation(summary = "绑定激活号码和IMSI", description = "将号码与IMSI进行绑定并激活")
    @PostMapping("/bind")
    public CommonResult<BindingActivationResponse> bindAndActivate(
            @Parameter(description = "绑定激活请求") @RequestBody @Validated BindingActivationRequest request) {
        
        log.info("Received binding activation request for number: {}, imsi: {}, iccid: {}", 
                request.getNumber(), request.getImsi(), request.getIccid());
        
        try {
            CommonResult<BindingActivationResponse> result = bindingActivationService.bindAndActivate(request);
            
            if (result.getSuccess()) {
                log.info("Binding activation completed successfully for number: {}", request.getNumber());
            } else {
                log.warn("Binding activation failed for number: {}, reason: {}", 
                        request.getNumber(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Exception occurred during binding activation for number: {}", 
                    request.getNumber(), e);
            return CommonResult.failed("Binding and activation failed: " + e.getMessage());
        }
    }
    
    /**
     * 根据号码解绑
     */
    @Operation(summary = "根据号码解绑", description = "根据号码解除绑定关系")
    @PostMapping("/unbind-by-number")
    public CommonResult<Void> unbindByNumber(
            @Parameter(description = "号码") @RequestParam String number,
            @Parameter(description = "操作用户ID") @RequestParam Long operatorUserId,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        log.info("Received unbinding request for number: {}, operatorUserId: {}", 
                number, operatorUserId);
        
        try {
            CommonResult<Void> result = bindingActivationService.unbindByNumber(number, operatorUserId, remark);
            
            if (result.getSuccess()) {
                log.info("Unbinding completed successfully for number: {}", number);
            } else {
                log.warn("Unbinding failed for number: {}, reason: {}", 
                        number, result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Exception occurred during unbinding for number: {}", number, e);
            return CommonResult.failed("Unbinding failed: " + e.getMessage());
        }
    }

}