package com.nsrs.busacc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * SIM卡选择响应DTO
 */
@Data
@Schema(description = "SIM卡选择响应")
public class SimCardSelectionResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private Boolean success;
    
    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private String message;
    
    /**
     * SIM卡列表
     */
    @Schema(description = "SIM卡列表")
    private List<SimCardInfo> simCards;
    
    /**
     * 总数量
     */
    @Schema(description = "总数量")
    private Integer totalCount;
    
    /**
     * 查询的ICCID后缀
     */
    @Schema(description = "查询的ICCID后缀")
    private String iccidSuffix;
    
    /**
     * SIM卡信息
     */
    @Data
    @Schema(description = "SIM卡信息")
    public static class SimCardInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * SIM卡ID
         */
        @Schema(description = "SIM卡ID")
        private Long cardId;
        
        /**
         * ICCID
         */
        @Schema(description = "ICCID")
        private String iccid;
        
        /**
         * IMSI
         */
        @Schema(description = "IMSI")
        private String imsi;
        
        /**
         * 数据类型：1-流量卡，2-语音卡，3-双模卡，4-物联网卡
         */
        @Schema(description = "数据类型：1-流量卡，2-语音卡，3-双模卡，4-物联网卡")
        private Integer dataType;
        
        /**
         * SIM卡状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收
         */
        @Schema(description = "SIM卡状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收")
        private Integer status;
        
        /**
         * 批次ID
         */
        @Schema(description = "批次ID")
        private Long batchId;
        
        /**
         * 供应商ID
         */
        @Schema(description = "供应商ID")
        private Long supplierId;
        
        /**
         * 组织ID
         */
        @Schema(description = "组织ID")
        private Long organizationId;
        
        /**
         * 创建时间
         */
        @Schema(description = "创建时间")
        private Date createTime;
        
        /**
         * 备注
         */
        @Schema(description = "备注")
        private String remark;
    }
}