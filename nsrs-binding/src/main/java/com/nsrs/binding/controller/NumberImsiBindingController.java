package com.nsrs.binding.controller;

import com.nsrs.binding.entity.NumberImsiBinding;
import com.nsrs.binding.query.NumberImsiBindingQuery;
import com.nsrs.binding.service.NumberImsiBindingService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 号码与IMSI绑定控制器
 */
@Tag(name = "号码与IMSI绑定管理", description = "号码与IMSI绑定关系的CRUD操作")
@RestController
@RequestMapping("/binding/number-imsi")
@RequiredArgsConstructor
public class NumberImsiBindingController {

    private final NumberImsiBindingService numberImsiBindingService;

    /**
     * 分页查询绑定关系
     */
    @Operation(summary = "分页查询号码与IMSI绑定关系")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberImsiBinding>> page(
            @RequestBody PageRequest<NumberImsiBindingQuery> request) {
        
        PageResult<NumberImsiBinding> pageResult = numberImsiBindingService.page(request);
        
        return CommonResult.success(pageResult);
    }

    /**
     * 绑定号码和IMSI
     */
    @Operation(summary = "绑定号码和Sim卡资源")
    @PostMapping("/bind")
    public CommonResult<Void> bind(@RequestBody NumberImsiBinding binding) {
        return numberImsiBindingService.bind(
            binding.getNumber(),
            binding.getImsi(),
            binding.getIccid(),
            binding.getOrderId(),
            binding.getBindingType(),
            binding.getOperatorUserId(),
            binding.getRemark()
        );
    }

    /**
     * 根据号码解绑
     */
    @Operation(summary = "根据号码解绑")
    @PostMapping("/unbind-by-number")
    public CommonResult<Void> unbindByNumber(
            @Parameter(description = "号码") @RequestParam String number,
            @Parameter(description = "操作用户ID") @RequestParam Long operatorUserId,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        return numberImsiBindingService.unbindByNumber(number, operatorUserId, remark);
    }

//    /**
//     * 批量绑定
//     */
//    @Operation(summary = "批量绑定号码和IMSI")
//    @PostMapping("/batch-bind")
//    public CommonResult<Integer> batchBind(
//            @Parameter(description = "绑定列表") @RequestBody List<NumberImsiBinding> bindingList,
//            @Parameter(description = "操作用户ID") @RequestParam Long operatorUserId) {
//
//        return numberImsiBindingService.batchBind(bindingList, operatorUserId);
//    }
//
//    /**
//     * 批量解绑
//     */
//    @Operation(summary = "批量解绑号码和IMSI")
//    @PostMapping("/batch-unbind")
//    public CommonResult<Integer> batchUnbind(
//            @Parameter(description = "绑定ID列表") @RequestBody List<Long> bindingIds,
//            @Parameter(description = "操作用户ID") @RequestParam Long operatorUserId,
//            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
//
//        return numberImsiBindingService.batchUnbind(bindingIds, operatorUserId, remark);
//    }
//
//    /**
//     * 批量解绑（新版本）
//     */
//    @Operation(summary = "批量解绑号码和IMSI（新版本）")
//    @PostMapping("/batch-unbind-v2")
//    public CommonResult<Integer> batchUnbindV2(
//            @Parameter(description = "批量解绑请求") @RequestBody com.nsrs.binding.dto.BatchUnbindRequest request) {
//
//        return numberImsiBindingService.batchUnbindV2(request);
//    }

    /**
     * 根据号码获取绑定关系
     */
    @Operation(summary = "根据号码获取绑定关系")
    @GetMapping("/by-number/{number}")
    public CommonResult<NumberImsiBinding> getByNumber(
            @Parameter(description = "号码") @PathVariable String number) {
        
        NumberImsiBinding binding = numberImsiBindingService.getByNumber(number);
        
        return CommonResult.success(binding);
    }

    /**
     * 根据IMSI获取绑定关系
     */
//    @Operation(summary = "根据IMSI获取绑定关系")
//    @GetMapping("/by-imsi/{imsi}")
//    public CommonResult<NumberImsiBinding> getByImsi(
//            @Parameter(description = "IMSI号码") @PathVariable String imsi) {
//
//        NumberImsiBinding binding = numberImsiBindingService.getByImsi(imsi);
//
//        return CommonResult.success(binding);
//    }

    /**
     * 根据订单ID获取绑定关系列表
     */
    @Operation(summary = "根据订单ID获取绑定关系列表")
    @GetMapping("/by-order/{orderId}")
    public CommonResult<List<NumberImsiBinding>> getByOrderId(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        
        List<NumberImsiBinding> bindings = numberImsiBindingService.getByOrderId(orderId);
        
        return CommonResult.success(bindings);
    }

    /**
     * 统计绑定数量
     */
//    @Operation(summary = "统计绑定数量")
//    @GetMapping("/count")
//    public CommonResult<Map<String, Object>> countBindings(
//            @Parameter(description = "号码") @RequestParam(required = false) String number,
//            @Parameter(description = "IMSI号码") @RequestParam(required = false) String imsi,
//            @Parameter(description = "绑定状态：1-已绑定，2-已解绑") @RequestParam(required = false) Integer bindingStatus) {
//
//        // 构建查询参数
//        Map<String, Object> params = new HashMap<>();
//        params.put("number", number);
//        params.put("imsi", imsi);
//        params.put("bindingStatus", bindingStatus);
//
//        Map<String, Object> countResult = numberImsiBindingService.countBindings(params);
//
//        return CommonResult.success(countResult);
//    }

}