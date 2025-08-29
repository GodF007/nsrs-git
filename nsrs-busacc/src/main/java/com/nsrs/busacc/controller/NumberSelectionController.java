package com.nsrs.busacc.controller;

import com.nsrs.busacc.dto.NumberSelectionRequest;
import com.nsrs.busacc.dto.NumberSelectionResponse;
import com.nsrs.busacc.service.NumberSelectionService;
import com.nsrs.common.model.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 号码选择控制器
 */
@Slf4j
@Tag(name = "号码选择管理", description = "选卡选号功能接口")
@RestController
@RequestMapping("/busacc/number-selection")
@RequiredArgsConstructor
public class NumberSelectionController {
    
    private final NumberSelectionService numberSelectionService;
    
    /**
     * 根据号段查询号码
     */
    @Operation(summary = "根据号段查询号码", description = "根据指定号段前缀查询可用号码")
    @GetMapping("/by-prefix")
    public CommonResult<NumberSelectionResponse> selectByPrefix(
            @Parameter(description = "号段前缀") @RequestParam String prefix,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "30") Integer poolSize,
            @Parameter(description = "号码类型") @RequestParam(required = false) Integer numberType) {
        
        log.info("Select numbers by prefix: {}, poolSize: {}, numberType: {}", prefix, poolSize, numberType);
        
        try {
            NumberSelectionRequest request = new NumberSelectionRequest();
            request.setNumberPrefix(prefix);
            request.setPoolSize(poolSize);
            request.setNumberType(numberType);
            
            NumberSelectionResponse response = numberSelectionService.selectNumbers(request);
            
            if (response.getSuccess()) {
                return CommonResult.success(response);
            } else {
                return CommonResult.failed(response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to select numbers by prefix: {}", e.getMessage(), e);
            return CommonResult.failed("Number selection failed: " + e.getMessage());
        }
    }
    
    /**
     * 随机获取号码池
     */
    @Operation(summary = "随机获取号码池", description = "随机从多个分表中获取指定数量的号码")
    @GetMapping("/random")
    public CommonResult<NumberSelectionResponse> getRandomPool(
            @Parameter(description = "号码池大小") @RequestParam(defaultValue = "30") Integer poolSize,
            @Parameter(description = "号码类型") @RequestParam(required = false) Integer numberType) {
        
        log.info("Get random number pool: poolSize: {}, numberType: {}", poolSize, numberType);
        
        try {
            NumberSelectionRequest request = new NumberSelectionRequest();
            request.setPoolSize(poolSize);
            request.setNumberType(numberType);
            
            NumberSelectionResponse response = numberSelectionService.getRandomPool(request);
            
            if (response.getSuccess()) {
                return CommonResult.success(response);
            } else {
                return CommonResult.failed(response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to get random number pool: {}", e.getMessage(), e);
            return CommonResult.failed("Get random pool failed: " + e.getMessage());
        }
    }

}