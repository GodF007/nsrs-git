package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.simcard.entity.Supplier;

import java.util.List;

/**
 * 供应商服务接口
 */
public interface SupplierService extends IService<Supplier> {
    
    /**
     * 分页查询供应商
     * 
     * @param page 页码
     * @param size 每页数量
     * @param supplierName 供应商名称 (可选)
     * @param supplierCode 供应商代码 (可选)
     * @param status 状态 (可选)
     * @return 分页结果
     */
    IPage<Supplier> getPage(int page, int size, String supplierName, String supplierCode, Integer status);
    
    /**
     * 获取所有启用的供应商
     * 
     * @return 供应商列表
     */
    List<Supplier> listAllEnabled();
} 