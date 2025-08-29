package com.nsrs.msisdn.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 号码资源VO
 */
@Data
public class NumberResourceVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 号码ID
     */
    private Long numberId;
    
    /**
     * 号码
     */
    private String number;
    
    /**
     * 号码类型：1-PSTN Number，2-Mobile Number，3-FTTH Number，4-SIP，5-VSAT
     */
    private Integer numberType;
    
    /**
     * 号码类型名称
     */
    private String numberTypeName;
    
    /**
     * 号码段ID
     */
    private Long segmentId;
    
    /**
     * 号码段编码
     */
    private String segmentCode;
    
    /**
     * 号码级别ID
     */
    private Long levelId;
    
    /**
     * 号码级别名称
     */
    private String levelName;
    
    /**
     * 号码模式ID
     */
    private Long patternId;
    
    /**
     * 号码模式名称
     */
    private String patternName;
    
    /**
     * HLR/交换机ID
     */
    private Long hlrId;
    
    /**
     * HLR/交换机名称
     */
    private String hlrName;
    
    /**
     * ICCID
     */
    private String iccid;
    
    /**
     * 状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定
     */
    private Integer status;
    
    /**
     * 状态名称
     */
    private String statusName;
    
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
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}