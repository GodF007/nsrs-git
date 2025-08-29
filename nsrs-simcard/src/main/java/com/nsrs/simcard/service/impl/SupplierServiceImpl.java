package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.simcard.entity.Supplier;
import com.nsrs.simcard.mapper.SupplierMapper;
import com.nsrs.simcard.service.SupplierService;
import com.nsrs.simcard.utils.SimCardConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 供应商服务实现类
 */
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    @Override
    public IPage<Supplier> getPage(int page, int size, String supplierName, String supplierCode, Integer status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(supplierName)) {
            wrapper.like(Supplier::getSupplierName, supplierName);
        }
        
        if (StringUtils.hasText(supplierCode)) {
            wrapper.like(Supplier::getSupplierCode, supplierCode);
        }
        
        if (status != null) {
            wrapper.eq(Supplier::getStatus, status);
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Supplier::getCreateTime);
        
        // 执行分页查询
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Supplier> listAllEnabled() {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getStatus, SimCardConstant.ENABLED)
               .orderByAsc(Supplier::getSupplierCode);
        return list(wrapper);
    }
} 