package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.msisdn.entity.NumberOperationLog;
import com.nsrs.msisdn.mapper.NumberOperationLogMapper;
import com.nsrs.msisdn.service.NumberOperationLogService;
import com.nsrs.common.enums.ResultStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 号码操作日志服务实现类
 */
@Slf4j
@Service
public class NumberOperationLogServiceImpl extends ServiceImpl<NumberOperationLogMapper, NumberOperationLog> implements NumberOperationLogService {

    @Override
    public Page<NumberOperationLog> pageList(Page<NumberOperationLog> page, String number, Integer numberType, 
                                           Integer operationType, Long operatorUserId, 
                                           Date beginTime, Date endTime) {
        // 构建查询条件
        LambdaQueryWrapper<NumberOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(number)) {
            queryWrapper.like(NumberOperationLog::getNumber, number);
        }
        
        if (numberType != null) {
            queryWrapper.eq(NumberOperationLog::getNumberType, numberType);
        }
        
        if (operationType != null) {
            queryWrapper.eq(NumberOperationLog::getOperationType, operationType);
        }
        
        if (operatorUserId != null) {
            queryWrapper.eq(NumberOperationLog::getOperatorUserId, operatorUserId);
        }
        
        // 时间范围查询
        if (beginTime != null) {
            queryWrapper.ge(NumberOperationLog::getOperationTime, beginTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(NumberOperationLog::getOperationTime, endTime);
        }
        
        // 默认按操作时间降序排序
        queryWrapper.orderByDesc(NumberOperationLog::getOperationTime);
        
        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public List<NumberOperationLog> listByNumberId(Long numberId) {
        if (numberId == null) {
            return null;
        }
        
        LambdaQueryWrapper<NumberOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberOperationLog::getNumberId, numberId)
                    .orderByDesc(NumberOperationLog::getOperationTime);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public List<NumberOperationLog> listByNumber(String number) {
        if (StringUtils.isBlank(number)) {
            return null;
        }
        
        LambdaQueryWrapper<NumberOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberOperationLog::getNumber, number)
                    .orderByDesc(NumberOperationLog::getOperationTime);
        
        return this.list(queryWrapper);
    }
    
    @Override
    public Page<NumberOperationLog> pageByNumber(Page<NumberOperationLog> page, String number) {
        if (StringUtils.isBlank(number)) {
            return page;
        }
        
        LambdaQueryWrapper<NumberOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberOperationLog::getNumber, number)
                    .orderByDesc(NumberOperationLog::getOperationTime);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public Long recordLog(Long numberId, String number, Integer numberType, Integer operationType, 
                         Integer oldStatus, Integer newStatus, Long operatorUserId, 
                         BigDecimal charge, String orgName, Integer resultStatus, 
                         String remark) {
        // 创建日志对象
        NumberOperationLog log = new NumberOperationLog();
        log.setNumberId(numberId)
           .setNumber(number)
           .setNumberType(numberType)
           .setOperationType(operationType)
           .setOldStatus(oldStatus)
           .setNewStatus(newStatus)
           .setOperationTime(new Date())
           .setOperatorUserId(operatorUserId)
           .setCharge(charge)
           .setOrgName(orgName)
           .setResultStatus(resultStatus)
           .setRemark(remark);
        
        // 保存日志
        boolean success = this.save(log);
        
        if (success) {
            log.info("Number operation log recorded successfully: Number={}, Operation Type={}, Result={}", 
                    number, operationType, resultStatus);
            return log.getId();
        } else {
            log.error("Failed to record number operation log: Number={}, Operation Type={}", number, operationType);
            return null;
        }
    }

    @Override
    public Map<String, Object> getStatistics(Date beginTime, Date endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // 构建查询条件
        LambdaQueryWrapper<NumberOperationLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加时间范围条件
        if (beginTime != null) {
            queryWrapper.ge(NumberOperationLog::getOperationTime, beginTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(NumberOperationLog::getOperationTime, endTime);
        }
        
        // 查询总记录数
        long totalCount = this.count(queryWrapper);
        result.put("totalCount", totalCount);
        
        // 查询成功操作数
        LambdaQueryWrapper<NumberOperationLog> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.ge(beginTime != null, NumberOperationLog::getOperationTime, beginTime)
                     .le(endTime != null, NumberOperationLog::getOperationTime, endTime)
                     .eq(NumberOperationLog::getResultStatus, ResultStatusEnum.SUCCESS.getCode());
        long successCount = this.count(successWrapper);
        result.put("successCount", successCount);
        
        // 查询失败操作数
        LambdaQueryWrapper<NumberOperationLog> failWrapper = new LambdaQueryWrapper<>();
        failWrapper.ge(beginTime != null, NumberOperationLog::getOperationTime, beginTime)
                  .le(endTime != null, NumberOperationLog::getOperationTime, endTime)
                  .eq(NumberOperationLog::getResultStatus, ResultStatusEnum.FAILED.getCode());
        long failCount = this.count(failWrapper);
        result.put("failCount", failCount);
        
        // 查询各操作类型数量
        for (int i = 1; i <= 8; i++) {
            LambdaQueryWrapper<NumberOperationLog> typeWrapper = new LambdaQueryWrapper<>();
            typeWrapper.ge(beginTime != null, NumberOperationLog::getOperationTime, beginTime)
                      .le(endTime != null, NumberOperationLog::getOperationTime, endTime)
                      .eq(NumberOperationLog::getOperationType, i);
            long typeCount = this.count(typeWrapper);
            result.put("typeCount" + i, typeCount);
        }
        
        return result;
    }
}