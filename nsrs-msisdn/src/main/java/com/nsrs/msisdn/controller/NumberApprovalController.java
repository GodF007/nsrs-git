package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.NumberApprovalQueryEntity;
import com.nsrs.msisdn.entity.NumberApproval;
import com.nsrs.msisdn.service.NumberApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 号码审批控制器
 */
@Tag(name = "号码审批", description = "号码资源审批流程管理")
@RestController
@RequestMapping("/msisdn/approval")
public class NumberApprovalController {

    @Autowired
    private NumberApprovalService approvalService;

    /**
     * 分页查询号码审批
     */
    @Operation(summary = "分页查询号码审批")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberApproval>> page(@Valid @RequestBody PageRequest<NumberApprovalQueryEntity> request) {
        NumberApprovalQueryEntity query = request.getQuery();
        
        // 构建分页参数
        Page<NumberApproval> page = new Page<>(request.getCurrent(), request.getSize());
        
        // 执行查询
        Page<NumberApproval> result = approvalService.pageList(page, 
            query != null ? query.getApplyNumber() : null, // 使用applyNumber映射到number参数
            null, // approvalNo字段在NumberApprovalQueryEntity中不存在，传null
            query != null ? query.getApplicant() : null, // 使用applicant映射到applicantName参数
            null, // levelId字段在NumberApprovalQueryEntity中不存在，传null
            query != null ? query.getApprovalStatus() : null); // 使用approvalStatus映射到status参数
        
        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 申请号码
     */
    @Operation(summary = "申请号码")
    @PostMapping("/apply")
    public CommonResult<Long> applyNumber(
            @Parameter(description = "号码ID") @RequestParam @NotNull(message = "Number ID cannot be null") Long numberId,
            @Parameter(description = "申请人ID") @RequestParam @NotNull(message = "Applicant ID cannot be null") Long applicantId,
            @Parameter(description = "申请人姓名") @RequestParam @NotBlank(message = "Applicant name cannot be blank") String applicantName,
            @Parameter(description = "申请原因") @RequestParam(required = false) String applyReason,
            @Parameter(description = "客户信息") @RequestParam(required = false) String customerInfo) {
        
        // 执行申请
        Long approvalId = approvalService.applyNumber(numberId, applicantId, applicantName, 
                                                      applyReason, customerInfo);
        
        return CommonResult.success(approvalId);
    }

    /**
     * 审批通过
     */
    @Operation(summary = "审批通过")
    @PutMapping("/{approvalId}/approve")
    public CommonResult<Boolean> approve(
            @Parameter(description = "审批ID") @PathVariable @NotNull(message = "Approval ID cannot be null") Long approvalId,
            @Parameter(description = "审批人ID") @RequestParam @NotNull(message = "Approver ID cannot be null") Long approverId,
            @Parameter(description = "审批人姓名") @RequestParam @NotBlank(message = "Approver name cannot be blank") String approverName,
            @Parameter(description = "审批意见") @RequestParam(required = false) String approvalOpinion) {
        
        // 执行审批通过
        boolean result = approvalService.approve(approvalId, approverId, approverName, approvalOpinion);
        
        return CommonResult.success(result);
    }

    /**
     * 审批拒绝
     */
    @Operation(summary = "审批拒绝")
    @PutMapping("/{approvalId}/reject")
    public CommonResult<Boolean> reject(
            @Parameter(description = "审批ID") @PathVariable @NotNull(message = "Approval ID cannot be null") Long approvalId,
            @Parameter(description = "审批人ID") @RequestParam @NotNull(message = "Approver ID cannot be null") Long approverId,
            @Parameter(description = "审批人姓名") @RequestParam @NotBlank(message = "Approver name cannot be blank") String approverName,
            @Parameter(description = "审批意见") @RequestParam(required = false) String approvalOpinion) {
        
        // 执行审批拒绝
        boolean result = approvalService.reject(approvalId, approverId, approverName, approvalOpinion);
        
        return CommonResult.success(result);
    }

    /**
     * 取消申请
     */
    @Operation(summary = "取消申请")
    @PutMapping("/{approvalId}/cancel")
    public CommonResult<Boolean> cancel(
            @Parameter(description = "审批ID") @PathVariable Long approvalId,
            @Parameter(description = "取消原因") @RequestParam(required = false) String cancelReason) {
        
        // 执行取消申请
        boolean result = approvalService.cancel(approvalId, cancelReason);
        
        return CommonResult.success(result);
    }

    /**
     * 获取审批详情
     */
    @Operation(summary = "获取审批详情")
    @GetMapping("/{approvalId}")
    public CommonResult<NumberApproval> getDetail(
            @Parameter(description = "审批ID") @PathVariable Long approvalId) {
        
        // 获取审批详情
        NumberApproval approval = approvalService.getDetail(approvalId);
        
        return CommonResult.success(approval);
    }

    /**
     * 根据审批编号获取详情
     */
    @Operation(summary = "根据审批编号获取详情")
    @GetMapping("/no/{approvalNo}")
    public CommonResult<NumberApproval> getByApprovalNo(
            @Parameter(description = "审批编号") @PathVariable String approvalNo) {
        
        // 获取审批详情
        NumberApproval approval = approvalService.getByApprovalNo(approvalNo);
        
        return CommonResult.success(approval);
    }
}