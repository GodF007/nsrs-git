package com.nsrs.msisdn.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;

/**
 * 号码资源查询参数实体
 */
@Schema(description = "号码资源查询参数")
public class NumberResourceQueryEntity {

    /**
     * 号码前缀
     */
    @NotBlank(message = "Number prefix cannot be empty, it is required for table sharding query")
    @Schema(description = "号码前缀", example = "138", required = true)
    private String numberPrefix;

    /**
     * 号码
     */
    @Schema(description = "号码", example = "13800138000")
    private String number;

    /**
     * 号码类型
     */
    @Schema(description = "号码类型")
    private Integer numberType;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long regionId;

    /**
     * 级别ID
     */
    @Schema(description = "级别ID")
    private Long levelId;

    /**
     * 模式ID
     */
    @Schema(description = "模式ID")
    private Long patternId;

    /**
     * 号段ID
     */
    @Schema(description = "号段ID")
    private Long segmentId;

    /**
     * HLR交换机ID
     */
    @Schema(description = "HLR交换机ID")
    private Long hlrSwitchId;

    /**
     * 归属机构
     */
    @Schema(description = "归属机构")
    private String attributiveOrg;

    public String getNumberPrefix() {
        return numberPrefix;
    }

    public void setNumberPrefix(String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getNumberType() {
        return numberType;
    }

    public void setNumberType(Integer numberType) {
        this.numberType = numberType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public Long getLevelId() {
        return levelId;
    }

    public void setLevelId(Long levelId) {
        this.levelId = levelId;
    }

    public Long getPatternId() {
        return patternId;
    }

    public void setPatternId(Long patternId) {
        this.patternId = patternId;
    }

    public Long getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(Long segmentId) {
        this.segmentId = segmentId;
    }

    public Long getHlrSwitchId() {
        return hlrSwitchId;
    }

    public void setHlrSwitchId(Long hlrSwitchId) {
        this.hlrSwitchId = hlrSwitchId;
    }

    public String getAttributiveOrg() {
        return attributiveOrg;
    }

    public void setAttributiveOrg(String attributiveOrg) {
        this.attributiveOrg = attributiveOrg;
    }

    @Override
    public String toString() {
        return "NumberResourceQueryEntity{" +
                "numberPrefix='" + numberPrefix + '\'' +
                ", number='" + number + '\'' +
                ", numberType=" + numberType +
                ", status=" + status +
                ", regionId=" + regionId +
                ", levelId=" + levelId +
                ", patternId=" + patternId +
                ", segmentId=" + segmentId +
                ", hlrSwitchId=" + hlrSwitchId +
                ", attributiveOrg='" + attributiveOrg + '\'' +
                '}';
    }
}