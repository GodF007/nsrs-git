package com.nsrs.simcard.service;

/**
 * IMSI组库存状态服务接口
 */
public interface ImsiGroupStockService {
    
    /**
     * 更新IMSI组库存状态
     * 根据IMSI资源状态统计更新组的总数、已使用数和可用数
     * 
     * @param groupId 组ID
     * @return 是否更新成功
     */
    boolean updateGroupStock(Long groupId);
    
    /**
     * 增加已使用数量
     * 
     * @param groupId 组ID
     * @param count 增加数量
     * @return 是否更新成功
     */
    boolean increaseUsedCount(Long groupId, Integer count);
    
    /**
     * 减少已使用数量
     * 
     * @param groupId 组ID
     * @param count 减少数量
     * @return 是否更新成功
     */
    boolean decreaseUsedCount(Long groupId, Integer count);
    
    /**
     * 根据状态变化更新库存
     * 
     * @param groupId 组ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param count 数量
     * @return 是否更新成功
     */
    boolean updateStockByStatusChange(Long groupId, Integer oldStatus, Integer newStatus, Integer count);
}