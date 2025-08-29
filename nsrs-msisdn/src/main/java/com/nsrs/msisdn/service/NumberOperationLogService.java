package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.NumberOperationLog;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 号码操作日志服务接口
 */
public interface NumberOperationLogService extends IService<NumberOperationLog> {
    
    /**
     * 分页查询号码操作日志
     *
     * @param page 分页参数
     * @param number 号码
     * @param numberType 号码类型
     * @param operationType 操作类型
     * @param operatorUserId 操作用户ID
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<NumberOperationLog> pageList(Page<NumberOperationLog> page, String number, Integer numberType, 
                                     Integer operationType, Long operatorUserId, 
                                     Date beginTime, Date endTime);
    
    /**
     * 查询号码的操作日志
     *
     * @param numberId 号码ID
     * @return 操作日志列表
     */
    List<NumberOperationLog> listByNumberId(Long numberId);
    
    /**
     * 根据号码查询操作日志
     *
     * @param number 号码
     * @return 操作日志列表
     */
    List<NumberOperationLog> listByNumber(String number);
    
    /**
     * 根据号码分页查询操作日志
     *
     * @param page 分页参数
     * @param number 号码
     * @return 分页结果
     */
    Page<NumberOperationLog> pageByNumber(Page<NumberOperationLog> page, String number);
    
    /**
     * 记录号码操作日志
     *
     * @param numberId 号码ID
     * @param number 号码
     * @param numberType 号码类型
     * @param operationType 操作类型
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param operatorUserId 操作用户ID
     * @param charge 费用
     * @param orgName 组织名称
     * @param resultStatus 操作结果状态
     * @param remark 备注
     * @return 日志ID
     */
    Long recordLog(Long numberId, String number, Integer numberType, Integer operationType, 
                  Integer oldStatus, Integer newStatus, Long operatorUserId, 
                  java.math.BigDecimal charge, String orgName, Integer resultStatus, 
                  String remark);
    
    /**
     * 获取操作日志统计信息
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getStatistics(Date beginTime, Date endTime);
}