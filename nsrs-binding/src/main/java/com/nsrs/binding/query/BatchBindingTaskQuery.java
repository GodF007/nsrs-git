package com.nsrs.binding.query;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批量绑定任务查询条件
 * @author system
 * @date 2025-01-20
 */
@Data
public class BatchBindingTaskQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务状态：0-待处理，1-处理中，2-已完成，3-已失败
     */
    private Integer status;

    /**
     * 任务类型：1-绑定，2-解绑
     */
    private Integer taskType;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}