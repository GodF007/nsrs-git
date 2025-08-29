package com.nsrs.simcard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.constants.ApiDescriptionConstants;
import com.nsrs.simcard.entity.SimCardType;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.service.SimCardTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;

/**
 * SIM Card Type Controller
 */
@RestController
@RequestMapping("/simcard/type")
@Tag(name = "SIM Card Type Management")
public class SimCardTypeController {

    @Autowired
    private SimCardTypeService simCardTypeService;

    /**
     * Paginated Query SIM Card Type List
     */
    @GetMapping("/page")
    @Operation(summary = "Paginated Query SIM Card Type List")
    public CommonResult<PageResult<SimCardType>> page(
            @Parameter(description = ApiDescriptionConstants.PAGE_NUM) @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = ApiDescriptionConstants.PAGE_SIZE) @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = ApiDescriptionConstants.TYPE_NAME) @RequestParam(required = false) String typeName,
            @Parameter(description = ApiDescriptionConstants.TYPE_CODE) @RequestParam(required = false) String typeCode,
            @Parameter(description = ApiDescriptionConstants.TYPE_STATUS) @RequestParam(required = false) Integer status) {
        IPage<SimCardType> result = simCardTypeService.getPage(page, size, typeName, typeCode, status);
        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * Get SIM Card Type Details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get SIM Card Type Details")
    public CommonResult<SimCardType> getDetail(@Parameter(description = ApiDescriptionConstants.TYPE_ID) @PathVariable Long id) {
        SimCardType type = simCardTypeService.getById(id);
        return type != null ? CommonResult.success(type) : CommonResult.failed(ErrorMessageEnum.TYPE_NOT_EXISTS.getMessage());
    }

    /**
     * Add SIM Card Type
     */
    @PostMapping("/add")
    @Operation(summary = "Add SIM Card Type")
    public CommonResult<Void> add(@Valid @RequestBody SimCardType type) {
        boolean success = simCardTypeService.save(type);
        return success ? CommonResult.success() : CommonResult.failed(ErrorMessageEnum.ADD_TYPE_FAILED.getMessage());
    }

    /**
     * Update SIM Card Type
     */
    @PutMapping("/update")
    @Operation(summary = "Update SIM Card Type")
    public CommonResult<Void> update(@Valid @RequestBody SimCardType type) {
        boolean success = simCardTypeService.updateById(type);
        return success ? CommonResult.success() : CommonResult.failed(ErrorMessageEnum.UPDATE_TYPE_FAILED.getMessage());
    }

    /**
     * Delete SIM Card Type
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SIM Card Type")
    public CommonResult<Void> delete(@Parameter(description = ApiDescriptionConstants.TYPE_ID) @PathVariable Long id) {
        boolean success = simCardTypeService.removeById(id);
        return success ? CommonResult.success() : CommonResult.failed(ErrorMessageEnum.DELETE_TYPE_FAILED.getMessage());
    }

    /**
     * Get All Enabled SIM Card Types
     */
    @GetMapping("/list")
    @Operation(summary = "Get All Enabled SIM Card Types")
    public CommonResult<List<SimCardType>> listAllEnabled() {
        List<SimCardType> list = simCardTypeService.listAllEnabled();
        return CommonResult.success(list);
    }
}