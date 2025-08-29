package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.SimCardInventoryAlert;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertDTO;
import com.nsrs.simcard.model.query.SimCardInventoryAlertQuery;

import java.util.List;

/**
 * SIM卡库存预警配置服务接口
 */
public interface SimCardInventoryAlertService extends IService<SimCardInventoryAlert> {
    
    /**
     * 分页查询库存预警配置
     * @param request 分页查询请求
     * @return 分页结果
     */
    PageResult<SimCardInventoryAlertDTO> pageAlert(PageRequest<SimCardInventoryAlertQuery> request);
    
    /**
     * 添加库存预警配置
     * @param alertDTO 预警配置信息
     * @return 操作结果
     */
    boolean addAlert(SimCardInventoryAlertDTO alertDTO);
    
    /**
     * 修改库存预警配置
     * @param alertDTO 预警配置信息
     * @return 操作结果
     */
    boolean updateAlert(SimCardInventoryAlertDTO alertDTO);
    
    /**
     * 删除库存预警配置
     * @param id 预警ID
     * @return 操作结果
     */
    boolean deleteAlert(Long id);
    
    /**
     * 获取库存预警配置详情
     * @param id 预警ID
     * @return 预警配置详情
     */
    SimCardInventoryAlertDTO getAlertDetail(Long id);
    
    /**
     * 修改库存预警配置状态
     * @param id 预警ID
     * @param isActive 状态：0-禁用，1-启用
     * @return 操作结果
     */
    boolean updateAlertStatus(Long id, Integer isActive);
    
    /**
     * 检查库存并生成预警日志
     * 定时任务调用此方法
     * @return 预警数量
     */
    int checkInventoryAndGenerateAlerts();
    
    /**
     * 获取所有启用的预警配置
     * @return 预警配置列表
     */
    List<SimCardInventoryAlertDTO> listActiveAlerts();
    
    /**
     * 清理过期缓存
     */
    void clearExpiredCache();
    
    /**
     * 清理所有缓存
     */
    void clearAllCache();
    
    /**
     * 手动触发库存检查和告警
     * 用于API接口调用
     * @return 操作结果信息
     */
    String triggerManualAlertCheck();
}