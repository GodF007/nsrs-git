package com.nsrs.binding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@TableName(value = "number_imsi_binding", autoResultMap = true)
@Schema(description = "号码与IMSI绑定信息")
public class NumberImsiBinding {
    
    @TableId(value = "binding_id", type = IdType.INPUT)
    @Schema(description = "绑定ID")
    private Long bindingId;
    
    @NotNull(message = "Number ID cannot be null")
    @TableField("number_id")
    @Schema(description = "号码ID", required = true)
    private Long numberId;
    
    @NotBlank(message = "Number cannot be blank")
    @TableField("number")
    @Schema(description = "号码", required = true)
    private String number;
    
    @NotNull(message = "IMSI ID cannot be null")
    @TableField("imsi_id")
    @Schema(description = "IMSI ID", required = true)
    private Long imsiId;
    
    @NotBlank(message = "IMSI cannot be blank")
    @TableField("imsi")
    @Schema(description = "IMSI号码", required = true)
    private String imsi;
    
    @TableField("iccid")
    @Schema(description = "ICCID")
    private String iccid;
    
    @NotNull(message = "Order ID cannot be null")
    @TableField("order_id")
    @Schema(description = "订单ID", required = true)
    private Long orderId;
    
    @NotNull(message = "Binding time cannot be null")
    @TableField("binding_time")
    @Schema(description = "绑定时间", required = true)
    private Date bindingTime;
    
    @TableField("binding_type")
    @Schema(description = "绑定类型: 1-普通绑定，2-批量绑定，3-测试")
    private Integer bindingType;
    
    @NotNull(message = "Binding status cannot be null")
    @TableField("binding_status")
    @Schema(description = "绑定状态：1-已绑定，2-已解绑", required = true)
    private Integer bindingStatus = 1;
    
    @TableField("unbind_time")
    @Schema(description = "解绑时间")
    private Date unbindTime;
    
    @TableField("operator_user_id")
    @Schema(description = "操作用户ID")
    private Long operatorUserId;
    
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    private Date createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @TableField("create_user_id")
    @Schema(description = "创建用户ID")
    private Long createUserId;
    
    @TableField("update_user_id")
    @Schema(description = "更新用户ID")
    private Long updateUserId;
}