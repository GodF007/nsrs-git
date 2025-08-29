package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.NumberPatternQueryEntity;
import com.nsrs.msisdn.entity.NumberPattern;
import com.nsrs.msisdn.service.NumberPatternService;
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
 * 号码模式控制�?
 */
@Tag(name = "号码模式管理")
@RestController
@RequestMapping("/msisdn/numberPattern")
public class NumberPatternController {

    @Autowired
    private NumberPatternService numberPatternService;

    /**
     * 分页查询号码模式
     */
    @Operation(summary = "分页查询号码模式")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberPattern>> page(@Valid @RequestBody PageRequest<NumberPatternQueryEntity> request) {
        NumberPatternQueryEntity query = request.getQuery();
        Page<NumberPattern> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<NumberPattern> pageResult = numberPatternService.pageList(page, 
            query != null ? query.getPatternName() : null,
            null, // levelId 字段在NumberPatternQueryEntity中不存在，传null
            query != null ? query.getStatus() : null);
        return CommonResult.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize()));
    }

    /**
     * 获取号码模式详情
     */
    @Operation(summary = "获取号码模式详情")
    @GetMapping("/{patternId}")
    public CommonResult<NumberPattern> getDetail(@Parameter(description = "模式ID") @PathVariable @NotNull(message = "Pattern ID cannot be null") Long patternId) {
        NumberPattern numberPattern = numberPatternService.getDetail(patternId);
        return CommonResult.success(numberPattern);
    }

    /**
     * 新增号码模式
     */
    @Operation(summary = "新增号码模式")
    @PostMapping("/add")
    public CommonResult<Boolean> add(@Valid @RequestBody NumberPattern numberPattern) {
        boolean result = numberPatternService.add(numberPattern);
        return CommonResult.success(result);
    }

    /**
     * 修改号码模式
     */
    @Operation(summary = "修改号码模式")
    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody NumberPattern numberPattern) {
        boolean result = numberPatternService.update(numberPattern);
        return CommonResult.success(result);
    }

    /**
     * 删除号码模式
     */
    @Operation(summary = "删除号码模式")
    @DeleteMapping("/{patternId}")
    public CommonResult<Boolean> delete(@Parameter(description = "模式ID") @PathVariable @NotNull(message = "Pattern ID cannot be null") Long patternId) {
        boolean result = numberPatternService.delete(patternId);
        return CommonResult.success(result);
    }

    /**
     * 启用号码模式
     */
    @Operation(summary = "启用号码模式")
    @PutMapping("/enable/{patternId}")
    public CommonResult<Boolean> enable(@Parameter(description = "模式ID") @PathVariable @NotNull(message = "Pattern ID cannot be null") Long patternId) {
        boolean result = numberPatternService.enable(patternId);
        return CommonResult.success(result);
    }

    /**
     * 禁用号码模式
     */
    @Operation(summary = "禁用号码模式")
    @PutMapping("/disable/{patternId}")
    public CommonResult<Boolean> disable(@Parameter(description = "模式ID") @PathVariable @NotNull(message = "Pattern ID cannot be null") Long patternId) {
        boolean result = numberPatternService.disable(patternId);
        return CommonResult.success(result);
    }

    /**
     * 根据级别ID获取号码模式列表
     */
    @Operation(summary = "根据级别ID获取号码模式列表")
    @GetMapping("/listByLevelId/{levelId}")
    public CommonResult<List<NumberPattern>> listByLevelId(@Parameter(description = "级别ID") @PathVariable @NotNull(message = "Level ID cannot be null") Long levelId) {
        List<NumberPattern> list = numberPatternService.listByLevelId(levelId);
        return CommonResult.success(list);
    }

    /**
     * 获取所有启用的号码模式
     */
    @Operation(summary = "获取所有启用的号码模式")
    @GetMapping("/listAllEnabled")
    public CommonResult<List<NumberPattern>> listAllEnabled() {
        List<NumberPattern> list = numberPatternService.listAllEnabled();
        return CommonResult.success(list);
    }

    /**
     * 验证号码是否符合指定的号码模式
     */
    @Operation(summary = "验证号码是否符合指定的号码模式")
    @GetMapping("/validateNumber")
    public CommonResult<Boolean> validateNumber(
            @Parameter(description = "号码") @RequestParam @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "模式ID") @RequestParam @NotNull(message = "Pattern ID cannot be null") Long patternId) {
        boolean result = numberPatternService.validateNumber(number, patternId);
        return CommonResult.success(result);
    }
}