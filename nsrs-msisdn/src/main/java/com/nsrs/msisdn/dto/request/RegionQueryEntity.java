package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 区域查询参数实体
 */
@Data
@Schema(description = "区域查询参数")
public class RegionQueryEntity {

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "北京")
    private String regionName;

    /**
     * 区域代码
     */
    @Schema(description = "区域代码", example = "010")
    private String regionCode;

    /**
     * 父区域ID
     */
    @Schema(description = "父区域ID")
    private Long parentId;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型")
    private Integer regionType;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;


}