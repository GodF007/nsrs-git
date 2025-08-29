package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.HlrSwitchQueryEntity;
import com.nsrs.msisdn.entity.HlrSwitch;
import com.nsrs.msisdn.service.HlrSwitchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * HLR/交换机控制器
 */
@Tag(name = "HLR/交换机管理")
@RestController
@RequestMapping("/msisdn/hlrSwitch")
public class HlrSwitchController {

    @Autowired
    private HlrSwitchService hlrSwitchService;

    /**
     * 分页查询HLR/交换机
     */
    @Operation(summary = "分页查询HLR/交换机")
    @PostMapping("/page")
    public CommonResult<PageResult<HlrSwitch>> page(@Valid @RequestBody PageRequest<HlrSwitchQueryEntity> request) {
        HlrSwitchQueryEntity query = request.getQuery();
        Page<HlrSwitch> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<HlrSwitch> pageResult = hlrSwitchService.pageList(page, 
            query != null ? query.getHlrName() : null,
            query != null ? query.getHlrCode() : null,
            null, // hlrType 字段在HlrSwitchQueryEntity中不存在，传null
            query != null ? query.getStatus() : null);
        return CommonResult.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize()));
    }

    /**
     * 获取HLR/交换机详�?
     */
    @Operation(summary = "获取HLR/交换机详情")
    @GetMapping("/{hlrId}")
    public CommonResult<HlrSwitch> getDetail(@Parameter(description = "HLR/交换机ID") @PathVariable Long hlrId) {
        HlrSwitch hlrSwitch = hlrSwitchService.getDetail(hlrId);
        return CommonResult.success(hlrSwitch);
    }

    /**
     * 新增HLR/交换机
     */
    @Operation(summary = "新增HLR/交换机")
    @PostMapping("/add")
    public CommonResult<Boolean> add(@Valid @RequestBody HlrSwitch hlrSwitch) {
        boolean result = hlrSwitchService.add(hlrSwitch);
        return CommonResult.success(result);
    }

    /**
     * 修改HLR/交换机
     */
    @Operation(summary = "修改HLR/交换机")
    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody HlrSwitch hlrSwitch) {
        boolean result = hlrSwitchService.update(hlrSwitch);
        return CommonResult.success(result);
    }

    /**
     * 删除HLR/交换�?
     */
    @Operation(summary = "删除HLR/交换机")
    @DeleteMapping("/{hlrId}")
    public CommonResult<Boolean> delete(@Parameter(description = "HLR/交换机ID") @PathVariable Long hlrId) {
        boolean result = hlrSwitchService.delete(hlrId);
        return CommonResult.success(result);
    }

    /**
     * 启用HLR/交换�?
     */
    @Operation(summary = "启用HLR/交换机")
    @PutMapping("/enable/{hlrId}")
    public CommonResult<Boolean> enable(@Parameter(description = "HLR/交换机ID") @PathVariable Long hlrId) {
        boolean result = hlrSwitchService.enable(hlrId);
        return CommonResult.success(result);
    }

    /**
     * 禁用HLR/交换�?
     */
    @Operation(summary = "禁用HLR/交换机")
    @PutMapping("/disable/{hlrId}")
    public CommonResult<Boolean> disable(@Parameter(description = "HLR/交换机ID") @PathVariable Long hlrId) {
        boolean result = hlrSwitchService.disable(hlrId);
        return CommonResult.success(result);
    }

    /**
     * 根据区域ID获取HLR/交换机列�?
     */
    @Operation(summary = "根据区域ID获取HLR/交换机列表")
    @GetMapping("/listByRegionId/{regionId}")
    public CommonResult<List<HlrSwitch>> listByRegionId(@Parameter(description = "区域ID") @PathVariable Long regionId) {
        List<HlrSwitch> list = hlrSwitchService.listByRegionId(regionId);
        return CommonResult.success(list);
    }

    /**
     * 根据类型获取HLR/交换机列�?
     */
    @Operation(summary = "根据类型获取HLR/交换机列表")
    @GetMapping("/listByType/{hlrType}")
    public CommonResult<List<HlrSwitch>> listByType(@Parameter(description = "HLR/交换机类型") @PathVariable Integer hlrType) {
        List<HlrSwitch> list = hlrSwitchService.listByType(hlrType);
        return CommonResult.success(list);
    }

    /**
     * 获取所有启用的HLR/交换机
     */
    @Operation(summary = "获取所有启用的HLR/交换机")
    @GetMapping("/listAllEnabled")
    public CommonResult<List<HlrSwitch>> listAllEnabled() {
        List<HlrSwitch> list = hlrSwitchService.listAllEnabled();
        return CommonResult.success(list);
    }
}