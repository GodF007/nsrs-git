package com.nsrs.simcard.controller;

import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.constants.ApiDescriptionConstants;
import com.nsrs.simcard.entity.SimCardBatch;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.model.dto.SimCardBatchDTO;
import com.nsrs.simcard.model.query.SimCardBatchQuery;
import com.nsrs.simcard.service.SimCardBatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SIM Card Batch Management Controller
 */
@Tag(name = "SIM Card Batch Management")
@RestController
@RequestMapping("/simcard/sim-card-batch")
@RequiredArgsConstructor
public class SimCardBatchController {

    private final SimCardBatchService simCardBatchService;

    /**
     * Paginated Query SIM Card Batch List
     */
    @Operation(summary = "Paginated Query SIM Card Batch List")
    @PostMapping("/page")
    public CommonResult<PageResult<SimCardBatchDTO>> page(@Valid @RequestBody PageRequest<SimCardBatchQuery> request) {
        PageResult<SimCardBatchDTO> pageResult = simCardBatchService.pageSimCardBatch(request);
        return CommonResult.success(pageResult);
    }

    /**
     * Get SIM Card Batch Details
     */
    @Operation(summary = "Get SIM Card Batch Details")
    @GetMapping("/{id}")
    public CommonResult<SimCardBatchDTO> getById(@Parameter(description = ApiDescriptionConstants.BATCH_ID) @PathVariable Long id) {
        SimCardBatchDTO batch = simCardBatchService.getSimCardBatchDetail(id);
        return CommonResult.success(batch);
    }

    /**
     * 查询所有SIM卡批次列表
     */
    @Operation(summary = "查询所有SIM卡批次列表")
    @GetMapping("/list")
    public CommonResult<List<SimCardBatchDTO>> list() {
        List<SimCardBatch> list = simCardBatchService.listAllBatches();
        // Need to convert SimCardBatch to SimCardBatchDTO
        List<SimCardBatchDTO> dtoList = list.stream()
            .map(batch -> {
                SimCardBatchDTO dto = new SimCardBatchDTO();
                dto.setBatchId(batch.getBatchId());
                dto.setBatchName(batch.getBatchName());
                dto.setBatchCode(batch.getBatchCode());
                dto.setSupplierId(batch.getSupplierId());
                dto.setProductionDate(batch.getProductionDate());
                dto.setImportDate(batch.getImportDate());
                dto.setImportUserId(batch.getImportUserId());
                dto.setTotalCount(batch.getTotalCount());
                dto.setActivatedCount(batch.getActivatedCount());
                dto.setDeactivatedCount(batch.getDeactivatedCount());
                dto.setRecycledCount(batch.getRecycledCount());
                dto.setAvailableCount(batch.getAvailableCount());
                dto.setRemark(batch.getRemark());
                dto.setCreateTime(batch.getCreateTime());
                dto.setUpdateTime(batch.getUpdateTime());
                dto.setCreateUserId(batch.getCreateUserId());
                dto.setUpdateUserId(batch.getUpdateUserId());
                return dto;
            })
            .collect(Collectors.toList());
        return CommonResult.success(dtoList);
    }

    /**
     * Add SIM Card Batch
     * 新增SIM卡批次，必填字段包括：批次名称、批次编码、供应商ID、总数量
     */
    @Operation(summary = "新增SIM卡批次", 
               description = "创建新的SIM卡批次，必填字段：批次名称、批次编码、供应商ID、总数量")
    @PostMapping("/add")
    public CommonResult<Boolean> add(@RequestBody @Validated SimCardBatchDTO batchDTO) {
        boolean result = simCardBatchService.addSimCardBatch(batchDTO);
        return CommonResult.success(result);
    }

    /**
     * Update SIM Card Batch
     */
    @Operation(summary = "Update SIM Card Batch")
    @PutMapping("/update")
    public CommonResult<Boolean> update(@RequestBody @Validated SimCardBatchDTO batchDTO) {
        boolean result = simCardBatchService.updateSimCardBatch(batchDTO);
        return CommonResult.success(result);
    }

    /**
     * Delete SIM Card Batch
     */
    @Operation(summary = "Delete SIM Card Batch")
    @DeleteMapping("/{id}")
    public CommonResult<Boolean> delete(@Parameter(description = ApiDescriptionConstants.BATCH_ID) @PathVariable Long id) {
        boolean result = simCardBatchService.deleteSimCardBatch(id);
        return CommonResult.success(result);
    }

    /**
     * Query Stock Alert Batch List
     */
    @Operation(summary = "Query Stock Alert Batch List")
    @GetMapping("/alert")
    @Deprecated
    public CommonResult<List<SimCardBatchDTO>> listAlerts() {
        // Check stock alert status for all batches
        List<SimCardBatch> allBatches = simCardBatchService.listAllBatches();
        List<SimCardBatchDTO> alertBatches = allBatches.stream()
            .filter(batch -> simCardBatchService.checkStockAlert(batch.getBatchId()))
            .map(batch -> {
                SimCardBatchDTO dto = new SimCardBatchDTO();
                dto.setBatchId(batch.getBatchId());
                dto.setBatchName(batch.getBatchName());
                dto.setBatchCode(batch.getBatchCode());
                dto.setSupplierId(batch.getSupplierId());
                dto.setProductionDate(batch.getProductionDate());
                dto.setImportDate(batch.getImportDate());
                dto.setImportUserId(batch.getImportUserId());
                dto.setTotalCount(batch.getTotalCount());
                dto.setActivatedCount(batch.getActivatedCount());
                dto.setDeactivatedCount(batch.getDeactivatedCount());
                dto.setRecycledCount(batch.getRecycledCount());
                dto.setAvailableCount(batch.getAvailableCount());
                dto.setRemark(batch.getRemark());
                dto.setCreateTime(batch.getCreateTime());
                dto.setUpdateTime(batch.getUpdateTime());
                dto.setCreateUserId(batch.getCreateUserId());
                dto.setUpdateUserId(batch.getUpdateUserId());
                return dto;
            })
            .collect(Collectors.toList());
        return CommonResult.success(alertBatches);
    }

    /**
     * Batch Update Available Count
     */
    @Operation(summary = "Batch Update Available Count")
    @PutMapping("/update-available-count")
    public CommonResult<Boolean> updateAvailableCount(@RequestBody List<SimCardBatchDTO> batchList) {
        // Update available count for each batch individually
        for (SimCardBatchDTO batch : batchList) {
            // Assume DTO has availableCount field representing the increment of available count
            simCardBatchService.updateAvailableCount(batch.getBatchId(), batch.getAvailableCount());
        }
        return CommonResult.success(true);
    }
}