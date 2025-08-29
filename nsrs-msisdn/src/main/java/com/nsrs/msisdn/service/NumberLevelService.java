package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.NumberLevel;

import java.util.List;

/**
 * 号码级别服务接口
 */
public interface NumberLevelService extends IService<NumberLevel> {
    
    /**
     * 分页查询号码级别
     *
     * @param page 分页参数
     * @param levelName 级别名称
     * @param levelCode 级别代码
     * @param status 状态
     * @return 分页结果
     */
    IPage<NumberLevel> pageList(Page<NumberLevel> page, String levelName, String levelCode, Integer status);
    
    /**
     * 获取号码级别详情
     *
     * @param levelId 级别ID
     * @return 号码级别详情
     */
    NumberLevel getDetail(Long levelId);
    
    /**
     * 根据级别代码获取号码级别
     *
     * @param levelCode 级别代码
     * @return 号码级别详情
     */
    NumberLevel getByLevelCode(String levelCode);
    
    /**
     * 新增号码级别
     *
     * @param numberLevel 号码级别信息
     * @return 是否成功
     */
    boolean add(NumberLevel numberLevel);
    
    /**
     * 修改号码级别
     *
     * @param numberLevel 号码级别信息
     * @return 是否成功
     */
    boolean update(NumberLevel numberLevel);
    
    /**
     * 删除号码级别
     *
     * @param levelId 级别ID
     * @return 是否成功
     */
    boolean delete(Long levelId);
    
    /**
     * 启用号码级别
     *
     * @param levelId 级别ID
     * @return 是否成功
     */
    boolean enable(Long levelId);
    
    /**
     * 禁用号码级别
     *
     * @param levelId 级别ID
     * @return 是否成功
     */
    boolean disable(Long levelId);
    
    /**
     * 获取所有启用的号码级别
     *
     * @return 号码级别列表
     */
    List<NumberLevel> listAllEnabled();
    
    /**
     * 获取需要审批的号码级别列表
     *
     * @return 需要审批的号码级别列表
     */
    List<NumberLevel> listNeedApproval();
} 