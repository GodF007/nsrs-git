package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.simcard.entity.SimCardType;
import com.nsrs.simcard.mapper.SimCardTypeMapper;
import com.nsrs.simcard.service.SimCardTypeService;
import com.nsrs.simcard.utils.SimCardConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * SIM卡类型服务实现类
 */
@Service
public class SimCardTypeServiceImpl extends ServiceImpl<SimCardTypeMapper, SimCardType> implements SimCardTypeService {

    @Override
    public IPage<SimCardType> getPage(int page, int size, String typeName, String typeCode, Integer status) {
        LambdaQueryWrapper<SimCardType> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(typeName)) {
            wrapper.like(SimCardType::getTypeName, typeName);
        }
        
        if (StringUtils.hasText(typeCode)) {
            wrapper.like(SimCardType::getTypeCode, typeCode);
        }
        
        if (status != null) {
            wrapper.eq(SimCardType::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(SimCardType::getCreateTime);
        
        // 执行分页查询
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<SimCardType> listAllEnabled() {
        LambdaQueryWrapper<SimCardType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SimCardType::getStatus, SimCardConstant.ENABLED)
               .orderByAsc(SimCardType::getTypeCode);
        return list(wrapper);
    }
} 