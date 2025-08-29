package com.nsrs.binding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * 批量绑定任务实体
 */
@Data
@TableName("batch_binding_task")
@Schema(description = "批量绑定任务")
public class BatchBindingTask {

    @Schema(description = "任务ID")
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    @NotBlank(message = "Task name cannot be blank")
    @Schema(description = "任务名称", required = true)
    @TableField("task_name")
    private String taskName;

    @NotNull(message = "Task type cannot be null")
    @Schema(description = "任务类型：1-绑定，2-解绑", required = true)
    @TableField("task_type")
    private Integer taskType;

    @Schema(description = "文件路径")
    @TableField("file_path")
    private String filePath;

    @Min(value = 0, message = "Total count cannot be negative")
    @Schema(description = "总数")
    @TableField("total_count")
    private Integer totalCount = 0;

    @Min(value = 0, message = "Success count cannot be negative")
    @Schema(description = "成功数量")
    @TableField("success_count")
    private Integer successCount = 0;

    @Min(value = 0, message = "Fail count cannot be negative")
    @Schema(description = "失败数量")
    @TableField("fail_count")
    private Integer failCount = 0;

    @NotNull(message = "Status cannot be null")
    @Schema(description = "状态：0-待处理，1-处理中，2-已完成，3-已失败", required = true)
    @TableField("status")
    private Integer status = 0;

    @Schema(description = "开始时间")
    @TableField("start_time")
    private Date startTime;

    @Schema(description = "结束时间")
    @TableField("end_time")
    private Date endTime;

    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private Date createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private Date updateTime;

    @Schema(description = "创建者ID")
    @TableField("create_user_id")
    private Long createUserId;

    @Schema(description = "更新者ID")
    @TableField("update_user_id")
    private Long updateUserId;
}