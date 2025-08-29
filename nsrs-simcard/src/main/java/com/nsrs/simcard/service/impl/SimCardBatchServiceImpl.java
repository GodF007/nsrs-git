package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.constants.StatusConstants;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.entity.SimCardBatch;
import com.nsrs.simcard.mapper.SimCardBatchMapper;
import com.nsrs.simcard.mapper.SimCardMapper;
import com.nsrs.simcard.model.dto.SimCardBatchDTO;
import com.nsrs.simcard.model.query.SimCardBatchQuery;
import com.nsrs.simcard.service.SimCardBatchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SIM卡批次服务实现类
 */
@Service
public class SimCardBatchServiceImpl extends ServiceImpl<SimCardBatchMapper, SimCardBatch> implements SimCardBatchService {

    @Autowired
    private SimCardMapper simCardMapper;

    @Override
    public PageResult<SimCardBatchDTO> pageSimCardBatch(PageRequest<SimCardBatchQuery> request) {
        // 参数校验
        if (request == null || request.getQuery() == null) {
            throw new IllegalArgumentException("Request and query data cannot be null");
        }
        
        SimCardBatchQuery query = request.getQuery();
        
        // 构造查询条件
        LambdaQueryWrapper<SimCardBatch> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(query.getBatchCode())) {
            queryWrapper.like(SimCardBatch::getBatchCode, query.getBatchCode());
        }
        
        if (StringUtils.hasText(query.getBatchName())) {
            queryWrapper.like(SimCardBatch::getBatchName, query.getBatchName());
        }
        
        // 注意：organization_id和status字段已从数据库表中移除
        // 如果需要按组织或状态查询，需要重新设计查询逻辑
        
        // 处理预警状态查询
//        if (query.getAlertStatus() != null && query.getAlertStatus() == 1) {
//            // 查询预警状态的批次，即可用数量低于阈值的批次
//            queryWrapper.apply("(available_count * 100 / total_count) <= alert_threshold");
//        }
        
        // 处理日期范围查询
        if (StringUtils.hasText(query.getBeginDate())) {
            queryWrapper.ge(SimCardBatch::getCreateTime, query.getBeginDate());
        }
        
        if (StringUtils.hasText(query.getEndDate())) {
            queryWrapper.le(SimCardBatch::getCreateTime, query.getEndDate());
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(SimCardBatch::getCreateTime);
        
        // 执行分页查询
        Page<SimCardBatch> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<SimCardBatch> pageResult = this.page(page, queryWrapper);
        
        // 转换为DTO对象
        List<SimCardBatchDTO> batchDTOList = new ArrayList<>();
        for (SimCardBatch batch : pageResult.getRecords()) {
            SimCardBatchDTO dto = new SimCardBatchDTO();
            BeanUtils.copyProperties(batch, dto);
            
            // 注意：organizationId字段已从实体类中移除
            // dto.setOrgId(batch.getOrganizationId());
            
            // 设置可用数量
            dto.setAvailableCount(batch.getAvailableCount());
            
            batchDTOList.add(dto);
        }
        
        // 封装分页结果
        return new PageResult<>(
            batchDTOList, 
            pageResult.getTotal(), 
            pageResult.getCurrent(), 
            pageResult.getSize()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addSimCardBatch(SimCardBatchDTO batchDTO) {
        SimCardBatch batch = new SimCardBatch();
        BeanUtils.copyProperties(batchDTO, batch);
        
        // 注意：organizationId字段已从实体类中移除
        
        // 初始化各种数量
        batch.setActivatedCount(0);
        batch.setDeactivatedCount(0);
        batch.setRecycledCount(0);
        batch.setAvailableCount(batchDTO.getTotalCount());
        
        // 设置创建和更新时间
        Date now = new Date();
        batch.setCreateTime(now);
        batch.setUpdateTime(now);
        
        return this.save(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSimCardBatch(SimCardBatchDTO batchDTO) {
        // 获取原有批次信息
        SimCardBatch existingBatch = this.getById(batchDTO.getBatchId());
        if (existingBatch == null) {
            return false;
        }
        
        // 更新批次信息
        BeanUtils.copyProperties(batchDTO, existingBatch);
        
        // 注意：organizationId字段已从实体类中移除
        
        // 设置更新时间
        existingBatch.setUpdateTime(new Date());
        
        return this.updateById(existingBatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSimCardBatch(Long id) {
        // 检查批次下是否有SIM卡
        LambdaQueryWrapper<SimCard> cardQuery = new LambdaQueryWrapper<>();
        cardQuery.eq(SimCard::getBatchId, id);
        long count = simCardMapper.selectCount(cardQuery);
        
        // 如果批次下有SIM卡，则不允许删除
        if (count > 0) {
            return false;
        }
        
        return this.removeById(id);
    }

    @Override
    public SimCardBatchDTO getSimCardBatchDetail(Long id) {
        SimCardBatch batch = this.getById(id);
        if (batch == null) {
            return null;
        }
        
        SimCardBatchDTO dto = new SimCardBatchDTO();
        BeanUtils.copyProperties(batch, dto);
        
        // 注意：organizationId字段已从实体类中移除
        // dto.setOrgId(batch.getOrganizationId());
        
        // 设置可用数量
        dto.setAvailableCount(batch.getAvailableCount());
        
        return dto;
    }

    @Override
    public List<SimCardBatch> listAllBatches() {
        LambdaQueryWrapper<SimCardBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SimCardBatch::getCreateTime);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBatchCardCount(Long batchId) {
        // 统计批次下的SIM卡数量
        LambdaQueryWrapper<SimCard> cardQuery = new LambdaQueryWrapper<>();
        cardQuery.eq(SimCard::getBatchId, batchId);
        long totalCount = simCardMapper.selectCount(cardQuery);
        
        // 统计已激活的SIM卡数量
        LambdaQueryWrapper<SimCard> activatedQuery = new LambdaQueryWrapper<>();
        activatedQuery.eq(SimCard::getBatchId, batchId);
        activatedQuery.eq(SimCard::getStatus, 1); // Published status
        long activatedCount = simCardMapper.selectCount(activatedQuery);
        
        // 统计已停用的SIM卡数量
        LambdaQueryWrapper<SimCard> deactivatedQuery = new LambdaQueryWrapper<>();
        deactivatedQuery.eq(SimCard::getStatus, StatusConstants.SIMCARD_STATUS_ASSIGNED); // Assigned status
        deactivatedQuery.eq(SimCard::getStatus, 2); // Assigned status
        long deactivatedCount = simCardMapper.selectCount(deactivatedQuery);
        
        // 统计已回收的SIM卡数量
        LambdaQueryWrapper<SimCard> recycledQuery = new LambdaQueryWrapper<>();
        recycledQuery.eq(SimCard::getStatus, StatusConstants.SIMCARD_STATUS_ACTIVATED); // Activated status
        recycledQuery.eq(SimCard::getStatus, 3); // Activated status
        long recycledCount = simCardMapper.selectCount(recycledQuery);
        
        // 计算可用数量
        int availableCount = (int) (totalCount - activatedCount - deactivatedCount - recycledCount);
        
        // 更新批次统计数据
        SimCardBatch batch = this.getById(batchId);
        if (batch != null) {
            batch.setTotalCount((int) totalCount);
            batch.setActivatedCount((int) activatedCount);
            batch.setDeactivatedCount((int) deactivatedCount);
            batch.setRecycledCount((int) recycledCount);
            batch.setAvailableCount(availableCount);
            batch.setUpdateTime(new Date());
            
            this.updateById(batch);
        }
    }
    
    @Override
    public boolean checkStockAlert(Long batchId) {
        SimCardBatch batch = this.getById(batchId);
        if (batch == null || batch.getTotalCount() == 0) {
            return false;
        }
        
        // 计算可用百分比
        int availablePercentage = batch.getAvailableCount() * 100 / batch.getTotalCount();
        
        // 检查是否达到预警阈值（可用数量低于阈值时预警）
        // 注意：这里需要根据实际业务逻辑调整，假设alertThreshold字段存在
        // return availablePercentage <= batch.getAlertThreshold();
        return availablePercentage <= 20; // Temporary threshold of 20%
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvailableCount(Long batchId, int count) {
        SimCardBatch batch = this.getById(batchId);
        if (batch != null) {
            int newAvailableCount = batch.getAvailableCount() + count;
            
            // Ensure available count is not less than 0 and not greater than total
        if (newAvailableCount < 0) {
             newAvailableCount = 0;
        } else if (newAvailableCount > batch.getTotalCount()) {
                newAvailableCount = batch.getTotalCount();
            }
            
            batch.setAvailableCount(newAvailableCount);
            batch.setUpdateTime(new Date());
            
            this.updateById(batch);
        }
    }
}