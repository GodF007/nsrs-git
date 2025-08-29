package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.simcard.entity.SimCardType;

import java.util.List;

/**
 * SIM卡类型服务接口
 */
public interface SimCardTypeService extends IService<SimCardType> {
    
    /**
     * 分页查询SIM卡类型
     * 
     * @param page 页码
     * @param size 每页数量
     * @param typeName 类型名称 (可选)
     * @param typeCode 类型代码 (可选)
     * @param status 状态 (可选)
     * @return 分页结果
     */
    IPage<SimCardType> getPage(int page, int size, String typeName, String typeCode, Integer status);
    
    /**
     * 获取所有启用的SIM卡类型
     * 
     * @return 类型列表
     */
    List<SimCardType> listAllEnabled();
} 