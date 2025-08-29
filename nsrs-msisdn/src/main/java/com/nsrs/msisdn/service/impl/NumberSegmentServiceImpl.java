package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.entity.NumberSegment;
import com.nsrs.msisdn.mapper.NumberResourceMapper;
import com.nsrs.msisdn.mapper.NumberSegmentMapper;
import com.nsrs.msisdn.service.NumberSegmentService;
import com.nsrs.common.enums.NumberStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 号码段服务实现类
 */
@Slf4j
@Service
public class NumberSegmentServiceImpl extends ServiceImpl<NumberSegmentMapper, NumberSegment> implements NumberSegmentService {

    @Autowired
    private NumberResourceMapper numberResourceMapper;

    @Override
    public IPage<NumberSegment> pageList(Page<NumberSegment> page, String segmentCode, Integer segmentType, 
                                  Long regionId, Long hlrSwitchId, Integer status) {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(segmentCode)) {
            queryWrapper.like(NumberSegment::getSegmentCode, segmentCode);
        }
        if (segmentType != null) {
            queryWrapper.eq(NumberSegment::getSegmentType, segmentType);
        }
        if (regionId != null) {
            queryWrapper.eq(NumberSegment::getRegionId, regionId);
        }
        if (hlrSwitchId != null) {
            queryWrapper.eq(NumberSegment::getHlrSwitchId, hlrSwitchId);
        }
        if (status != null) {
            queryWrapper.eq(NumberSegment::getStatus, status);
        }
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        return page(page, queryWrapper);
    }

    @Override
    public NumberSegment getDetail(Long segmentId) {
        return getById(segmentId);
    }

    @Override
    public NumberSegment getBySegmentCode(String segmentCode) {
        if (StringUtils.isBlank(segmentCode)) {
            return null;
        }
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getSegmentCode, segmentCode);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(NumberSegment numberSegment) {
        // Check if segment code already exists
        NumberSegment existSegment = getBySegmentCode(numberSegment.getSegmentCode());
        if (existSegment != null) {
            throw new RuntimeException("Number segment code already exists");
        }
        
        // Check start number and end number
        String startNumber = numberSegment.getStartNumber();
        String endNumber = numberSegment.getEndNumber();
        if (startNumber.compareTo(endNumber) > 0) {
            throw new RuntimeException("Start number cannot be greater than end number");
        }
        
        // Calculate total quantity
        // Simplified calculation, specific implementation may need adjustment based on number format
        long start = Long.parseLong(startNumber);
        long end = Long.parseLong(endNumber);
        Long totalQty = end - start + 1;
        
        numberSegment.setTotalQty(totalQty);
        numberSegment.setIdleQty(totalQty); // Initially all are idle
        numberSegment.setActivatedQty(0L);
        numberSegment.setFrozenQty(0L);
        numberSegment.setBlockedQty(0L);

        numberSegment.setReservedQty(0L);
        
        numberSegment.setCreateTime(new Date());
        numberSegment.setUpdateTime(new Date());
        return save(numberSegment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(NumberSegment numberSegment) {
        // Check if segment code already exists (exclude self)
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getSegmentCode, numberSegment.getSegmentCode())
                   .ne(NumberSegment::getSegmentId, numberSegment.getSegmentId());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("Number segment code already exists");
        }
        
        // Do not allow modification of start number and end number
        NumberSegment oldSegment = getById(numberSegment.getSegmentId());
        if (oldSegment == null) {
            return false;
        }
        
        // Keep original number range and quantity information
        numberSegment.setStartNumber(oldSegment.getStartNumber());
        numberSegment.setEndNumber(oldSegment.getEndNumber());
        numberSegment.setTotalQty(oldSegment.getTotalQty());
        numberSegment.setIdleQty(oldSegment.getIdleQty());
        numberSegment.setActivatedQty(oldSegment.getActivatedQty());
        numberSegment.setFrozenQty(oldSegment.getFrozenQty());
        numberSegment.setBlockedQty(oldSegment.getBlockedQty());

        numberSegment.setReservedQty(oldSegment.getReservedQty());
        
        numberSegment.setUpdateTime(new Date());
        return updateById(numberSegment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long segmentId) {
        // Check if there are number resources associated with this segment
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        long count = numberResourceMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("Cannot delete segment with existing number resources");
        }
        
        return removeById(segmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long segmentId) {
        NumberSegment numberSegment = getById(segmentId);
        if (numberSegment == null) {
            return false;
        }
        numberSegment.setStatus(1); // Enable
        numberSegment.setUpdateTime(new Date());
        return updateById(numberSegment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long segmentId) {
        NumberSegment numberSegment = getById(segmentId);
        if (numberSegment == null) {
            return false;
        }
        numberSegment.setStatus(0); // Disable
        numberSegment.setUpdateTime(new Date());
        return updateById(numberSegment);
    }

    @Override
    public List<NumberSegment> listByType(Integer segmentType) {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getSegmentType, segmentType);
        queryWrapper.eq(NumberSegment::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        return list(queryWrapper);
    }

    @Override
    public List<NumberSegment> listByRegionId(Long regionId) {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getRegionId, regionId);
        queryWrapper.eq(NumberSegment::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        return list(queryWrapper);
    }

    @Override
    public List<NumberSegment> listByHlrSwitchId(Long hlrSwitchId) {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getHlrSwitchId, hlrSwitchId);
        queryWrapper.eq(NumberSegment::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        return list(queryWrapper);
    }

    @Override
    public List<NumberSegment> listByConditions(Integer segmentType, Long regionId, Long hlrSwitchId) {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据传入的条件动态构建查询条件
        if (segmentType != null) {
            queryWrapper.eq(NumberSegment::getSegmentType, segmentType);
        }
        if (regionId != null) {
            queryWrapper.eq(NumberSegment::getRegionId, regionId);
        }
        if (hlrSwitchId != null) {
            queryWrapper.eq(NumberSegment::getHlrSwitchId, hlrSwitchId);
        }
        
        // 只查询启用状态的号码段
        queryWrapper.eq(NumberSegment::getStatus, 1);
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSegmentStatusCount(Long segmentId) {
        NumberSegment segment = getById(segmentId);
        if (segment == null) {
            return false;
        }

        // 查询各状态号码数量
        LambdaQueryWrapper<NumberResource> queryWrapper = new LambdaQueryWrapper<>();
        
        // 空闲数量
        queryWrapper.clear();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        queryWrapper.eq(NumberResource::getStatus, NumberStatusEnum.IDLE.getCode());
        Long idleQty = numberResourceMapper.selectCount(queryWrapper);
        
        // 预留数量
        queryWrapper.clear();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        queryWrapper.eq(NumberResource::getStatus, NumberStatusEnum.RESERVED.getCode());
        Long reservedQty = numberResourceMapper.selectCount(queryWrapper);
        
        // 已激活数量
        queryWrapper.clear();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        queryWrapper.eq(NumberResource::getStatus, NumberStatusEnum.ACTIVATED.getCode());
        Long activatedQty = numberResourceMapper.selectCount(queryWrapper);
        
        // 已冻结数量
        queryWrapper.clear();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        queryWrapper.eq(NumberResource::getStatus, NumberStatusEnum.FROZEN.getCode());
        Long frozenQty = numberResourceMapper.selectCount(queryWrapper);
        
        // 已锁定数量
        queryWrapper.clear();
        queryWrapper.eq(NumberResource::getSegmentId, segmentId);
        queryWrapper.eq(NumberResource::getStatus, NumberStatusEnum.LOCKED.getCode());
        Long blockedQty = numberResourceMapper.selectCount(queryWrapper);
        
        // 释放后的号码状态为空闲，已包含在idleQty中，无需单独统计
        
        // Update segment status statistics
        segment.setIdleQty(idleQty);
        segment.setReservedQty(reservedQty);
        segment.setActivatedQty(activatedQty);
        segment.setFrozenQty(frozenQty);
        segment.setBlockedQty(blockedQty);

        segment.setUpdateTime(new Date());
        
        return updateById(segment);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Query total segment count
        statistics.put("totalSegments", count());
        
        // Query enabled segment count
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getStatus, 1);
        statistics.put("enabledSegments", count(queryWrapper));
        
        // Query segment count by type
        for (int i = 1; i <= 5; i++) {
            queryWrapper.clear();
            queryWrapper.eq(NumberSegment::getSegmentType, i);
            statistics.put("type" + i + "Segments", count(queryWrapper));
        }
        
        // Query total number count
        statistics.put("totalNumbers", baseMapper.sumTotalQty());
        
        // Query number count by status
        statistics.put("idleNumbers", baseMapper.sumIdleQty());
        statistics.put("reservedNumbers", baseMapper.sumReservedQty());
        statistics.put("activatedNumbers", baseMapper.sumActivatedQty());
        statistics.put("frozenNumbers", baseMapper.sumFrozenQty());
        statistics.put("blockedNumbers", baseMapper.sumBlockedQty());
        // 释放后的号码状态为空闲，已包含在空闲数量统计中
        
        return statistics;
    }

    @Override
    public List<NumberSegment> listEnabled() {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getStatus, 1);
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        return list(queryWrapper);
    }
    
    @Override
    public List<NumberSegment> listAllEnabled() {
        LambdaQueryWrapper<NumberSegment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NumberSegment::getStatus, 1);
        queryWrapper.orderByAsc(NumberSegment::getSegmentCode);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNumberCount(Long segmentId) {
        NumberSegment segment = getById(segmentId);
        if (segment == null) {
            return false;
        }

        // Recalculate total quantity
        String startNumber = segment.getStartNumber();
        String endNumber = segment.getEndNumber();
        long start = Long.parseLong(startNumber);
        long end = Long.parseLong(endNumber);
        Long totalQty = end - start + 1;
        
        segment.setTotalQty(totalQty);
        segment.setUpdateTime(new Date());
        
        return updateById(segment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementalUpdateStatistics(Long segmentId, Integer oldStatus, Integer newStatus) {
        if (segmentId == null) {
            return false;
        }
        
        NumberSegment segment = getById(segmentId);
        if (segment == null) {
            return false;
        }
        
        // If status hasn't changed, return directly
        if (oldStatus != null && oldStatus.equals(newStatus)) {
            return true;
        }
        
        // Decrease count for old status
        if (oldStatus != null) {
            decrementStatusCount(segment, oldStatus);
        }
        
        // Increase count for new status
        if (newStatus != null) {
            incrementStatusCount(segment, newStatus);
        }
        
        segment.setUpdateTime(new Date());
        return updateById(segment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchIncrementalUpdateStatistics(Long segmentId, Map<Integer, Integer> statusChanges) {
        if (segmentId == null || statusChanges == null || statusChanges.isEmpty()) {
            return false;
        }
        
        NumberSegment segment = getById(segmentId);
        if (segment == null) {
            return false;
        }
        
        // Batch update count for each status
        for (Map.Entry<Integer, Integer> entry : statusChanges.entrySet()) {
            Integer status = entry.getKey();
            Integer change = entry.getValue();
            
            if (change > 0) {
                // Increase count
                for (int i = 0; i < change; i++) {
                    incrementStatusCount(segment, status);
                }
            } else if (change < 0) {
                // Decrease count
                for (int i = 0; i < Math.abs(change); i++) {
                    decrementStatusCount(segment, status);
                }
            }
        }
        
        segment.setUpdateTime(new Date());
        return updateById(segment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetStatistics(Long segmentId) {
        // Reset statistics and recalculate
        return updateSegmentStatusCount(segmentId);
    }
    
    /**
     * 增加指定状态的数量
     */
    private void incrementStatusCount(NumberSegment segment, Integer status) {
        if (NumberStatusEnum.IDLE.getCode().equals(status)) {
            segment.setIdleQty(segment.getIdleQty() + 1);
        } else if (NumberStatusEnum.RESERVED.getCode().equals(status)) {
            segment.setReservedQty(segment.getReservedQty() + 1);
        } else if (NumberStatusEnum.ACTIVATED.getCode().equals(status)) {
            segment.setActivatedQty(segment.getActivatedQty() + 1);
        } else if (NumberStatusEnum.FROZEN.getCode().equals(status)) {
            segment.setFrozenQty(segment.getFrozenQty() + 1);
        } else if (NumberStatusEnum.LOCKED.getCode().equals(status)) {
            segment.setBlockedQty(segment.getBlockedQty() + 1);
        } else {
            // 对于ASSIGNED和IN_USE状态，暂时不在NumberSegment中单独统计
            // 可以根据业务需要后续扩展
            log.warn("Status {} not handled in segment statistics", status);
        }
    }
    
    /**
     * 减少指定状态的数量
     */
    private void decrementStatusCount(NumberSegment segment, Integer status) {
        if (NumberStatusEnum.IDLE.getCode().equals(status)) {
            segment.setIdleQty(Math.max(0, segment.getIdleQty() - 1));
        } else if (NumberStatusEnum.RESERVED.getCode().equals(status)) {
            segment.setReservedQty(Math.max(0, segment.getReservedQty() - 1));
        } else if (NumberStatusEnum.ACTIVATED.getCode().equals(status)) {
            segment.setActivatedQty(Math.max(0, segment.getActivatedQty() - 1));
        } else if (NumberStatusEnum.FROZEN.getCode().equals(status)) {
            segment.setFrozenQty(Math.max(0, segment.getFrozenQty() - 1));
        } else if (NumberStatusEnum.LOCKED.getCode().equals(status)) {
            segment.setBlockedQty(Math.max(0, segment.getBlockedQty() - 1));
        } else {
            // 对于ASSIGNED和IN_USE状态，暂时不在NumberSegment中单独统计
            // 可以根据业务需要后续扩展
            log.warn("Status {} not handled in segment statistics", status);
        }
    }
}