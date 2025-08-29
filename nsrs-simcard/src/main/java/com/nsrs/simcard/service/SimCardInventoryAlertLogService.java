package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.SimCardInventoryAlertLog;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertLogDTO;
import com.nsrs.simcard.model.query.SimCardInventoryAlertLogQuery;

import java.util.List;

/**
 * SIM卡库存预警日志服务接口
 */
public interface SimCardInventoryAlertLogService extends IService<SimCardInventoryAlertLog> {
    
    /**
     * 分页查询库存预警日志
     * @param request 分页查询请求
     * @return 分页结果
     */
    PageResult<SimCardInventoryAlertLogDTO> pageAlertLog(PageRequest<SimCardInventoryAlertLogQuery> request);
    
    /**
     * 添加库存预警日志
     * @param logDTO 预警日志信息
     * @return 操作结果
     */
    boolean addAlertLog(SimCardInventoryAlertLogDTO logDTO);
    
    /**
     * 获取库存预警日志详情
     * @param id 日志ID
     * @return 预警日志详情
     */
    SimCardInventoryAlertLogDTO getAlertLogDetail(Long id);
    
    /**
     * 修改库存预警日志通知状态
     * @param id 日志ID
     * @param notifyStatus 通知状态：0-未通知，1-已通知
     * @return 操作结果
     */
    boolean updateNotifyStatus(Long id, Integer notifyStatus);
    
    /**
     * 获取指定预警ID的所有日志
     * @param alertId 预警ID
     * @return 预警日志列表
     */
    List<SimCardInventoryAlertLogDTO> getLogsByAlertId(Long alertId);
    
    /**
     * 统计未通知的预警日志数量
     * @return 未通知的预警日志数量
     */
    int countUnnotifiedLogs();
    
    /**
     * 获取最近的预警日志
     * @param limit 数量限制
     * @return 预警日志列表
     */
    List<SimCardInventoryAlertLogDTO> getRecentLogs(int limit);
    
    /**
     * 获取待处理的通知日志
     * @param limit 数量限制
     * @return 待处理的预警日志列表
     */
    List<SimCardInventoryAlertLogDTO> getPendingNotificationLogs(int limit);
    
    /**
     * 获取失败的通知日志
     * @param limit 数量限制
     * @return 失败的预警日志列表
     */
    List<SimCardInventoryAlertLogDTO> getFailedNotificationLogs(int limit);
}