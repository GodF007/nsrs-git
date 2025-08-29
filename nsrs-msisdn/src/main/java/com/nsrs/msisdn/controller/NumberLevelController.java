package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.NumberLevelQueryEntity;
import com.nsrs.msisdn.entity.NumberLevel;
import com.nsrs.msisdn.service.NumberLevelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 号码级别控制�?
 */
@Tag(name = "号码级别管理")
@RestController
@RequestMapping("/msisdn/numberLevel")
public class NumberLevelController {

    @Autowired
    private NumberLevelService numberLevelService;

    /**
     * 分页查询号码级别
     */
    @Operation(summary = "分页查询号码级别")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberLevel>> page(@Valid @RequestBody PageRequest<NumberLevelQueryEntity> request) {
        NumberLevelQueryEntity query = request.getQuery();
        Page<NumberLevel> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<NumberLevel> pageResult = numberLevelService.pageList(page, 
            query != null ? query.getLevelName() : null,
            query != null ? query.getLevelCode() : null,
            query != null ? query.getStatus() : null);
        return CommonResult.success(new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize()));
    }

    /**
     * 获取号码级别详情
     */
    @Operation(summary = "获取号码级别详情")
    @GetMapping("/{levelId}")
    public CommonResult<NumberLevel> getDetail(@Parameter(description = "级别ID") @PathVariable Long levelId) {
        NumberLevel numberLevel = numberLevelService.getDetail(levelId);
        return CommonResult.success(numberLevel);
    }

    /**
     * 根据级别代码获取号码级别
     */
    @Operation(summary = "根据级别代码获取号码级别")
    @GetMapping("/getByCode/{levelCode}")
    public CommonResult<NumberLevel> getByLevelCode(@Parameter(description = "级别代码") @PathVariable String levelCode) {
        NumberLevel numberLevel = numberLevelService.getByLevelCode(levelCode);
        return CommonResult.success(numberLevel);
    }

    /**
     * 新增号码级别
     */
    @Operation(summary = "新增号码级别")
    @PostMapping("/add")
    public CommonResult<Boolean> add(@Valid @RequestBody NumberLevel numberLevel) {
        boolean result = numberLevelService.add(numberLevel);
        return CommonResult.success(result);
    }

    /**
     * 修改号码级别
     */
    @Operation(summary = "修改号码级别")
    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody NumberLevel numberLevel) {
        boolean result = numberLevelService.update(numberLevel);
        return CommonResult.success(result);
    }

    /**
     * 删除号码级别
     */
    @Operation(summary = "删除号码级别")
    @DeleteMapping("/{levelId}")
    public CommonResult<Boolean> delete(@Parameter(description = "级别ID") @PathVariable Long levelId) {
        boolean result = numberLevelService.delete(levelId);
        return CommonResult.success(result);
    }

    /**
     * 启用号码级别
     */
    @Operation(summary = "启用号码级别")
    @PutMapping("/enable/{levelId}")
    public CommonResult<Boolean> enable(@Parameter(description = "级别ID") @PathVariable Long levelId) {
        boolean result = numberLevelService.enable(levelId);
        return CommonResult.success(result);
    }

    /**
     * 禁用号码级别
     */
    @Operation(summary = "禁用号码级别")
    @PutMapping("/disable/{levelId}")
    public CommonResult<Boolean> disable(@Parameter(description = "级别ID") @PathVariable Long levelId) {
        boolean result = numberLevelService.disable(levelId);
        return CommonResult.success(result);
    }

    /**
     * 获取所有启用的号码级别
     */
    @Operation(summary = "获取所有启用的号码级别")
    @GetMapping("/listAllEnabled")
    public CommonResult<List<NumberLevel>> listAllEnabled() {
        List<NumberLevel> list = numberLevelService.listAllEnabled();
        return CommonResult.success(list);
    }

    /**
     * 获取需要审批的号码级别列表
     */
    @Operation(summary = "获取需要审批的号码级别列表")
    @GetMapping("/listNeedApproval")
    public CommonResult<List<NumberLevel>> listNeedApproval() {
        List<NumberLevel> list = numberLevelService.listNeedApproval();
        return CommonResult.success(list);
    }
}