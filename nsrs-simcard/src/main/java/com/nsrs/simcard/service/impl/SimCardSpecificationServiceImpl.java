package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.simcard.entity.SimCardSpecification;
import com.nsrs.simcard.mapper.SimCardSpecificationMapper;
import com.nsrs.simcard.service.SimCardSpecificationService;
import com.nsrs.simcard.utils.SimCardConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * SIM卡规格服务实现类
 */
@Service
public class SimCardSpecificationServiceImpl extends ServiceImpl<SimCardSpecificationMapper, SimCardSpecification> implements SimCardSpecificationService {

    @Override
    public IPage<SimCardSpecification> getPage(int page, int size, String specName, String specCode, Long typeId, Integer status) {
        LambdaQueryWrapper<SimCardSpecification> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(specName)) {
            wrapper.like(SimCardSpecification::getSpecName, specName);
        }
        
        if (StringUtils.hasText(specCode)) {
            wrapper.like(SimCardSpecification::getSpecCode, specCode);
        }
        
        if (typeId != null) {
            wrapper.eq(SimCardSpecification::getTypeId, typeId);
        }
        
        if (status != null) {
            wrapper.eq(SimCardSpecification::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(SimCardSpecification::getCreateTime);
        
        // 执行分页查询
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<SimCardSpecification> listAllEnabled() {
        LambdaQueryWrapper<SimCardSpecification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SimCardSpecification::getStatus, SimCardConstant.ENABLED)
               .orderByAsc(SimCardSpecification::getSpecCode);
        return list(wrapper);
    }

    @Override
    public List<SimCardSpecification> listByTypeId(Long typeId) {
        LambdaQueryWrapper<SimCardSpecification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SimCardSpecification::getTypeId, typeId)
               .eq(SimCardSpecification::getStatus, SimCardConstant.ENABLED)
               .orderByAsc(SimCardSpecification::getSpecCode);
        return list(wrapper);
    }
} 