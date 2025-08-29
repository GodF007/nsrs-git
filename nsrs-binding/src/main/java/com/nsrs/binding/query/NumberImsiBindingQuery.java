package com.nsrs.binding.query;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 号码与IMSI绑定查询条件
 * @author system
 * @date 2025-01-20
 */
@Data
public class NumberImsiBindingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 号码（支持精确查询和前缀查询）
     */
    private String number;

    /**
     * IMSI号码
     */
    private String imsi;

    /**
     * ICCID
     */
    private String iccid;

    /**
     * 绑定状态：1-已绑定，2-已解绑
     */
    private Integer bindingStatus;

    /**
     * 绑定类型：1-手动绑定，2-自动绑定，3-批量绑定
     */
    private Integer bindingType;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 操作用户ID
     */
    private Long operatorUserId;
}