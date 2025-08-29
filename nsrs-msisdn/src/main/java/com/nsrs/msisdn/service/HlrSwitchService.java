package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.HlrSwitch;

import java.util.List;

/**
 * HLR/交换机服务接口
 */
public interface HlrSwitchService extends IService<HlrSwitch> {
    
    /**
     * 分页查询HLR/交换机
     *
     * @param page 分页参数
     * @param hlrName HLR/交换机名称
     * @param hlrCode HLR/交换机代码
     * @param hlrType HLR/交换机类型
     * @param status 状态
     * @return 分页结果
     */
    IPage<HlrSwitch> pageList(Page<HlrSwitch> page, String hlrName, String hlrCode, 
                         Integer hlrType, Integer status);
    
    /**
     * 获取HLR/交换机详情
     *
     * @param hlrId HLR/交换机ID
     * @return HLR/交换机详情
     */
    HlrSwitch getDetail(Long hlrId);
    
    /**
     * 根据HLR/交换机代码获取HLR/交换机
     *
     * @param hlrCode HLR/交换机代码
     * @return HLR/交换机详情
     */
    HlrSwitch getByHlrCode(String hlrCode);
    
    /**
     * 新增HLR/交换机
     *
     * @param hlrSwitch HLR/交换机信息
     * @return 是否成功
     */
    boolean add(HlrSwitch hlrSwitch);
    
    /**
     * 修改HLR/交换机
     *
     * @param hlrSwitch HLR/交换机信息
     * @return 是否成功
     */
    boolean update(HlrSwitch hlrSwitch);
    
    /**
     * 删除HLR/交换机
     *
     * @param hlrId HLR/交换机ID
     * @return 是否成功
     */
    boolean delete(Long hlrId);
    
    /**
     * 启用HLR/交换机
     *
     * @param hlrId HLR/交换机ID
     * @return 是否成功
     */
    boolean enable(Long hlrId);
    
    /**
     * 禁用HLR/交换机
     *
     * @param hlrId HLR/交换机ID
     * @return 是否成功
     */
    boolean disable(Long hlrId);
    
    /**
     * 根据区域ID获取HLR/交换机列表
     *
     * @param regionId 区域ID
     * @return HLR/交换机列表
     */
    List<HlrSwitch> listByRegionId(Long regionId);
    
    /**
     * 根据类型获取HLR/交换机列表
     *
     * @param hlrType HLR/交换机类型
     * @return HLR/交换机列表
     */
    List<HlrSwitch> listByType(Integer hlrType);
    
    /**
     * 获取所有启用的HLR/交换机
     *
     * @return HLR/交换机列表
     */
    List<HlrSwitch> listAllEnabled();
}