package com.nsrs.simcard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SIM卡分页结果DTO
 */
@Data
@Schema(description = "SIM卡分页结果")
public class SimCardPageResultDTO {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer pageNum;

    @Schema(description = "每页数量")
    private Integer pageSize;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "数据列表")
    private List<SimCardDTO> list;
}