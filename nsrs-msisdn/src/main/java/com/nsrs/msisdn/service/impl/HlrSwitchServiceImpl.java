package com.nsrs.msisdn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.msisdn.entity.HlrSwitch;
import com.nsrs.msisdn.entity.NumberSegment;
import com.nsrs.msisdn.mapper.HlrSwitchMapper;
import com.nsrs.msisdn.mapper.NumberSegmentMapper;
import com.nsrs.msisdn.service.HlrSwitchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * HLR/交换机服务实现类
 */
@Service
public class HlrSwitchServiceImpl extends ServiceImpl<HlrSwitchMapper, HlrSwitch> implements HlrSwitchService {

    @Autowired
    private NumberSegmentMapper numberSegmentMapper;

    @Override
    public IPage<HlrSwitch> pageList(Page<HlrSwitch> page, String hlrName, String hlrCode, Integer hlrType, Integer status) {
        LambdaQueryWrapper<HlrSwitch> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(hlrName)) {
            queryWrapper.like(HlrSwitch::getHlrName, hlrName);
        }
        if (StringUtils.isNotBlank(hlrCode)) {
            queryWrapper.like(HlrSwitch::getHlrCode, hlrCode);
        }
        if (hlrType != null) {
            queryWrapper.eq(HlrSwitch::getHlrType, hlrType);
        }
        if (status != null) {
            queryWrapper.eq(HlrSwitch::getStatus, status);
        }
        queryWrapper.orderByAsc(HlrSwitch::getHlrCode);
        return page(page, queryWrapper);
    }

    @Override
    public HlrSwitch getDetail(Long hlrId) {
        return getById(hlrId);
    }

    @Override
    public HlrSwitch getByHlrCode(String hlrCode) {
        if (StringUtils.isBlank(hlrCode)) {
            return null;
        }
        LambdaQueryWrapper<HlrSwitch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HlrSwitch::getHlrCode, hlrCode);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(HlrSwitch hlrSwitch) {
        // 检查代码是否已存在
        HlrSwitch existHlr = getByHlrCode(hlrSwitch.getHlrCode());
        if (existHlr != null) {
            throw new RuntimeException("HLR/Switch code already exists");
        }
        
        hlrSwitch.setCreateTime(new Date());
        hlrSwitch.setUpdateTime(new Date());
        return save(hlrSwitch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(HlrSwitch hlrSwitch) {
        // 检查代码是否已存在（排除自己）
        LambdaQueryWrapper<HlrSwitch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HlrSwitch::getHlrCode, hlrSwitch.getHlrCode())
                   .ne(HlrSwitch::getHlrId, hlrSwitch.getHlrId());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("HLR/Switch code already exists");
        }
        
        hlrSwitch.setUpdateTime(new Date());
        return updateById(hlrSwitch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long hlrId) {
        // 检查是否被号码段关联
        long segmentCount = numberSegmentMapper.selectCount(
            new LambdaQueryWrapper<NumberSegment>()
                .eq(NumberSegment::getHlrSwitchId, hlrId)
        );
        if (segmentCount > 0) {
            throw new RuntimeException("Cannot delete HLR/Switch: it is referenced by " + segmentCount + " number segment(s)");
        }
        
        return removeById(hlrId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enable(Long hlrId) {
        HlrSwitch hlrSwitch = getById(hlrId);
        if (hlrSwitch == null) {
            return false;
        }
        hlrSwitch.setStatus(1); // Enable
        hlrSwitch.setUpdateTime(new Date());
        return updateById(hlrSwitch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disable(Long hlrId) {
        HlrSwitch hlrSwitch = getById(hlrId);
        if (hlrSwitch == null) {
            return false;
        }
        hlrSwitch.setStatus(0); // Disable
        hlrSwitch.setUpdateTime(new Date());
        return updateById(hlrSwitch);
    }

    @Override
    public List<HlrSwitch> listByRegionId(Long regionId) {
        LambdaQueryWrapper<HlrSwitch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HlrSwitch::getRegionId, regionId);
        queryWrapper.eq(HlrSwitch::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(HlrSwitch::getHlrCode);
        return list(queryWrapper);
    }

    @Override
    public List<HlrSwitch> listByType(Integer hlrType) {
        LambdaQueryWrapper<HlrSwitch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HlrSwitch::getHlrType, hlrType);
        queryWrapper.eq(HlrSwitch::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(HlrSwitch::getHlrCode);
        return list(queryWrapper);
    }
    
    @Override
    public List<HlrSwitch> listAllEnabled() {
        LambdaQueryWrapper<HlrSwitch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HlrSwitch::getStatus, 1); // Only query enabled status
        queryWrapper.orderByAsc(HlrSwitch::getHlrCode);
        return list(queryWrapper);
    }
}