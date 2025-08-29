package com.nsrs.msisdn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 号码资源DTO
 */
@Data
@Schema(description = "号码资源信息")
public class NumberResourceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 号码
     */
    @Schema(description = "号码", required = true)
    @NotBlank(message = "Number cannot be blank")
    private String number;
    
    /**
     * 号码类型：1-PSTN Number，2-Mobile Number，3-FTTH Number，4-SIP，5-VSAT
     */
    @Schema(description = "号码类型", required = true)
    @NotNull(message = "Number type cannot be null")
    private Integer numberType;
    
    /**
     * 号码段ID
     */
    @Schema(description = "号码段ID")
    private Long segmentId;
    
    /**
     * 号码级别ID
     */
    private Long levelId;
    
    /**
     * 号码模式ID
     */
    private Long patternId;
    
    /**
     * HLR/交换机ID
     */
    private Long hlrId;
    
    /**
     * HLR/交换机ID (别名，用于前端兼容)
     */
    private Long hlrSwitchId;
    
    /**
     * ICCID
     */
    private String iccid;
    
    /**
     * 状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定
     */
    @Schema(description = "状态", required = true)
    @NotNull(message = "Status cannot be null")
    private Integer status;
    
    /**
     * 费用
     */
    private BigDecimal charge;
    
    /**
     * 归属组织
     */
    private String attributiveOrg;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 是否需要分页
     */
    private Boolean needPage = true;
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页记录数
     */
    private Integer size = 10;
}