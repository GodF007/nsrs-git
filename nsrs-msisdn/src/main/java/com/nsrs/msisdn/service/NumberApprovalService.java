package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.NumberApproval;

/**
 * 号码审批服务接口
 */
public interface NumberApprovalService extends IService<NumberApproval> {
    
    /**
     * 分页查询号码审批
     *
     * @param page 分页参数
     * @param number 号码
     * @param approvalNo 审批编号
     * @param applicantName 申请人姓名
     * @param levelId 号码级别ID
     * @param status 状态
     * @return 分页结果
     */
    Page<NumberApproval> pageList(Page<NumberApproval> page, String number, String approvalNo, 
                                 String applicantName, Long levelId, Integer status);
    
    /**
     * 申请号码
     *
     * @param numberId 号码ID
     * @param applicantId 申请人ID
     * @param applicantName 申请人姓名
     * @param applyReason 申请原因
     * @param customerInfo 客户信息
     * @return 审批ID
     */
    Long applyNumber(Long numberId, Long applicantId, String applicantName, String applyReason, String customerInfo);
    
    /**
     * 审批通过
     *
     * @param approvalId 审批ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approvalOpinion 审批意见
     * @return 是否成功
     */
    boolean approve(Long approvalId, Long approverId, String approverName, String approvalOpinion);
    
    /**
     * 审批拒绝
     *
     * @param approvalId 审批ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approvalOpinion 审批意见
     * @return 是否成功
     */
    boolean reject(Long approvalId, Long approverId, String approverName, String approvalOpinion);
    
    /**
     * 取消申请
     *
     * @param approvalId 审批ID
     * @param cancelReason 取消原因
     * @return 是否成功
     */
    boolean cancel(Long approvalId, String cancelReason);
    
    /**
     * 获取审批详情
     *
     * @param approvalId 审批ID
     * @return 审批详情
     */
    NumberApproval getDetail(Long approvalId);
    
    /**
     * 根据审批编号获取详情
     *
     * @param approvalNo 审批编号
     * @return 审批详情
     */
    NumberApproval getByApprovalNo(String approvalNo);
} 