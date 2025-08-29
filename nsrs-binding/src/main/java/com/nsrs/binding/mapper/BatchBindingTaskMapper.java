package com.nsrs.binding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.binding.entity.BatchBindingTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 批量绑定任务Mapper接口
 */
@Mapper
public interface BatchBindingTaskMapper extends BaseMapper<BatchBindingTask> {
    
    /**
     * 根据任务ID查询任务详情
     * @param taskId 任务ID
     * @return 任务详情
     */
    BatchBindingTask selectTaskDetailById(@Param("taskId") Long taskId);
}