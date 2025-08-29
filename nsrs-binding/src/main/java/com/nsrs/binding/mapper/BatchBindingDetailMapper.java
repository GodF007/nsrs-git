package com.nsrs.binding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.binding.entity.BatchBindingDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 批量绑定详情Mapper接口
 */
@Mapper
public interface BatchBindingDetailMapper extends BaseMapper<BatchBindingDetail> {
    
    /**
     * 根据任务ID查询绑定详情列表
     *
     * @param taskId 任务ID
     * @return 详情列表
     */
    List<BatchBindingDetail> selectByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 批量插入绑定详情
     *
     * @param detailList 详情列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<BatchBindingDetail> detailList);
    
    /**
     * 根据任务ID统计各状态的数量
     *
     * @param taskId 任务ID
     * @return 统计结果
     */
    List<Map<String, Object>> countStatusByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 更新绑定详情状态
     *
     * @param detailId 详情ID
     * @param status 状态
     * @param errorMsg 错误信息
     * @return 影响行数
     */
    int updateStatus(@Param("detailId") Long detailId, @Param("status") Integer status, @Param("errorMsg") String errorMsg);
    
    /**
     * 根据任务ID删除详情
     *
     * @param taskId 任务ID
     * @return 删除数量
     */
    int deleteByTaskId(@Param("taskId") Long taskId);
    
    /**
     * 批量更新状态
     *
     * @param list 详情列表
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("list") List<BatchBindingDetail> list);
}