package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.simcard.entity.SimCardSpecification;

import java.util.List;

/**
 * SIM卡规格服务接口
 */
public interface SimCardSpecificationService extends IService<SimCardSpecification> {
    
    /**
     * 分页查询SIM卡规格
     * 
     * @param page 页码
     * @param size 每页数量
     * @param specName 规格名称 (可选)
     * @param specCode 规格代码 (可选)
     * @param typeId 类型ID (可选)
     * @param status 状态 (可选)
     * @return 分页结果
     */
    IPage<SimCardSpecification> getPage(int page, int size, String specName, String specCode, Long typeId, Integer status);
    
    /**
     * 获取所有启用的SIM卡规格
     * 
     * @return 规格列表
     */
    List<SimCardSpecification> listAllEnabled();
    
    /**
     * 根据类型ID获取启用的SIM卡规格
     * 
     * @param typeId 类型ID
     * @return 规格列表
     */
    List<SimCardSpecification> listByTypeId(Long typeId);
} 