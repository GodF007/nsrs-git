package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.NumberOperationLogQueryEntity;
import com.nsrs.msisdn.entity.NumberOperationLog;
import com.nsrs.msisdn.service.NumberOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 号码操作日志控制器
 */
@Tag(name = "号码操作日志", description = "号码资源操作日志查询和统计")
@RestController
@RequestMapping("/msisdn/operation-log")
public class NumberOperationLogController {

    @Autowired
    private NumberOperationLogService operationLogService;

    /**
     * 分页查询号码操作日志
     */
    @Operation(summary = "分页查询号码操作日志")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberOperationLog>> page(@Valid @RequestBody PageRequest<NumberOperationLogQueryEntity> request) {
        NumberOperationLogQueryEntity query = request.getQuery();
        
        // 构建分页参数
        Page<NumberOperationLog> page = new Page<>(request.getCurrent(), request.getSize());
        
        // 执行查询
        Page<NumberOperationLog> result = operationLogService.pageList(page, 
            query != null ? query.getOperationNumber() : null, // 使用operationNumber映射到number参数
            null, // numberType字段在NumberOperationLogQueryEntity中不存在，传null
            query != null ? query.getOperationType() : null,
            null, // operatorUserId字段在NumberOperationLogQueryEntity中不存在，传null
            null, // beginTime字段在NumberOperationLogQueryEntity中不存在，传null
            null); // endTime字段在NumberOperationLogQueryEntity中不存在，传null
        
        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 查询号码的操作日志
     */
    @Operation(summary = "查询号码的操作日志")
    @GetMapping("/number/{numberId}")
    public CommonResult<List<NumberOperationLog>> listByNumberId(
            @Parameter(description = "号码ID") @PathVariable @NotNull(message = "Number ID cannot be null") Long numberId) {
        
        // 查询号码操作日志
        List<NumberOperationLog> logs = operationLogService.listByNumberId(numberId);
        
        return CommonResult.success(logs);
    }
    
    /**
     * 根据号码查询操作日志
     */
//    @Operation(summary = "根据号码查询操作日志")
//    @GetMapping("/number-string/{number}")
//    public CommonResult<List<NumberOperationLog>> listByNumber(
//            @Parameter(description = "号码") @PathVariable String number) {
//
//        // 根据号码查询操作日志
//        List<NumberOperationLog> logs = operationLogService.listByNumber(number);
//
//        return CommonResult.success(logs);
//    }
    
    /**
     * 根据号码分页查询操作日志
     */
    @Operation(summary = "根据号码分页查询操作日志")
    @GetMapping("/number-string/{number}/page")
    public CommonResult<PageResult<NumberOperationLog>> pageByNumber(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "10") Integer size) {
        
        // 构建分页参数
        Page<NumberOperationLog> page = new Page<>(current, size);
        
        // 根据号码分页查询操作日志
        Page<NumberOperationLog> result = operationLogService.pageByNumber(page, number);
        
        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 获取操作日志统计信息
     */
    @Operation(summary = "获取操作日志统计信息 -- 暂时用不到")
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        
        // 获取统计信息
        Map<String, Object> statistics = operationLogService.getStatistics(beginTime, endTime);
        
        return CommonResult.success(statistics);
    }

}