package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.msisdn.entity.NumberApproval;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.mapper.NumberApprovalMapper;
import com.nsrs.msisdn.mapper.NumberResourceMapper;
import com.nsrs.msisdn.service.NumberApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 号码审批服务实现类
 */
@Slf4j
@Service
public class NumberApprovalServiceImpl extends ServiceImpl<NumberApprovalMapper, NumberApproval> implements NumberApprovalService {

    @Autowired
    private NumberResourceMapper numberResourceMapper;

    @Override
    public Page<NumberApproval> pageList(Page<NumberApproval> page, String number, String approvalNo, 
                                       String applicantName, Long levelId, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<NumberApproval> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(number)) {
            queryWrapper.like(NumberApproval::getNumber, number);
        }
        
        if (StringUtils.isNotBlank(approvalNo)) {
            queryWrapper.like(NumberApproval::getApprovalNo, approvalNo);
        }
        
        if (StringUtils.isNotBlank(applicantName)) {
            queryWrapper.like(NumberApproval::getApplicantName, applicantName);
        }
        
        if (levelId != null) {
            queryWrapper.eq(NumberApproval::getLevelId, levelId);
        }
        
        if (status != null) {
            queryWrapper.eq(NumberApproval::getStatus, status);
        }
        
        // 默认按申请时间降序排序
        queryWrapper.orderByDesc(NumberApproval::getApplyTime);
        
        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyNumber(Long numberId, Long applicantId, String applicantName, String applyReason, String customerInfo) {
        if (numberId == null) {
            throw new BusinessException("400", "Number ID cannot be empty");
        }
        
        if (applicantId == null) {
            throw new BusinessException("400", "Applicant ID cannot be empty");
        }
        
        // 查询号码信息
        NumberResource resource = numberResourceMapper.selectById(numberId);
        if (resource == null) {
            throw new BusinessException("404", "Number does not exist");
        }
        
        // 检查号码状态是否为空闲
        if (resource.getStatus() != 1) {
            throw new BusinessException("400", "Only idle numbers can be applied for");
        }
        
        // 生成审批编号
        String approvalNo = generateApprovalNo();
        
        // 创建审批对象
        NumberApproval approval = new NumberApproval();
        approval.setApprovalNo(approvalNo);
        approval.setNumberId(numberId);
        approval.setNumber(resource.getNumber());
        approval.setApplicantId(applicantId);
        approval.setApplicantName(applicantName);
        approval.setApplyTime(new Date());
        approval.setApplyReason(applyReason);
        approval.setCustomerInfo(customerInfo);
        approval.setLevelId(resource.getLevelId());
        approval.setStatus(0); // 待审批
        
        // 保存审批记录
        boolean success = this.save(approval);
        
        if (success) {
            log.info("Number application successful: Number={}, Applicant={}, Approval No={}", 
                    resource.getNumber(), applicantName, approvalNo);
            
            // 更新号码状态为已锁定
            resource.setStatus(7); // 已锁定
            resource.setUpdateTime(new Date());
            numberResourceMapper.updateById(resource);
            
            return approval.getApprovalId();
        } else {
            log.error("Number application failed: Number={}, Applicant={}", resource.getNumber(), applicantName);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long approvalId, Long approverId, String approverName, String approvalOpinion) {
        if (approvalId == null) {
            throw new BusinessException("400", "Approval ID cannot be empty");
        }
        
        // 查询审批信息
        NumberApproval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException("404", "Approval record does not exist");
        }
        
        // 检查审批状态
        if (approval.getStatus() != 0) {
            throw new BusinessException("400", "Only pending approval records can be approved");
        }
        
        // 更新审批信息
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApprovalTime(new Date());
        approval.setApprovalOpinion(approvalOpinion);
        approval.setStatus(1); // 已通过
        
        boolean success = this.updateById(approval);
        
        if (success) {
            log.info("Approval successful: Number={}, Approver={}, Approval ID={}", 
                    approval.getNumber(), approverName, approvalId);
            
            // 更新号码状态为已分配
            NumberResource resource = numberResourceMapper.selectById(approval.getNumberId());
            if (resource != null) {
                resource.setStatus(3); // 已分配
                resource.setUpdateTime(new Date());
                numberResourceMapper.updateById(resource);
            }
            
            return true;
        } else {
            log.error("Approval failed: Number={}, Approver={}, Approval ID={}", 
                    approval.getNumber(), approverName, approvalId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long approvalId, Long approverId, String approverName, String approvalOpinion) {
        if (approvalId == null) {
            throw new BusinessException("400", "Approval ID cannot be empty");
        }
        
        // 查询审批信息
        NumberApproval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException("404", "Approval record does not exist");
        }
        
        // 检查审批状态
        if (approval.getStatus() != 0) {
            throw new BusinessException("400", "Only pending approval records can be approved");
        }
        
        // 更新审批信息
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApprovalTime(new Date());
        approval.setApprovalOpinion(approvalOpinion);
        approval.setStatus(2); // 已拒绝
        
        boolean success = this.updateById(approval);
        
        if (success) {
            log.info("Approval rejection successful: Number={}, Approver={}, Approval ID={}", 
                    approval.getNumber(), approverName, approvalId);
            
            // 更新号码状态为空闲
            NumberResource resource = numberResourceMapper.selectById(approval.getNumberId());
            if (resource != null) {
                resource.setStatus(1); // 空闲
                resource.setUpdateTime(new Date());
                numberResourceMapper.updateById(resource);
            }
            
            return true;
        } else {
            log.error("Approval rejection failed: Number={}, Approver={}, Approval ID={}", 
                    approval.getNumber(), approverName, approvalId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long approvalId, String cancelReason) {
        if (approvalId == null) {
            throw new BusinessException("400", "Approval ID cannot be empty");
        }
        
        // 查询审批信息
        NumberApproval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException("404", "Approval record does not exist");
        }
        
        // 检查审批状态
        if (approval.getStatus() != 0) {
            throw new BusinessException("400", "Only pending approval records can be cancelled");
        }
        
        // 更新审批信息
        approval.setApprovalOpinion(cancelReason);
        approval.setStatus(3); // 已取消
        
        boolean success = this.updateById(approval);
        
        if (success) {
            log.info("Application cancellation successful: Number={}, Approval ID={}", approval.getNumber(), approvalId);
            
            // 更新号码状态为空闲
            NumberResource resource = numberResourceMapper.selectById(approval.getNumberId());
            if (resource != null) {
                resource.setStatus(1); // 空闲
                resource.setUpdateTime(new Date());
                numberResourceMapper.updateById(resource);
            }
            
            return true;
        } else {
            log.error("Application cancellation failed: Number={}, Approval ID={}", approval.getNumber(), approvalId);
            return false;
        }
    }

    @Override
    public NumberApproval getDetail(Long approvalId) {
        if (approvalId == null) {
            return null;
        }
        
        return this.getById(approvalId);
    }

    @Override
    public NumberApproval getByApprovalNo(String approvalNo) {
        if (StringUtils.isBlank(approvalNo)) {
            return null;
        }
        
        LambdaQueryWrapper<NumberApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberApproval::getApprovalNo, approvalNo);
        
        return this.getOne(queryWrapper);
    }
    
    /**
     * 生成审批编号
     * 格式：NA + 年月日 + 6位随机数
     */
    private String generateApprovalNo() {
        String prefix = "NA";
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String random = String.format("%06d", (int)(Math.random() * 1000000));
        
        return prefix + date + random;
    }
}