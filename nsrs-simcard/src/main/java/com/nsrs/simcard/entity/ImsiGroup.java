package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * IMSI组实体类
 */
@Data
@TableName("imsi_group")
@Schema(description = "IMSI组信息")
public class ImsiGroup {
    
    @TableId(value = "group_id", type = IdType.AUTO)
    @Schema(description = "组ID")
    private Long groupId;
    
    @Schema(description = "组名称")
    private String groupName;
    
    @Schema(description = "IMSI前缀")
    private String imsiPrefix;
    
    @Schema(description = "起始IMSI")
    private String imsiStart;
    
    @Schema(description = "结束IMSI")
    private String imsiEnd;
    
    @Schema(description = "IMSI类型：1-GSM Postpaid，2-GSM Prepaid，3-CDMA......")
    private Integer imsiType;
    
    @Schema(description = "总数量")
    private Integer totalCount;
    
    @Schema(description = "已使用数量")
    private Integer usedCount;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "创建用户ID")
    private Long createUserId;
    
    @Schema(description = "更新用户ID")
    private Long updateUserId;
}