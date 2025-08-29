package com.nsrs.binding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.binding.entity.BatchBindingDetail;
import com.nsrs.binding.mapper.BatchBindingDetailMapper;
import com.nsrs.binding.query.BatchBindingDetailQuery;
import com.nsrs.binding.service.BatchBindingDetailService;
import com.nsrs.binding.constants.BindingConstants;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Batch binding detail service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchBindingDetailServiceImpl extends ServiceImpl<BatchBindingDetailMapper, BatchBindingDetail> implements BatchBindingDetailService {

    @Override
    public PageResult<BatchBindingDetail> page(PageRequest<BatchBindingDetailQuery> request) {
        LambdaQueryWrapper<BatchBindingDetail> queryWrapper = new LambdaQueryWrapper<>();
        
        BatchBindingDetailQuery query = request.getQuery();
        if (query != null) {
            // 任务ID查询条件
            if (query.getTaskId() != null) {
                queryWrapper.eq(BatchBindingDetail::getTaskId, query.getTaskId());
            }
            
            // 状态查询条件
            if (query.getStatus() != null) {
                queryWrapper.eq(BatchBindingDetail::getStatus, query.getStatus());
            }
            
            // 号码查询条件
            if (StringUtils.hasText(query.getNumber())) {
                queryWrapper.eq(BatchBindingDetail::getNumber, query.getNumber());
            }
            
            // IMSI查询条件
            if (StringUtils.hasText(query.getImsi())) {
                queryWrapper.eq(BatchBindingDetail::getImsi, query.getImsi());
            }
            
            // 错误信息查询条件
            if (StringUtils.hasText(query.getErrorMsg())) {
                queryWrapper.like(BatchBindingDetail::getErrorMsg, query.getErrorMsg());
            }
        }
        
        // 默认按创建时间倒序
        queryWrapper.orderByDesc(BatchBindingDetail::getCreateTime);
        
        // 执行分页查询
        Page<BatchBindingDetail> page = new Page<>(request.getCurrent(), request.getSize());
        Page<BatchBindingDetail> pageResult = this.page(page, queryWrapper);
        
        // 构建返回结果
        PageResult<BatchBindingDetail> result = new PageResult<>();
        result.setList(pageResult.getRecords());
        result.setTotal(pageResult.getTotal());
        result.setPageNum(request.getCurrent());
        result.setPageSize(request.getSize());
        result.setPages((pageResult.getTotal() + request.getSize() - 1) / request.getSize());
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> batchCreate(Long taskId, List<BatchBindingDetail> details) {
        if (taskId == null || CollectionUtils.isEmpty(details)) {
            return CommonResult.failed(BindingConstants.ErrorMessage.INVALID_PARAMETERS);
        }
        
        // Set task ID and creation time
        Date now = new Date();
        for (BatchBindingDetail detail : details) {
            detail.setTaskId(taskId);
            detail.setStatus(BindingConstants.ProcessStatus.PENDING); // Pending
            detail.setCreateTime(now);
            detail.setUpdateTime(now);
        }
        
        // Batch save
        boolean saveResult = this.saveBatch(details);
        if (!saveResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.BATCH_CREATE_DETAILS_FAILED);
        }
        
        return CommonResult.success(details.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> updateStatus(Long detailId, Integer status, String errorMsg) {
        if (detailId == null || status == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.INVALID_PARAMETERS);
        }
        
        // Query detail
        BatchBindingDetail detail = this.getById(detailId);
        if (detail == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.DETAIL_NOT_FOUND);
        }
        
        // Update status
        detail.setStatus(status);
        detail.setErrorMsg(errorMsg);
        detail.setProcessTime(new Date());
        detail.setUpdateTime(new Date());
        
        // Save update
        boolean updateResult = this.updateById(detail);
        if (!updateResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.UPDATE_STATUS_FAILED);
        }
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> batchUpdateStatus(List<Long> detailIds, Integer status) {
        if (CollectionUtils.isEmpty(detailIds) || status == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.INVALID_PARAMETERS);
        }
        
        // Batch update status
        List<BatchBindingDetail> details = detailIds.stream().map(id -> {
            BatchBindingDetail detail = new BatchBindingDetail();
            detail.setDetailId(id);
            detail.setStatus(status);
            detail.setProcessTime(new Date());
            detail.setUpdateTime(new Date());
            return detail;
        }).collect(Collectors.toList());
        
        boolean updateResult = this.updateBatchById(details);
        if (!updateResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.BATCH_UPDATE_STATUS_FAILED);
        }
        
        return CommonResult.success(detailIds.size());
    }

    @Override
    public Map<String, Integer> countStatus(Long taskId) {
        if (taskId == null) {
            return new HashMap<>();
        }
        
        // Query count for each status
        LambdaQueryWrapper<BatchBindingDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BatchBindingDetail::getTaskId, taskId)
                    .select(BatchBindingDetail::getStatus);
        
        List<BatchBindingDetail> details = this.list(queryWrapper);
        
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("totalCount", details.size());
        countMap.put("pendingCount", 0);
        countMap.put("successCount", 0);
        countMap.put("failCount", 0);
        
        // Count for each status
        for (BatchBindingDetail detail : details) {
            Integer status = detail.getStatus();
            if (status.equals(BindingConstants.ProcessStatus.PENDING)) {
                countMap.put("pendingCount", countMap.get("pendingCount") + 1);
            } else if (status.equals(BindingConstants.ProcessStatus.SUCCESS)) {
                countMap.put("successCount", countMap.get("successCount") + 1);
            } else if (status.equals(BindingConstants.ProcessStatus.FAILED)) {
                countMap.put("failCount", countMap.get("failCount") + 1);
            }
        }
        
        return countMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> deleteByTaskId(Long taskId) {
        if (taskId == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_ID_REQUIRED);
        }
        
        // Delete details
        LambdaQueryWrapper<BatchBindingDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BatchBindingDetail::getTaskId, taskId);
        
        int deleteCount = baseMapper.delete(queryWrapper);
        
        return CommonResult.success(deleteCount);
    }
}