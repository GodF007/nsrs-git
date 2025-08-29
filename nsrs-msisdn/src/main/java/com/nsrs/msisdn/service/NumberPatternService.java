package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.NumberPattern;

import java.util.List;

/**
 * 号码模式服务接口
 */
public interface NumberPatternService extends IService<NumberPattern> {
    
    /**
     * 分页查询号码模式
     *
     * @param page 分页参数
     * @param patternName 模式名称
     * @param levelId 级别ID
     * @param status 状态
     * @return 分页结果
     */
    IPage<NumberPattern> pageList(Page<NumberPattern> page, String patternName, Long levelId, Integer status);
    
    /**
     * 获取号码模式详情
     *
     * @param patternId 模式ID
     * @return 号码模式详情
     */
    NumberPattern getDetail(Long patternId);
    
    /**
     * 新增号码模式
     *
     * @param numberPattern 号码模式信息
     * @return 是否成功
     */
    boolean add(NumberPattern numberPattern);
    
    /**
     * 修改号码模式
     *
     * @param numberPattern 号码模式信息
     * @return 是否成功
     */
    boolean update(NumberPattern numberPattern);
    
    /**
     * 删除号码模式
     *
     * @param patternId 模式ID
     * @return 是否成功
     */
    boolean delete(Long patternId);
    
    /**
     * 启用号码模式
     *
     * @param patternId 模式ID
     * @return 是否成功
     */
    boolean enable(Long patternId);
    
    /**
     * 禁用号码模式
     *
     * @param patternId 模式ID
     * @return 是否成功
     */
    boolean disable(Long patternId);
    
    /**
     * 根据级别ID获取号码模式列表
     *
     * @param levelId 级别ID
     * @return 号码模式列表
     */
    List<NumberPattern> listByLevelId(Long levelId);
    
    /**
     * 获取所有启用的号码模式
     *
     * @return 号码模式列表
     */
    List<NumberPattern> listAllEnabled();
    
    /**
     * 验证号码是否符合指定的号码模式
     *
     * @param number 号码
     * @param patternId 模式ID
     * @return 是否符合
     */
    boolean validateNumber(String number, Long patternId);
} 