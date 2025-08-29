package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

/**
 * 号段查询参数实体
 */
@Schema(description = "号段查询参数")
public class NumberSegmentQueryEntity {

    /**
     * 号段编码（必填）
     */
    @Schema(description = "号段编码", example = "138", required = true)
    @NotBlank(message = "号段编码不能为空")
    private String segmentCode;

    /**
     * 号段类型
     */
    @Schema(description = "号段类型")
    private Integer segmentType;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long regionId;

    /**
     * HLR交换机ID
     */
    @Schema(description = "HLR交换机ID")
    private Long hlrSwitchId;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    public String getSegmentCode() {
        return segmentCode;
    }

    public void setSegmentCode(String segmentCode) {
        this.segmentCode = segmentCode;
    }

    public Integer getSegmentType() {
        return segmentType;
    }

    public void setSegmentType(Integer segmentType) {
        this.segmentType = segmentType;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public Long getHlrSwitchId() {
        return hlrSwitchId;
    }

    public void setHlrSwitchId(Long hlrSwitchId) {
        this.hlrSwitchId = hlrSwitchId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NumberSegmentQueryEntity{" +
                "segmentCode='" + segmentCode + '\'' +
                ", segmentType=" + segmentType +
                ", regionId=" + regionId +
                ", hlrSwitchId=" + hlrSwitchId +
                ", status=" + status +
                '}';
    }
}