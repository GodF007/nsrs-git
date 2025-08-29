package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.RegionQueryEntity;
import com.nsrs.msisdn.entity.Region;
import com.nsrs.msisdn.service.RegionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 区域控制�?
 */
@Tag(name = "区域管理")
@RestController
@RequestMapping("/msisdn/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    /**
     * 分页查询区域
     */
    @Operation(summary = "分页查询区域")
    @PostMapping("/page")
    public CommonResult<PageResult<Region>> page(@Valid @RequestBody PageRequest<RegionQueryEntity> request) {
        RegionQueryEntity query = request.getQuery();
        Page<Region> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<Region> pageResult = regionService.pageList(page, 
            query != null ? query.getRegionCode() : null,
            query != null ? query.getRegionName() : null,
            query != null ? query.getRegionType() : null,
            query != null ? query.getStatus() : null);
        return CommonResult.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize()));
    }

    /**
     * 获取区域详情
     */
    @Operation(summary = "获取区域详情")
    @GetMapping("/{regionId}")
    public CommonResult<Region> getDetail(@Parameter(description = "地区ID") @PathVariable Long regionId) {
        Region region = regionService.getDetail(regionId);
        return CommonResult.success(region);
    }

    /*
     * 根据区域代码获取区域
    @Operation(summary = "根据区域代码获取区域")
    @GetMapping("/getByCode/{regionCode}")
    public CommonResult<Region> getByRegionCode(@Parameter(description = "区域代码") @PathVariable String regionCode) {
        Region region = regionService.getByRegionCode(regionCode);
        return CommonResult.success(region);
    }*/

    /**
     * 新增区域
     */
    @Operation(summary = "新增区域")
    @PostMapping("/add")
    public CommonResult<Boolean> add(@Valid @RequestBody Region region) {
        boolean result = regionService.add(region);
        return CommonResult.success(result);
    }

    /**
     * 修改区域
     */
    @Operation(summary = "修改区域")
    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody Region region) {
        boolean result = regionService.update(region);
        return CommonResult.success(result);
    }

    /**
     * 删除区域
     */
    @Operation(summary = "删除区域")
    @DeleteMapping("/{regionId}")
    public CommonResult<Boolean> delete(@Parameter(description = "区域ID") @PathVariable Long regionId) {
        boolean result = regionService.delete(regionId);
        return CommonResult.success(result);
    }

    /**
     * 启用区域
     */
    @Operation(summary = "启用区域")
    @PutMapping("/enable/{regionId}")
    public CommonResult<Boolean> enable(@Parameter(description = "区域ID") @PathVariable Long regionId) {
        boolean result = regionService.enable(regionId);
        return CommonResult.success(result);
    }

    /**
     * 禁用区域
     */
    @Operation(summary = "禁用区域")
    @PutMapping("/disable/{regionId}")
    public CommonResult<Boolean> disable(@Parameter(description = "区域ID") @PathVariable Long regionId) {
        boolean result = regionService.disable(regionId);
        return CommonResult.success(result);
    }

    /**
     * 获取子区域列�?
     */
    @Operation(summary = "获取子区域列表")
    @GetMapping("/listByParentId/{parentId}")
    public CommonResult<List<Region>> listByParentId(@Parameter(description = "父区域ID") @PathVariable Long parentId) {
        List<Region> list = regionService.listByParentId(parentId);
        return CommonResult.success(list);
    }

    /**
     * 获取区域树
     */
    @Operation(summary = "获取区域树")
    @GetMapping("/tree")
    public CommonResult<List<Region>> listRegionTree(
            @Parameter(description = "父区域ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "区域类型") @RequestParam(required = false) Integer regionType) {
        List<Region> tree = regionService.listRegionTree(parentId, regionType);
        return CommonResult.success(tree);
    }

    /**
     * 获取所有启用的区域
     */
    @Operation(summary = "获取所有启用的区域 -- 按需选择，不一定有用")
    @GetMapping("/listAllEnabled")
    public CommonResult<List<Region>> listAllEnabled() {
        List<Region> list = regionService.listAllEnabled();
        return CommonResult.success(list);
    }

}