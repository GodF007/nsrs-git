package com.nsrs.busacc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 号码选择响应DTO
 */
@Data
@Schema(description = "号码选择响应")
public class NumberSelectionResponse implements Serializable {
    
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
     * 号码列表
     */
    @Schema(description = "号码列表")
    private List<NumberInfo> numbers;
    
    /**
     * 总数量
     */
    @Schema(description = "总数量")
    private Integer totalCount;
    
    /**
     * 查询的号段前缀
     */
    @Schema(description = "查询的号段前缀")
    private String numberPrefix;
    
    /**
     * 号码信息
     */
    @Data
    @Schema(description = "号码信息")
    public static class NumberInfo implements Serializable {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 号码ID
         */
        @Schema(description = "号码ID")
        private Long numberId;
        
        /**
         * 号码
         */
        @Schema(description = "号码")
        private String number;
        
        /**
         * 号码类型
         */
        @Schema(description = "号码类型：1-固话，2-手机，3-800，4-400，5-VOIP，6-物联网")
        private Integer numberType;
        
        /**
         * 号码状态
         */
        @Schema(description = "号码状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定")
        private Integer status;
        
        /**
         * 费用
         */
        @Schema(description = "费用")
        private java.math.BigDecimal charge;
        
        /**
         * 号码段ID
         */
        @Schema(description = "号码段ID")
        private Long segmentId;
        
        /**
         * 号码级别ID
         */
        @Schema(description = "号码级别ID")
        private Long levelId;
    }
}