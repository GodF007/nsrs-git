package com.nsrs.busacc.controller;

import com.nsrs.busacc.dto.SimCardSelectionRequest;
import com.nsrs.busacc.dto.SimCardSelectionResponse;
import com.nsrs.busacc.service.SimCardSelectionService;
import com.nsrs.common.model.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SIM卡选择控制器
 * 统一的SIM卡选择接口，支持多种选择方式
 */
@Slf4j
@RestController
@RequestMapping("/busacc/simcard-selection")
@RequiredArgsConstructor
@Tag(name = "SIM卡选择", description = "SIM卡选择相关接口")
public class SimCardSelectionController {
    
    private final SimCardSelectionService simCardSelectionService;
    
    /**
     * 统一的SIM卡选择接口
     * 支持多种选择方式：
     * 1. 随机选择：不传iccidSuffix参数
     * 2. 按ICCID后缀选择：传入iccidSuffix参数
     * 3. 按条件过滤：传入相应的过滤条件
     */
    @PostMapping("/select")
    @Operation(summary = "选择SIM卡", 
               description = "统一的SIM卡选择接口。支持随机选择（不传iccidSuffix）或按ICCID后缀选择（传入iccidSuffix）")
    public CommonResult<SimCardSelectionResponse> selectSimCards(@Validated @RequestBody SimCardSelectionRequest request) {
        return handleSimCardSelection(request);
    }
    
    /**
     * GET方式的SIM卡选择接口（兼容性接口）
     * 主要用于简单的随机选择或按后缀选择
     */
    @GetMapping("/select")
    @Operation(summary = "选择SIM卡（GET方式）", 
               description = "GET方式的SIM卡选择接口，支持通过URL参数进行选择")
    public CommonResult<SimCardSelectionResponse> selectSimCardsGet(
            @Parameter(description = "ICCID后缀，不传则随机选择") 
            @RequestParam(value = "iccidSuffix", required = false) String iccidSuffix,
            @Parameter(description = "SIM卡池大小，默认30") 
            @RequestParam(value = "poolSize", defaultValue = "30") Integer poolSize,
            @Parameter(description = "数据类型过滤") 
            @RequestParam(value = "dataType", required = false) Integer dataType,
            @Parameter(description = "供应商ID过滤") 
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @Parameter(description = "组织ID过滤") 
            @RequestParam(value = "organizationId", required = false) Long organizationId,
            @Parameter(description = "批次ID过滤") 
            @RequestParam(value = "batchId", required = false) Long batchId) {
        
        SimCardSelectionRequest request = new SimCardSelectionRequest();
        request.setIccidSuffix(iccidSuffix);
        request.setPoolSize(poolSize);
        request.setDataType(dataType);
        request.setSupplierId(supplierId);
        request.setOrganizationId(organizationId);
        request.setBatchId(batchId);
        
        return handleSimCardSelection(request);
    }
    
    /**
     * 统一的SIM卡选择处理逻辑
     * 根据请求参数自动判断选择方式
     */
    private CommonResult<SimCardSelectionResponse> handleSimCardSelection(SimCardSelectionRequest request) {
        try {
            // 记录请求日志
            if (StringUtils.hasText(request.getIccidSuffix())) {
                log.info("SimCard selection by suffix: suffix={}, poolSize={}, dataType={}", 
                        request.getIccidSuffix(), request.getPoolSize(), request.getDataType());
            } else {
                log.info("Random sim card selection: poolSize={}, dataType={}", 
                        request.getPoolSize(), request.getDataType());
            }
            
            // 根据是否有ICCID后缀选择不同的服务方法
            SimCardSelectionResponse response;
            if (StringUtils.hasText(request.getIccidSuffix())) {
                // 按ICCID后缀选择
                response = simCardSelectionService.selectSimCards(request);
            } else {
                // 随机选择
                response = simCardSelectionService.getRandomPool(request);
            }
            
            // 统一返回处理
            if (response.getSuccess()) {
                return CommonResult.success(response);
            } else {
                return CommonResult.failed(response.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to select sim cards", e);
            return CommonResult.failed("选择SIM卡失败: " + e.getMessage());
        }
    }
}