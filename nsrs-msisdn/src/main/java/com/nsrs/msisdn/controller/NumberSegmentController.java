package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.NumberSegmentQueryEntity;
import com.nsrs.msisdn.entity.NumberSegment;
import com.nsrs.msisdn.service.NumberSegmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 号码段控制器
 */
@Tag(name = "号码段管理")
@RestController
@RequestMapping("/msisdn/numberSegment")
public class NumberSegmentController {

    @Autowired
    private NumberSegmentService numberSegmentService;

    /**
     * 分页查询号码段
     */
    @Operation(summary = "分页查询号码段")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberSegment>> page(@Valid @RequestBody PageRequest<NumberSegmentQueryEntity> request) {
        NumberSegmentQueryEntity query = request.getQuery();
        Page<NumberSegment> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<NumberSegment> pageResult = numberSegmentService.pageList(page, 
            query != null ? query.getSegmentCode() : null,
            query != null ? query.getSegmentType() : null, 
            query != null ? query.getRegionId() : null,
            query != null ? query.getHlrSwitchId() : null,
            query != null ? query.getStatus() : null);
        return CommonResult.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize()));
    }

    /**
     * 获取号码段详情
     */
    @Operation(summary = "获取号码段详情")
    @GetMapping("/{segmentId}")
    public CommonResult<NumberSegment> getDetail(@Parameter(description = "号码段ID") @PathVariable @NotNull(message = "Segment ID cannot be null") Long segmentId) {
        NumberSegment numberSegment = numberSegmentService.getDetail(segmentId);
        return CommonResult.success(numberSegment);
    }

    /**
     * 根据号码段代码获取号码段
     */
    @Operation(summary = "根据号码段代码获取号码段")
    @GetMapping("/getByCode/{segmentCode}")
    public CommonResult<NumberSegment> getBySegmentCode(@Parameter(description = "号码段代码") @PathVariable @NotBlank(message = "Segment code cannot be blank") String segmentCode) {
        NumberSegment numberSegment = numberSegmentService.getBySegmentCode(segmentCode);
        return CommonResult.success(numberSegment);
    }

    /**
     * 新增号码段
     */
    @Operation(summary = "新增号码段")
    @PostMapping("/add")
    public CommonResult<Boolean> add(@RequestBody @Valid NumberSegment numberSegment) {
        boolean result = numberSegmentService.add(numberSegment);
        return CommonResult.success(result);
    }

    /**
     * 修改号码段
     */
    @Operation(summary = "修改号码段")
    @PutMapping("/update")
    public CommonResult<Boolean> update(@RequestBody @Valid NumberSegment numberSegment) {
        boolean result = numberSegmentService.update(numberSegment);
        return CommonResult.success(result);
    }

    /**
     * 删除号码段
     */
    @Operation(summary = "删除号码段")
    @DeleteMapping("/{segmentId}")
    public CommonResult<Boolean> delete(@Parameter(description = "号码段ID") @PathVariable @NotNull(message = "Segment ID cannot be null") Long segmentId) {
        boolean result = numberSegmentService.delete(segmentId);
        return CommonResult.success(result);
    }

    /**
     * 启用号码�?
     */
    @Operation(summary = "启用号码段")
    @PutMapping("/enable/{segmentId}")
    public CommonResult<Boolean> enable(@Parameter(description = "号码段ID") @PathVariable @NotNull(message = "Segment ID cannot be null") Long segmentId) {
        boolean result = numberSegmentService.enable(segmentId);
        return CommonResult.success(result);
    }

    /**
     * 禁用号码�?
     */
    @Operation(summary = "禁用号码段")
    @PutMapping("/disable/{segmentId}")
    public CommonResult<Boolean> disable(@Parameter(description = "号码段ID") @PathVariable @NotNull(message = "Segment ID cannot be null") Long segmentId) {
        boolean result = numberSegmentService.disable(segmentId);
        return CommonResult.success(result);
    }

    /**
     * 根据类型获取号码段列�?
     */
    @Operation(summary = "根据类型获取号码段列表")
    @GetMapping("/listByType/{segmentType}")
    public CommonResult<List<NumberSegment>> listByType(@Parameter(description = "号码段类型") @PathVariable @NotNull(message = "Segment type cannot be null") Integer segmentType) {
        List<NumberSegment> list = numberSegmentService.listByType(segmentType);
        return CommonResult.success(list);
    }

    /**
     * 根据区域ID获取号码段列�?
     */
    @Operation(summary = "根据区域ID获取号码段列表")
    @GetMapping("/listByRegionId/{regionId}")
    public CommonResult<List<NumberSegment>> listByRegionId(@Parameter(description = "区域ID") @PathVariable @NotNull(message = "Region ID cannot be null") Long regionId) {
        List<NumberSegment> list = numberSegmentService.listByRegionId(regionId);
        return CommonResult.success(list);
    }

    /**
     * 根据HLR/交换机ID获取号码段列表
     */
    @Operation(summary = "根据HLR/交换机ID获取号码段列表")
    @GetMapping("/listByHlrSwitchId/{hlrSwitchId}")
    public CommonResult<List<NumberSegment>> listByHlrSwitchId(@Parameter(description = "HLR/交换机ID") @PathVariable @NotNull(message = "HLR switch ID cannot be null") Long hlrSwitchId) {
        List<NumberSegment> list = numberSegmentService.listByHlrSwitchId(hlrSwitchId);
        return CommonResult.success(list);
    }

    /**
     * 根据条件查询号码段列表（支持单条件和组合条件查询）
     */
    @Operation(summary = "根据条件查询号码段列表", description = "支持按类型、区域ID、HLR/交换机ID进行单条件或组合条件查询，所有参数均为可选")
    @GetMapping("/listByConditions")
    public CommonResult<List<NumberSegment>> listByConditions(
            @Parameter(description = "号码段类型") @RequestParam(required = false) Integer segmentType,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long regionId,
            @Parameter(description = "HLR/交换机ID") @RequestParam(required = false) Long hlrSwitchId) {
        List<NumberSegment> list = numberSegmentService.listByConditions(segmentType, regionId, hlrSwitchId);
        return CommonResult.success(list);
    }

    /**
     * 更新号码段的号码数量统计
     */
    @Operation(summary = "更新号码段的号码总数 -- 没什么用")
    @PutMapping("/updateNumberCount/{segmentId}")
    public CommonResult<Boolean> updateNumberCount(@Parameter(description = "号码段ID") @PathVariable @NotNull(message = "Segment ID cannot be null") Long segmentId) {
        boolean result = numberSegmentService.updateNumberCount(segmentId);
        return CommonResult.success(result);
    }

    /**
     * 获取所有启用的号码段
     */
    @Operation(summary = "获取所有启用的号码段")
    @GetMapping("/listAllEnabled")
    public CommonResult<List<NumberSegment>> listAllEnabled() {
        List<NumberSegment> list = numberSegmentService.listAllEnabled();
        return CommonResult.success(list);
    }
    
}