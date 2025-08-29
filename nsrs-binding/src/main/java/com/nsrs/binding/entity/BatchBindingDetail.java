package com.nsrs.binding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 批量绑定详情实体类
 */
@Data
@TableName("batch_binding_detail")
@Schema(description = "批量绑定详情")
public class BatchBindingDetail {
    
    @Schema(description = "详情ID")
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Long detailId;
    
    @Schema(description = "任务ID")
    @TableField("task_id")
    private Long taskId;
    
    @Schema(description = "号码")
    @TableField("number")
    private String number;
    
    @Schema(description = "IMSI")
    @TableField("imsi")
    private String imsi;
    
    @Schema(description = "ICCID")
    @TableField("iccid")
    private String iccid;
    
    @Schema(description = "状态：0-待处理，1-成功，2-失败")
    @TableField("status")
    private Integer status;
    
    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;
    
    @Schema(description = "处理时间")
    @TableField("process_time")
    private Date processTime;
    
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