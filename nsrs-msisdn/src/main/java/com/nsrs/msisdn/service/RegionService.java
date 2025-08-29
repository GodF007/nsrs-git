package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.Region;

import java.util.List;

/**
 * 区域服务接口
 */
public interface RegionService extends IService<Region> {
    
    /**
     * 分页查询区域
     *
     * @param page 分页参数
     * @param regionCode 区域代码
     * @param regionName 区域名称
     * @param regionType 区域类型
     * @param status 状态
     * @return 分页结果
     */
    IPage<Region> pageList(Page<Region> page, String regionCode, String regionName, 
                         Integer regionType, Integer status);
    
    /**
     * 获取区域详情
     *
     * @param regionId 区域ID
     * @return 区域详情
     */
    Region getDetail(Long regionId);
    
    /**
     * 根据区域代码获取区域
     *
     * @param regionCode 区域代码
     * @return 区域详情
     */
    Region getByRegionCode(String regionCode);
    
    /**
     * 新增区域
     *
     * @param region 区域信息
     * @return 是否成功
     */
    boolean add(Region region);
    
    /**
     * 修改区域
     *
     * @param region 区域信息
     * @return 是否成功
     */
    boolean update(Region region);
    
    /**
     * 删除区域
     *
     * @param regionId 区域ID
     * @return 是否成功
     */
    boolean delete(Long regionId);
    
    /**
     * 启用区域
     *
     * @param regionId 区域ID
     * @return 是否成功
     */
    boolean enable(Long regionId);
    
    /**
     * 禁用区域
     *
     * @param regionId 区域ID
     * @return 是否成功
     */
    boolean disable(Long regionId);
    
    /**
     * 获取子区域列表
     *
     * @param parentId 父区域ID
     * @return 子区域列表
     */
    List<Region> listByParentId(Long parentId);
    
    /**
     * 获取区域树
     *
     * @param parentId 父区域ID，为null时表示获取全部
     * @param regionType 区域类型，为null时表示获取全部
     * @return 区域树
     */
    List<Region> listRegionTree(Long parentId, Integer regionType);
    
    /**
     * 获取所有启用的区域
     *
     * @return 区域列表
     */
    List<Region> listAllEnabled();
}