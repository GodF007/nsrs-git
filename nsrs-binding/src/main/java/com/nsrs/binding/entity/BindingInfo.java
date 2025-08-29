package com.nsrs.binding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName("binding_info")
@Schema(description = "号码与卡绑定信息")
public class BindingInfo {
    
    @TableId(value = "binding_id", type = IdType.AUTO)
    @Schema(description = "绑定ID")
    private Long bindingId;
    
    @Schema(description = "任务ID")
    private Long taskId;
    
    @Schema(description = "MSISDN")
    private String msisdn;
    
    @Schema(description = "IMSI")
    private String imsi;
    
    @Schema(description = "ICCID")
    private String iccid;
    
    @Schema(description = "分区字段")
    private String partitionField;
    
    @Schema(description = "状态：0-待绑定，1-已绑定，2-解绑")
    private Integer status;
    
    @Schema(description = "绑定时间")
    private Date bindTime;
    
    @Schema(description = "解绑时间")
    private Date unbindTime;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "创建用户ID")
    private Long createUserId;
    
    @Schema(description = "更新用户ID")
    private Long updateUserId;
    
    @Schema(description = "备注")
    private String remark;
} 