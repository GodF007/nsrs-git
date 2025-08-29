package com.nsrs.msisdn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.msisdn.entity.NumberSegment;

import java.util.List;
import java.util.Map;

/**
 * 号码段服务接口
 */
public interface NumberSegmentService extends IService<NumberSegment> {
    
    /**
     * 分页查询号码段
     *
     * @param page 分页参数
     * @param segmentCode 号码段代码
     * @param segmentType 号码段类型
     * @param regionId 区域ID
     * @param hlrSwitchId HLR/交换机ID
     * @param status 状态
     * @return 分页结果
     */
    IPage<NumberSegment> pageList(Page<NumberSegment> page, String segmentCode, Integer segmentType, 
                                Long regionId, Long hlrSwitchId, Integer status);
    
    /**
     * 查询所有启用的号码段
     *
     * @return 号码段列表
     */
    List<NumberSegment> listEnabled();
    
    /**
     * 获取所有启用的号码段
     *
     * @return 号码段列表
     */
    List<NumberSegment> listAllEnabled();
    
    /**
     * 根据区域ID查询号码段
     *
     * @param regionId 区域ID
     * @return 号码段列表
     */
    List<NumberSegment> listByRegionId(Long regionId);
    
    /**
     * 根据HLR/交换机ID查询号码段
     *
     * @param hlrSwitchId HLR/交换机ID
     * @return 号码段列表
     */
    List<NumberSegment> listByHlrSwitchId(Long hlrSwitchId);
    
    /**
     * 根据条件查询号码段列表（支持单条件和组合条件查询）
     *
     * @param segmentType 号码段类型（可选）
     * @param regionId 区域ID（可选）
     * @param hlrSwitchId HLR/交换机ID（可选）
     * @return 号码段列表
     */
    List<NumberSegment> listByConditions(Integer segmentType, Long regionId, Long hlrSwitchId);
    
    /**
     * 根据ID获取号码段
     *
     * @param segmentId 号码段ID
     * @return 号码段
     */
    NumberSegment getDetail(Long segmentId);
    
    /**
     * 根据号码段代码获取号码段
     *
     * @param segmentCode 号码段代码
     * @return 号码段详情
     */
    NumberSegment getBySegmentCode(String segmentCode);
    
    /**
     * 新增号码段
     *
     * @param numberSegment 号码段信息
     * @return 是否成功
     */
    boolean add(NumberSegment numberSegment);
    
    /**
     * 修改号码段
     *
     * @param numberSegment 号码段信息
     * @return 是否成功
     */
    boolean update(NumberSegment numberSegment);
    
    /**
     * 删除号码段
     *
     * @param segmentId 号码段ID
     * @return 是否成功
     */
    boolean delete(Long segmentId);
    
    /**
     * 启用号码段
     *
     * @param segmentId 号码段ID
     * @return 是否成功
     */
    boolean enable(Long segmentId);
    
    /**
     * 禁用号码段
     *
     * @param segmentId 号码段ID
     * @return 是否成功
     */
    boolean disable(Long segmentId);
    
    /**
     * 根据类型获取号码段列表
     *
     * @param segmentType 号码段类型
     * @return 号码段列表
     */
    List<NumberSegment> listByType(Integer segmentType);
    
    /**
     * 获取号码段统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();
    
    /**
     * 更新号码段状态统计
     *
     * @param segmentId 号码段ID
     * @return 是否成功
     */
    boolean updateSegmentStatusCount(Long segmentId);
    
    /**
     * 更新号码段的号码数量统计
     *
     * @param segmentId 号码段ID
     * @return 是否成功
     */
    boolean updateNumberCount(Long segmentId);
    
    /**
     * 增量更新号码段统计信息
     *
     * @param segmentId 号码段ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @return 是否成功
     */
    boolean incrementalUpdateStatistics(Long segmentId, Integer oldStatus, Integer newStatus);
    
    /**
     * 批量增量更新号码段统计信息
     *
     * @param segmentId 号码段ID
     * @param statusChanges 状态变更映射，key为状态，value为变更数量（正数为增加，负数为减少）
     * @return 是否成功
     */
    boolean batchIncrementalUpdateStatistics(Long segmentId, Map<Integer, Integer> statusChanges);
    
    /**
     * 重置号码段统计信息（重新计算）
     *
     * @param segmentId 号码段ID
     * @return 是否成功
     */
    boolean resetStatistics(Long segmentId);
}