package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * HLR/交换机查询参数实体
 */
@Data
@Schema(description = "HLR/交换机查询参数")
public class HlrSwitchQueryEntity {

    /**
     * HLR/交换机名称
     */
    @Schema(description = "HLR/交换机名称", example = "HLR001")
    private String hlrName;

    /**
     * HLR/交换机代码
     */
    @Schema(description = "HLR/交换机代码", example = "HLR001")
    private String hlrCode;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long regionId;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;


}