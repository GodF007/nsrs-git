package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nsrs.simcard.utils.SimCardConstant;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.entity.SimCardBatch;
import com.nsrs.simcard.service.SimCardBatchService;
import com.nsrs.simcard.service.SimCardBatchStockService;
import com.nsrs.simcard.service.SimCardService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * SIM卡批次库存状态服务实现类
 */
@Slf4j
@Service
public class SimCardBatchStockServiceImpl implements SimCardBatchStockService {



    @Autowired
    private SimCardBatchService simCardBatchService;

    @Lazy
    @Autowired
    private SimCardService simCardService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchStock(Long batchId) {
        if (batchId == null) {
            return;
        }

        try {
            SimCardBatch batch = simCardBatchService.getById(batchId);
            if (batch == null) {
                log.warn("Batch not found: batchId={}", batchId);
                return;
            }

            // 统计各状态的SIM卡数量
            LambdaQueryWrapper<SimCard> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SimCard::getBatchId, batchId);

            // 总数量
            long totalCount = simCardService.count(queryWrapper);

            // 已激活数量
            LambdaQueryWrapper<SimCard> activatedWrapper = new LambdaQueryWrapper<>();
            activatedWrapper.eq(SimCard::getBatchId, batchId)
                           .eq(SimCard::getStatus, SimCardConstant.STATUS_ACTIVATED);
            long activatedCount = simCardService.count(activatedWrapper);

            // 已停用数量
            LambdaQueryWrapper<SimCard> deactivatedWrapper = new LambdaQueryWrapper<>();
            deactivatedWrapper.eq(SimCard::getBatchId, batchId)
                             .eq(SimCard::getStatus, SimCardConstant.STATUS_DEACTIVATED);
            long deactivatedCount = simCardService.count(deactivatedWrapper);

            // 已回收数量
            LambdaQueryWrapper<SimCard> recycledWrapper = new LambdaQueryWrapper<>();
            recycledWrapper.eq(SimCard::getBatchId, batchId)
                          .eq(SimCard::getStatus, SimCardConstant.STATUS_RECYCLED);
            long recycledCount = simCardService.count(recycledWrapper);

            // 可用数量（已发布 + 已分配）
            LambdaQueryWrapper<SimCard> availableWrapper = new LambdaQueryWrapper<>();
            availableWrapper.eq(SimCard::getBatchId, batchId)
                           .in(SimCard::getStatus, SimCardConstant.STATUS_PUBLISHED, SimCardConstant.STATUS_ASSIGNED);
            long availableCount = simCardService.count(availableWrapper);

            // 更新批次库存信息
            batch.setTotalCount((int) totalCount);
            batch.setActivatedCount((int) activatedCount);
            batch.setDeactivatedCount((int) deactivatedCount);
            batch.setRecycledCount((int) recycledCount);
            batch.setAvailableCount((int) availableCount);
            batch.setUpdateTime(new Date());

            simCardBatchService.updateById(batch);

            log.info("Batch stock updated successfully: batchId={}, total={}, activated={}, deactivated={}, recycled={}, available={}", 
                    batchId, totalCount, activatedCount, deactivatedCount, recycledCount, availableCount);

        } catch (Exception e) {
            log.error("Failed to update batch stock: batchId={}", batchId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseActivatedCount(Long batchId, int count) {
        updateCountField(batchId, "activated", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseActivatedCount(Long batchId, int count) {
        updateCountField(batchId, "activated", -count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseDeactivatedCount(Long batchId, int count) {
        updateCountField(batchId, "deactivated", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseDeactivatedCount(Long batchId, int count) {
        updateCountField(batchId, "deactivated", -count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseRecycledCount(Long batchId, int count) {
        updateCountField(batchId, "recycled", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseRecycledCount(Long batchId, int count) {
        updateCountField(batchId, "recycled", -count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvailableCount(Long batchId) {
        if (batchId == null) {
            return;
        }

        try {
            SimCardBatch batch = simCardBatchService.getById(batchId);
            if (batch == null) {
                return;
            }

            // 可用数量 = 总数量 - 已激活 - 已停用 - 已回收
            int availableCount = batch.getTotalCount() - batch.getActivatedCount() 
                               - batch.getDeactivatedCount() - batch.getRecycledCount();
            
            if (availableCount < 0) {
                availableCount = 0;
            }

            batch.setAvailableCount(availableCount);
            batch.setUpdateTime(new Date());
            simCardBatchService.updateById(batch);

        } catch (Exception e) {
            log.error("Failed to update available count: batchId={}", batchId, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockByStatusChange(Long batchId, Integer oldStatus, Integer newStatus, int count) {
        if (batchId == null || oldStatus == null || newStatus == null || count <= 0) {
            return;
        }

        try {
            // 减少原状态的数量
            if (oldStatus.equals(SimCardConstant.STATUS_ACTIVATED)) {
                decreaseActivatedCount(batchId, count);
            } else if (oldStatus.equals(SimCardConstant.STATUS_DEACTIVATED)) {
                decreaseDeactivatedCount(batchId, count);
            } else if (oldStatus.equals(SimCardConstant.STATUS_RECYCLED)) {
                decreaseRecycledCount(batchId, count);
            }

            // 增加新状态的数量
            if (newStatus.equals(SimCardConstant.STATUS_ACTIVATED)) {
                increaseActivatedCount(batchId, count);
            } else if (newStatus.equals(SimCardConstant.STATUS_DEACTIVATED)) {
                increaseDeactivatedCount(batchId, count);
            } else if (newStatus.equals(SimCardConstant.STATUS_RECYCLED)) {
                increaseRecycledCount(batchId, count);
            }

            // 更新可用数量
            updateAvailableCount(batchId);

        } catch (Exception e) {
            log.error("Failed to update stock by status change: batchId={}, oldStatus={}, newStatus={}, count={}", 
                     batchId, oldStatus, newStatus, count, e);
            throw e;
        }
    }

    /**
     * 更新指定字段的数量
     *
     * @param batchId 批次ID
     * @param field 字段名称
     * @param delta 变化量
     */
    private void updateCountField(Long batchId, String field, int delta) {
        if (batchId == null || delta == 0) {
            return;
        }

        try {
            SimCardBatch batch = simCardBatchService.getById(batchId);
            if (batch == null) {
                return;
            }

            int newValue = 0;
            switch (field) {
                case "activated":
                    newValue = Math.max(0, batch.getActivatedCount() + delta);
                    batch.setActivatedCount(newValue);
                    break;
                case "deactivated":
                    newValue = Math.max(0, batch.getDeactivatedCount() + delta);
                    batch.setDeactivatedCount(newValue);
                    break;
                case "recycled":
                    newValue = Math.max(0, batch.getRecycledCount() + delta);
                    batch.setRecycledCount(newValue);
                    break;
                default:
                    log.warn("Unknown field name: {}", field);
                    return;
            }

            batch.setUpdateTime(new Date());
            simCardBatchService.updateById(batch);

            log.debug("Batch {} field updated successfully: batchId={}, delta={}, newValue={}", field, batchId, delta, newValue);

        } catch (Exception e) {
            log.error("Failed to update batch {} field: batchId={}, delta={}", field, batchId, delta, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockAfterImport(Long batchId, int importCount) {
        if (batchId == null || importCount <= 0) {
            return;
        }

        try {
            SimCardBatch batch = simCardBatchService.getById(batchId);
            if (batch == null) {
                log.warn("Batch not found: batchId={}", batchId);
                return;
            }

            // 增加总数量和可用数量
            batch.setTotalCount(batch.getTotalCount() + importCount);
            batch.setAvailableCount(batch.getAvailableCount() + importCount);
            batch.setUpdateTime(new Date());

            simCardBatchService.updateById(batch);

            log.info("Batch stock updated after import: batchId={}, importCount={}, newTotal={}, newAvailable={}", 
                    batchId, importCount, batch.getTotalCount(), batch.getAvailableCount());

        } catch (Exception e) {
            log.error("Failed to update stock after import: batchId={}, importCount={}", batchId, importCount, e);
            throw e;
        }
    }
}