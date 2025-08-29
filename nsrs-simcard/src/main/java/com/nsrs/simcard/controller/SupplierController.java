package com.nsrs.simcard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.entity.Supplier;
import com.nsrs.simcard.service.SupplierService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Supplier Controller
 */
@RestController
@RequestMapping("/simcard/supplier")
@Tag(name = "Supplier Management")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * Paginated Query Supplier List
     */
    @GetMapping("/page")
    @Operation(summary = "Paginated Query Supplier List")
    public CommonResult<PageResult<Supplier>> page(
            @Parameter(description = "Page Number") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "Page Size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Supplier Name") @RequestParam(required = false) String supplierName,
            @Parameter(description = "Supplier Code") @RequestParam(required = false) String supplierCode,
            @Parameter(description = "Status") @RequestParam(required = false) Integer status) {
        IPage<Supplier> result = supplierService.getPage(page, size, supplierName, supplierCode, status);
        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * Get Supplier Details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Supplier Details")
    public CommonResult<Supplier> getDetail(@Parameter(description = "Supplier ID") @PathVariable Long id) {
        Supplier supplier = supplierService.getById(id);
        return supplier != null ? CommonResult.success(supplier) : CommonResult.failed("Supplier not found");
    }

    /**
     * Add Supplier
     */
    @PostMapping("/add")
    @Operation(summary = "Add Supplier")
    public CommonResult<Void> add(@RequestBody Supplier supplier) {
        boolean success = supplierService.save(supplier);
        return success ? CommonResult.success() : CommonResult.failed("Failed to add supplier");
    }

    /**
     * Update Supplier
     */
    @PutMapping("/update")
    @Operation(summary = "Update Supplier")
    public CommonResult<Void> update(@RequestBody Supplier supplier) {
        boolean success = supplierService.updateById(supplier);
        return success ? CommonResult.success() : CommonResult.failed("Failed to update supplier");
    }

    /**
     * Delete Supplier
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Supplier")
    public CommonResult<Void> delete(@Parameter(description = "Supplier ID") @PathVariable Long id) {
        boolean success = supplierService.removeById(id);
        return success ? CommonResult.success() : CommonResult.failed("Failed to delete supplier");
    }

    /**
     * Get All Enabled Suppliers
     */
    @GetMapping("/list")
    @Operation(summary = "Get All Enabled Suppliers")
    public CommonResult<List<Supplier>> listAllEnabled() {
        List<Supplier> list = supplierService.listAllEnabled();
        return CommonResult.success(list);
    }
}