package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.SimCardBatch;
import com.nsrs.simcard.model.dto.SimCardBatchDTO;
import com.nsrs.simcard.model.query.SimCardBatchQuery;

import java.util.List;

/**
 * SIM卡批次服务接口
 */
public interface SimCardBatchService extends IService<SimCardBatch> {
    
    /**
     * 分页查询SIM卡批次
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<SimCardBatchDTO> pageSimCardBatch(PageRequest<SimCardBatchQuery> request);
    
    /**
     * 添加SIM卡批次
     * @param batchDTO 批次信息
     * @return 操作结果
     */
    boolean addSimCardBatch(SimCardBatchDTO batchDTO);
    
    /**
     * 修改SIM卡批次
     * @param batchDTO 批次信息
     * @return 操作结果
     */
    boolean updateSimCardBatch(SimCardBatchDTO batchDTO);
    
    /**
     * 删除SIM卡批次
     * @param id 批次ID
     * @return 操作结果
     */
    boolean deleteSimCardBatch(Long id);
    
    /**
     * 获取SIM卡批次详情
     * @param id 批次ID
     * @return 批次详情
     */
    SimCardBatchDTO getSimCardBatchDetail(Long id);
    
    /**
     * 获取所有批次列表
     * @return 批次列表
     */
    List<SimCardBatch> listAllBatches();
    
    /**
     * 更新批次卡片数量统计
     * @param batchId 批次ID
     */
    void updateBatchCardCount(Long batchId);
    
    /**
     * 检查库存是否达到预警阈值
     * @param batchId 批次ID
     * @return 是否达到预警阈值
     */
    boolean checkStockAlert(Long batchId);
    
    /**
     * 更新批次可用数量
     * @param batchId 批次ID
     * @param count 增加的数量（可为负数）
     */
    void updateAvailableCount(Long batchId, int count);
}