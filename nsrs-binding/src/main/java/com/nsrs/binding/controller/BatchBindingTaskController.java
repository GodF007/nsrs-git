package com.nsrs.binding.controller;

import com.nsrs.binding.entity.BatchBindingDetail;
import com.nsrs.binding.entity.BatchBindingTask;
import com.nsrs.binding.query.BatchBindingDetailQuery;
import com.nsrs.binding.query.BatchBindingTaskQuery;
import com.nsrs.binding.service.BatchBindingDetailService;
import com.nsrs.binding.service.BatchBindingTaskService;
import com.nsrs.binding.constants.BindingConstants;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 批量绑定任务控制器
 */
@Tag(name = "批量绑定任务管理", description = "批量绑定任务的CRUD操作")
@RestController
@RequestMapping("/binding/task")
@RequiredArgsConstructor
public class BatchBindingTaskController {

    private final BatchBindingTaskService batchBindingTaskService;
    private final BatchBindingDetailService batchBindingDetailService;

    /**
     * 分页查询批量绑定任务
     */
    @Operation(summary = "分页查询批量绑定任务")
    @PostMapping("/page")
    public CommonResult<PageResult<BatchBindingTask>> page(
            @RequestBody PageRequest<BatchBindingTaskQuery> request) {
        
        PageResult<BatchBindingTask> pageResult = batchBindingTaskService.page(request);
        
        return CommonResult.success(pageResult);
    }

    /**
     * 获取批量绑定任务详情
     */
    @Operation(summary = "获取批量绑定任务详情")
    @GetMapping("/{taskId}")
    public CommonResult<BatchBindingTask> getTaskDetail(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        BatchBindingTask task = batchBindingTaskService.getTaskDetail(taskId);
        
        return CommonResult.success(task);
    }

    /**
     * 创建批量绑定任务
     */
    @PostMapping("/create")
    @Operation(summary = "创建批量绑定任务", description = "创建批量绑定任务，任务创建后不会立即执行，需要手动启动")
    public CommonResult<String> createTask(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskName") String taskName,
            @RequestParam(value = "description", required = false) String description) {
        return batchBindingTaskService.createTask(file, taskName, description);
    }

    /**
     * 创建批量解绑任务
     */
    @PostMapping("/create-unbind")
    @Operation(summary = "创建批量解绑任务", description = "创建批量解绑任务，任务创建后不会立即执行，需要手动启动")
    public CommonResult<String> createUnbindTask(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskName") String taskName,
            @RequestParam(value = "description", required = false) String description) {
        return batchBindingTaskService.createUnbindTask(file, taskName, description);
    }

    @GetMapping("/download-binding-template")
    @Operation(summary = "下载批量绑定模板")
    public void downloadBindingTemplate(HttpServletResponse response) {
        batchBindingTaskService.downloadBindingTemplate(response);
    }

    @GetMapping("/download-unbind-template")
    @Operation(summary = "下载批量解绑模板")
    public void downloadUnbindTemplate(HttpServletResponse response) {
        batchBindingTaskService.downloadUnbindTemplate(response);
    }

    /**
     * 更新批量绑定任务
     */
    @Operation(summary = "更新批量绑定任务")
    @PutMapping("/updateTask")
    public CommonResult<Void> updateTask(
            @Parameter(description = "任务信息") @RequestBody BatchBindingTask task) {
        
        return batchBindingTaskService.updateTask(task);
    }

    /**
     * 删除批量绑定任务
     */
    @Operation(summary = "删除批量绑定任务")
    @DeleteMapping("/{taskIds}")
    public CommonResult<Void> deleteTasks(
            @Parameter(description = "任务ID列表，多个ID用逗号分隔") @PathVariable List<Long> taskIds) {
        
        return batchBindingTaskService.deleteTasks(taskIds);
    }

    /**
     * 取消批量绑定任务
     */
    @Operation(summary = "取消批量绑定任务")
    @PutMapping("/{taskId}/cancel")
    public CommonResult<Void> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        return batchBindingTaskService.cancelTask(taskId);
    }

    /**
     * 重试批量绑定任务
     */
    @Operation(summary = "重试批量绑定任务")
    @PutMapping("/{taskId}/retry")
    public CommonResult<Void> retryTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        return batchBindingTaskService.retryTask(taskId);
    }

    /**
     * 分页查询批量绑定详情
     */
    @Operation(summary = "分页查询批量绑定详情")
    @PostMapping("/{taskId}/detail/page")
    public CommonResult<PageResult<BatchBindingDetail>> pageDetail(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestBody PageRequest<BatchBindingDetailQuery> request) {
        
        // 设置任务ID到查询条件中
        if (request.getQuery() == null) {
            request.setQuery(new BatchBindingDetailQuery());
        }
        request.getQuery().setTaskId(taskId);
        
        PageResult<BatchBindingDetail> pageResult = batchBindingDetailService.page(request);
        
        return CommonResult.success(pageResult);
    }

    /**
     * 统计任务各状态详情数量
     */
    @Operation(summary = "统计任务各状态详情数量")
    @GetMapping("/{taskId}/detail/count")
    public CommonResult<Map<String, Integer>> countDetailStatus(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        Map<String, Integer> countResult = batchBindingDetailService.countStatus(taskId);
        
        return CommonResult.success(countResult);
    }

    /**
     * 启动任务
     */
    @Operation(summary = "启动任务")
    @PostMapping("/{taskId}/start")
    public CommonResult<Void> startTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        return batchBindingTaskService.startTask(taskId);
    }

    /**
     * 停止任务
     */
    @Operation(summary = "停止任务")
    @PostMapping("/{taskId}/stop")
    public CommonResult<Void> stopTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        
        return batchBindingTaskService.stopTask(taskId);
    }

//    /**
//     * 测试Spring事务注解
//     */
//    @Operation(summary = "测试Spring事务注解")
//    @PostMapping("/test-transaction")
//    public CommonResult<String> testTransaction(
//            @Parameter(description = "是否抛出异常进行回滚测试") @RequestParam(defaultValue = "false") Boolean throwException) {
//
//        // 调用Service层的事务方法
//        return batchBindingTaskService.testTransaction(throwException);
//    }
}