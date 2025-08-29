package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.simcard.entity.SimCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * SIM卡数据访问层
 */
@Mapper
public interface SimCardMapper extends BaseMapper<SimCard> {
    
    /**
     * 批量导入SIM卡
     * 
     * @param simCards SIM卡列表
     * @return 导入结果
     */
    int batchInsert(@Param("list") List<SimCard> simCards);
    
    /**
     * 根据条件分页查询SIM卡列表
     * 
     * @param page 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SimCard> selectSimCardPage(Page<SimCard> page, @Param("params") Map<String, Object> params);
    
    /**
     * 统计各状态SIM卡数量
     * 
     * @param params 查询参数
     * @return 状态统计
     */
    List<Map<String, Object>> countByStatus(@Param("params") Map<String, Object> params);
    
    /**
     * 批量更新SIM卡状态
     * 
     * @param cardIds 卡ID列表
     * @param status 新状态
     * @return 更新结果
     */
    int batchUpdateStatus(@Param("cardIds") List<Long> cardIds, @Param("status") Integer status);
    
    /**
     * 按卡类型统计SIM卡数量
     * 
     * @param params 查询参数
     * @return 卡类型统计
     */
    List<Map<String, Object>> countByCardType(@Param("params") Map<String, Object> params);
    
    /**
     * 按组织统计SIM卡数量
     * 
     * @param params 查询参数
     * @return 组织统计
     */
    List<Map<String, Object>> countByOrganization(@Param("params") Map<String, Object> params);
    
    /**
     * 告警系统专用：统计可用库存数量（只统计已发布状态）
     * 
     * @param params 查询参数
     * @return 可用库存数量
     */
    Integer countAvailableByAlertConditions(@Param("params") Map<String, Object> params);
    
    /**
     * 告警系统专用：统计总库存数量
     * 
     * @param params 查询参数
     * @return 总库存数量
     */
    Integer countTotalByAlertConditions(@Param("params") Map<String, Object> params);
    
    /**
     * 告警系统专用：按状态统计库存数量
     * 
     * @param params 查询参数
     * @return 状态统计结果
     */
    List<Map<String, Object>> countByStatusAndAlertConditions(@Param("params") Map<String, Object> params);
    
    /**
     * 告警系统专用：批量统计库存数量
     * 减少数据库查询次数，提升性能
     * 
     * @param alertConfigs 告警配置列表
     * @return 库存统计结果
     */
    List<Map<String, Object>> batchCountInventoryForAlerts(@Param("alertConfigs") List<Map<String, Object>> alertConfigs);
}