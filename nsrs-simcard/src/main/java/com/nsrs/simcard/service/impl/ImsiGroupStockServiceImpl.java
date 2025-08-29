package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nsrs.simcard.entity.ImsiGroup;
import com.nsrs.simcard.entity.ImsiResource;
import com.nsrs.simcard.service.ImsiGroupService;
import com.nsrs.simcard.service.ImsiGroupStockService;
import com.nsrs.simcard.service.ImsiResourceService;
import com.nsrs.simcard.utils.ImsiConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * IMSI组库存状态服务实现类
 */
@Slf4j
@Service
public class ImsiGroupStockServiceImpl implements ImsiGroupStockService {
    
    @Autowired
    private ImsiGroupService imsiGroupService;
    
    @Autowired
    private ImsiResourceService imsiResourceService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGroupStock(Long groupId) {
        if (groupId == null) {
            return false;
        }
        
        try {
            ImsiGroup imsiGroup = imsiGroupService.getById(groupId);
            if (imsiGroup == null) {
                log.warn("IMSI group not found, groupId: {}", groupId);
                return false;
            }
            
            // 统计各状态的IMSI数量
            LambdaQueryWrapper<ImsiResource> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImsiResource::getGroupId, groupId);
            
            // 统计总数
            long totalCount = imsiResourceService.count(queryWrapper);
            
            // 统计已使用数量（状态为已绑定、已使用、已锁定的都算已使用）
            LambdaQueryWrapper<ImsiResource> usedWrapper = new LambdaQueryWrapper<>();
            usedWrapper.eq(ImsiResource::getGroupId, groupId)
                      .in(ImsiResource::getStatus, ImsiConstant.STATUS_BOUND, 
                          ImsiConstant.STATUS_USED, ImsiConstant.STATUS_LOCKED);
            long usedCount = imsiResourceService.count(usedWrapper);
            
            // 更新IMSI组信息
            imsiGroup.setTotalCount((int) totalCount);
            imsiGroup.setUsedCount((int) usedCount);
            imsiGroup.setUpdateTime(new Date());
            
            boolean result = imsiGroupService.updateById(imsiGroup);
            
            if (result) {
                log.info("IMSI group stock updated successfully, groupId: {}, totalCount: {}, usedCount: {}", 
                        groupId, totalCount, usedCount);
            } else {
                log.error("Failed to update IMSI group stock, groupId: {}", groupId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Exception occurred while updating IMSI group stock, groupId: {}", groupId, e);
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseUsedCount(Long groupId, Integer count) {
        if (groupId == null || count == null || count <= 0) {
            return false;
        }
        
        try {
            ImsiGroup imsiGroup = imsiGroupService.getById(groupId);
            if (imsiGroup == null) {
                log.warn("IMSI group not found, groupId: {}", groupId);
                return false;
            }
            
            imsiGroup.setUsedCount(imsiGroup.getUsedCount() + count);
            imsiGroup.setUpdateTime(new Date());
            
            boolean result = imsiGroupService.updateById(imsiGroup);
            
            if (result) {
                log.info("IMSI group used count increased successfully, groupId: {}, count: {}, newUsedCount: {}", 
                        groupId, count, imsiGroup.getUsedCount());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Exception occurred while increasing IMSI group used count, groupId: {}, count: {}", groupId, count, e);
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseUsedCount(Long groupId, Integer count) {
        if (groupId == null || count == null || count <= 0) {
            return false;
        }
        
        try {
            ImsiGroup imsiGroup = imsiGroupService.getById(groupId);
            if (imsiGroup == null) {
                log.warn("IMSI group does not exist, groupId: {}", groupId);
                return false;
            }
            
            int newUsedCount = Math.max(0, imsiGroup.getUsedCount() - count);
            imsiGroup.setUsedCount(newUsedCount);
            imsiGroup.setUpdateTime(new Date());
            
            boolean result = imsiGroupService.updateById(imsiGroup);
            
            if (result) {
                log.info("IMSI group used count decreased successfully, groupId: {}, count: {}, newUsedCount: {}", 
                        groupId, count, newUsedCount);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Exception occurred while decreasing IMSI group used count, groupId: {}, count: {}", groupId, count, e);
            throw e;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStockByStatusChange(Long groupId, Integer oldStatus, Integer newStatus, Integer count) {
        if (groupId == null || count == null || count <= 0) {
            return false;
        }
        
        try {
            // 判断状态变化对已使用数量的影响
            boolean oldStatusIsUsed = isUsedStatus(oldStatus);
            boolean newStatusIsUsed = isUsedStatus(newStatus);
            
            if (oldStatusIsUsed && !newStatusIsUsed) {
                // 从已使用状态变为空闲状态，减少已使用数量
                return decreaseUsedCount(groupId, count);
            } else if (!oldStatusIsUsed && newStatusIsUsed) {
                // 从空闲状态变为已使用状态，增加已使用数量
                return increaseUsedCount(groupId, count);
            }
            
            // 状态变化不影响已使用数量统计
            return true;
            
        } catch (Exception e) {
            log.error("Exception occurred while updating IMSI group stock by status change, groupId: {}, oldStatus: {}, newStatus: {}, count: {}", 
                    groupId, oldStatus, newStatus, count, e);
            throw e;
        }
    }
    
    /**
     * 判断状态是否为已使用状态
     * 
     * @param status 状态
     * @return 是否为已使用状态
     */
    private boolean isUsedStatus(Integer status) {
        if (status == null) {
            return false;
        }
        
        return status.equals(ImsiConstant.STATUS_BOUND) || 
               status.equals(ImsiConstant.STATUS_USED) || 
               status.equals(ImsiConstant.STATUS_LOCKED);
    }
}