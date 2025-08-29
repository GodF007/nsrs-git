package com.nsrs.simcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量回收SIM卡请求DTO
 */
@Data
@Schema(description = "批量回收SIM卡请求")
public class BatchRecycleRequest {
    
    @Schema(description = "ICCID列表（推荐使用，支持分表环境）")
    private List<String> iccids;
    
    @Schema(description = "卡ID列表（兼容旧版本，分表环境下可能数据不完整）")
    private List<Long> cardIds;
    
    @NotNull(message = "Operator user ID cannot be null")
    @Schema(description = "操作用户ID", required = true)
    private Long operatorUserId;
    
    /**
     * 验证请求参数
     * @return 是否有效
     */
    public boolean isValid() {
        return (iccids != null && !iccids.isEmpty()) || (cardIds != null && !cardIds.isEmpty());
    }
    
    /**
     * 是否使用ICCID方式
     * @return true表示使用ICCID，false表示使用cardIds
     */
    public boolean useIccids() {
        return iccids != null && !iccids.isEmpty();
    }
}