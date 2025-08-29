package com.nsrs.binding.query;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 批量绑定详情查询条件
 * @author system
 * @date 2025-01-20
 */
@Data
public class BatchBindingDetailQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID（必填，用于分表路由）
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /**
     * 状态：0-待处理，1-成功，2-失败
     */
    private Integer status;

    /**
     * 号码
     */
    private String number;

    /**
     * IMSI号码
     */
    private String imsi;

    /**
     * 错误信息
     */
    private String errorMsg;
}