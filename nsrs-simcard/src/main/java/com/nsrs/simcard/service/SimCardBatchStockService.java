package com.nsrs.simcard.service;

/**
 * SIM卡批次库存状态服务接口
 */
public interface SimCardBatchStockService {

    /**
     * 更新批次库存状态
     *
     * @param batchId 批次ID
     */
    void updateBatchStock(Long batchId);

    /**
     * 增加已激活数量
     *
     * @param batchId 批次ID
     * @param count 增加数量
     */
    void increaseActivatedCount(Long batchId, int count);

    /**
     * 减少已激活数量
     *
     * @param batchId 批次ID
     * @param count 减少数量
     */
    void decreaseActivatedCount(Long batchId, int count);

    /**
     * 增加已停用数量
     *
     * @param batchId 批次ID
     * @param count 增加数量
     */
    void increaseDeactivatedCount(Long batchId, int count);

    /**
     * 减少已停用数量
     *
     * @param batchId 批次ID
     * @param count 减少数量
     */
    void decreaseDeactivatedCount(Long batchId, int count);

    /**
     * 增加已回收数量
     *
     * @param batchId 批次ID
     * @param count 增加数量
     */
    void increaseRecycledCount(Long batchId, int count);

    /**
     * 减少已回收数量
     *
     * @param batchId 批次ID
     * @param count 减少数量
     */
    void decreaseRecycledCount(Long batchId, int count);

    /**
     * 更新可用数量
     *
     * @param batchId 批次ID
     */
    void updateAvailableCount(Long batchId);

    /**
     * 根据状态变化更新库存
     *
     * @param batchId 批次ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param count 数量
     */
    void updateStockByStatusChange(Long batchId, Integer oldStatus, Integer newStatus, int count);

    /**
     * 导入后更新库存统计
     *
     * @param batchId 批次ID
     * @param importCount 导入数量
     */
    void updateStockAfterImport(Long batchId, int importCount);
}