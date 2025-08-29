package com.nsrs.simcard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.constants.ApiDescriptionConstants;
import com.nsrs.simcard.entity.SimCardSpecification;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.service.SimCardSpecificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;

/**
 * SIM Card Specification Controller
 */
@RestController
@RequestMapping("/simcard/specification")
@Tag(name = "SIM Card Specification Management")
public class SimCardSpecificationController {

    @Autowired
    private SimCardSpecificationService simCardSpecificationService;

    /**
     * Paginated Query SIM Card Specification List
     */
    @GetMapping("/page")
    @Operation(summary = "Paginated Query SIM Card Specification List")
    public CommonResult<PageResult<SimCardSpecification>> page(
            @Parameter(description = ApiDescriptionConstants.PAGE_NUM) @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = ApiDescriptionConstants.PAGE_SIZE) @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = ApiDescriptionConstants.SPEC_NAME) @RequestParam(required = false) String specName,
            @Parameter(description = ApiDescriptionConstants.SPEC_CODE) @RequestParam(required = false) String specCode,
            @Parameter(description = ApiDescriptionConstants.TYPE_ID) @RequestParam(required = false) Long typeId,
            @Parameter(description = ApiDescriptionConstants.SPEC_STATUS) @RequestParam(required = false) Integer status) {
        IPage<SimCardSpecification> result = simCardSpecificationService.getPage(page, size, specName, specCode, typeId, status);
        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * Get SIM Card Specification Details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get SIM Card Specification Details")
    public CommonResult<SimCardSpecification> getDetail(@Parameter(description = ApiDescriptionConstants.SPEC_ID) @PathVariable Long id) {
        SimCardSpecification spec = simCardSpecificationService.getById(id);
        return spec != null ? CommonResult.success(spec) : CommonResult.failed(ErrorMessageEnum.SPECIFICATION_NOT_EXISTS.getMessage());
    }

    /**
     * Add SIM Card Specification
     */
    @PostMapping("/add")
    @Operation(summary = "Add SIM Card Specification")
    public CommonResult<Void> add(@Valid @RequestBody SimCardSpecification specification) {
        boolean success = simCardSpecificationService.save(specification);
        return success ? CommonResult.success() : CommonResult.failed(ErrorMessageEnum.ADD_SPECIFICATION_FAILED.getMessage());
    }

    /**
     * Update SIM Card Specification
     */
    @PutMapping("/update")
    @Operation(summary = "Update SIM Card Specification")
    public CommonResult<Void> update(@Valid @RequestBody SimCardSpecification specification) {
        boolean success = simCardSpecificationService.updateById(specification);
        return success ? CommonResult.success() : CommonResult.failed(ErrorMessageEnum.UPDATE_SPECIFICATION_FAILED.getMessage());
    }

    /**
     * Delete SIM Card Specification
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete SIM Card Specification")
    public CommonResult<Void> delete(@Parameter(description = ApiDescriptionConstants.SPEC_ID) @PathVariable Long id) {
        boolean success = simCardSpecificationService.removeById(id);
        return success ? CommonResult.success() : CommonResult.failed(ErrorMessageEnum.DELETE_SPECIFICATION_FAILED.getMessage());
    }

    /**
     * Get All Enabled SIM Card Specifications
     */
    @GetMapping("/list")
    @Operation(summary = "Get All Enabled SIM Card Specifications")
    public CommonResult<List<SimCardSpecification>> listAllEnabled() {
        List<SimCardSpecification> list = simCardSpecificationService.listAllEnabled();
        return CommonResult.success(list);
    }

    /**
     * Get Specification List by Type ID
     */
    @GetMapping("/list/type/{typeId}")
    @Operation(summary = "Get Specification List by Type ID")
    public CommonResult<List<SimCardSpecification>> listByTypeId(@Parameter(description = ApiDescriptionConstants.TYPE_ID) @PathVariable Long typeId) {
        List<SimCardSpecification> list = simCardSpecificationService.listByTypeId(typeId);
        return CommonResult.success(list);
    }

}