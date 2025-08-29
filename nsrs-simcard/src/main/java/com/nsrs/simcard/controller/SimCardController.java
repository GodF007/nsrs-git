package com.nsrs.simcard.controller;


import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.utils.ExcelUtils;
import com.nsrs.simcard.dto.BatchAllocateRequest;
import com.nsrs.simcard.dto.BatchRecycleRequest;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.model.dto.SimCardDTO;
import com.nsrs.simcard.model.query.SimCardQuery;
import com.nsrs.simcard.service.SimCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * SIM Card Controller
 */
@Slf4j
@RestController
@RequestMapping("/simcard/card")
@Tag(name = "SIM Card Management")
public class SimCardController {

    @Autowired
    private SimCardService simCardService;

    /**
     * Paginated Query SIM Card List
     */
    @PostMapping("/page")
    @Operation(summary = "Paginated Query SIM Card List")
    public CommonResult<PageResult<SimCardDTO>> page(@Valid @RequestBody PageRequest<SimCardQuery> request) {
        PageResult<SimCardDTO> pageResult = simCardService.pageCard(request);
        return CommonResult.success(pageResult);
    }

    /**
     * Get SIM Card Details
     * 【分表兼容性警告】建议使用 /iccid/{iccid} 接口以确保分表环境下的数据准确性
     */
//    @GetMapping("/{id}")
//    @Operation(summary = "Get SIM Card Details")
//    public CommonResult<SimCardDTO> getDetail(@PathVariable Long id,
//                                             @RequestParam(required = false) String iccid) {
//        // 分表环境兼容性处理：优先使用ICCID查询
//        if (iccid != null && !iccid.trim().isEmpty()) {
//            SimCardDTO cardDTO = simCardService.getCardByIccid(iccid.trim());
//            return cardDTO != null ? CommonResult.success(cardDTO) : CommonResult.failed("SIM card with specified ICCID not found");
//        }
//
//        // 使用ID查询（在分表环境下可能存在问题）
//        SimCardDTO cardDTO = simCardService.getCardDetail(id);
//        return cardDTO != null ? CommonResult.success(cardDTO) : CommonResult.failed("SIM card not found");
//    }

    /**
     * Get SIM Card Details by ICCID
     */
    @GetMapping("/iccid/{iccid}")
    @Operation(summary = "Get SIM Card Details by ICCID")
    public CommonResult<SimCardDTO> getByIccid(@PathVariable String iccid) {
        SimCardDTO cardDTO = simCardService.getCardByIccid(iccid);
        return cardDTO != null ? CommonResult.success(cardDTO) : CommonResult.failed("SIM card with specified ICCID not found");
    }

    /**
     * Delete SIM Card by ICCID (分表友好)
     */
    @DeleteMapping("/iccid/{iccid}")
    @Operation(summary = "Delete SIM Card by ICCID")
    public CommonResult<Void> deleteByIccid(@PathVariable String iccid) {
        return simCardService.deleteSimCardByIccid(iccid) ? 
                CommonResult.success() : CommonResult.failed("Failed to delete SIM card");
    }

    /**
     * Update SIM Card Status by ICCID (分表友好)
     */
    @PutMapping("/iccid/{iccid}/status/{status}")
    @Operation(summary = "Update SIM Card Status by ICCID")
    public CommonResult<Void> updateStatusByIccid(@PathVariable String iccid, @PathVariable Integer status) {
        return simCardService.updateStatusByIccid(iccid, status) ? 
                CommonResult.success() : CommonResult.failed("Failed to update SIM card status");
    }

    /**
     * Activate SIM Card by ICCID (分表友好)
     */
    @PutMapping("/iccid/{iccid}/activate")
    @Operation(summary = "Activate SIM Card by ICCID")
    public CommonResult<Void> activateCardByIccid(@PathVariable String iccid, @RequestParam Long operatorUserId) {
        return simCardService.activateCardByIccid(iccid, operatorUserId) ? 
                CommonResult.success() : CommonResult.failed("Failed to activate SIM card");
    }

    /**
     * Deactivate SIM Card by ICCID (分表友好)
     */
    @PutMapping("/iccid/{iccid}/deactivate")
    @Operation(summary = "Deactivate SIM Card by ICCID")
    public CommonResult<Void> deactivateCardByIccid(@PathVariable String iccid, @RequestParam Long operatorUserId) {
        return simCardService.deactivateCardByIccid(iccid, operatorUserId) ? 
                CommonResult.success() : CommonResult.failed("Failed to deactivate SIM card");
    }

    /**
     * Add SIM Card
     */
    @PostMapping("/add")
    @Operation(summary = "Add SIM Card")
    public CommonResult<Void> add(@Valid @RequestBody SimCardDTO cardDTO) {
        return simCardService.addCard(cardDTO) ? CommonResult.success() : CommonResult.failed("Failed to add SIM card");
    }

    /**
     * Update SIM Card
     */
    @PutMapping("/update")
    @Operation(summary = "Update SIM Card")
    public CommonResult<Void> update(@Valid @RequestBody SimCardDTO cardDTO) {
        return simCardService.updateCard(cardDTO) ? CommonResult.success() : CommonResult.failed("Failed to update SIM card");
    }

    /**
     * Delete SIM Card
     * 【分表兼容性警告】建议提供ICCID参数以确保分表环境下的数据准确性
     */
//    @DeleteMapping("/{id}")
//    @Operation(summary = "Delete SIM Card")
//    public CommonResult<Void> delete(@PathVariable Long id,
//                                    @RequestParam(required = false) String iccid) {
//        // 分表环境兼容性处理：如果提供了ICCID，先验证ID和ICCID的匹配性
//        if (iccid != null && !iccid.trim().isEmpty()) {
//            SimCardDTO cardDTO = simCardService.getCardByIccid(iccid.trim());
//            if (cardDTO == null) {
//                return CommonResult.failed("SIM card with specified ICCID not found");
//            }
//            if (!cardDTO.getId().equals(id)) {
//                return CommonResult.failed("ID and ICCID do not match");
//            }
//        }
//
//        return simCardService.deleteCard(id) ? CommonResult.success() : CommonResult.failed("Failed to delete SIM card");
//    }

    /**
     * Update SIM Card Status
     * 【分表兼容性警告】建议提供ICCID参数以确保分表环境下的数据准确性
     */
//    @PutMapping("/{id}/status/{status}")
//    @Operation(summary = "Update SIM Card Status")
//    public CommonResult<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status,
//                                          @RequestParam(required = false) String iccid) {
//        // 分表环境兼容性处理：如果提供了ICCID，先验证ID和ICCID的匹配性
//        if (iccid != null && !iccid.trim().isEmpty()) {
//            SimCardDTO cardDTO = simCardService.getCardByIccid(iccid.trim());
//            if (cardDTO == null) {
//                return CommonResult.failed("SIM card with specified ICCID not found");
//            }
//            if (!cardDTO.getId().equals(id)) {
//                return CommonResult.failed("ID and ICCID do not match");
//            }
//        }
//
//        return simCardService.updateCardStatus(id, status) ? CommonResult.success() : CommonResult.failed("Failed to update SIM card status");
//    }

//    /**
//     * 批量导入SIM卡（已废弃，请使用Excel文件导入）
//     * @deprecated 请使用 /import-excel 接口进行文件导入
//     */
//    @Deprecated
//    @PostMapping("/import")
//    @Operation(summary = "Batch Import SIM Cards (Deprecated)", description = "已废弃，请使用Excel文件导入功能")
//    public CommonResult<Integer> importCards(@RequestBody List<SimCardDTO> cardList) {
//        log.warn("使用了已废弃的JSON导入接口，建议使用Excel文件导入");
//        int successCount = simCardService.importCards(cardList);
//        return CommonResult.success(successCount, "Successfully imported " + successCount + " records");
//    }

    /**
     * Batch Allocate SIM Cards
     * 【分表优化】使用iccids进行批量分配，确保分表环境下的数据准确性
     */
    @PostMapping("/allocate")
    @Operation(summary = "Batch Allocate SIM Cards")
    public CommonResult<Void> allocateCards(@RequestBody @Valid BatchAllocateRequest request) {
        // 验证请求参数
        if (!request.isValid()) {
            return CommonResult.failed("Either iccids or cardIds must be provided");
        }
        
        // 优先使用iccids进行批量操作，兼容分表环境
        if (request.useIccids()) {
            return simCardService.allocateCardsByIccids(request.getIccids(), request.getOrgId(), request.getOperatorUserId()) ? 
                    CommonResult.success() : CommonResult.failed("Failed to allocate SIM cards");
        } else {
            // 兼容旧版本cardIds参数，但添加警告
            log.warn("[Sharding Compatibility Warning] Using cardIds for batch allocation may cause incomplete data in sharded environment. Consider using iccids instead.");
            
            return simCardService.allocateCards(request.getCardIds(), request.getOrgId(), request.getOperatorUserId()) ? 
                    CommonResult.success() : CommonResult.failed("Failed to allocate SIM cards");
        }
    }

    /**
     * Batch Recycle SIM Cards
     */
    /**
     * Batch Recycle SIM Cards
     * 【分表优化】使用iccids进行批量回收，确保分表环境下的数据准确性
     */
    @PostMapping("/recycle")
    @Operation(summary = "Batch Recycle SIM Cards")
    public CommonResult<Void> recycleCards(@RequestBody @Valid BatchRecycleRequest request) {
        // 验证请求参数
        if (!request.isValid()) {
            return CommonResult.failed("Either iccids or cardIds must be provided");
        }
        
        // 优先使用iccids进行批量操作，兼容分表环境
        if (request.useIccids()) {
            return simCardService.recycleCardsByIccids(request.getIccids(), request.getOperatorUserId()) ? 
                    CommonResult.success() : CommonResult.failed("Failed to recycle SIM cards");
        } else {
            // 兼容旧版本cardIds参数，但添加警告
            log.warn("[Sharding Compatibility Warning] Using cardIds for batch recycling may cause incomplete data in sharded environment. Consider using iccids instead.");
            
            return simCardService.recycleCards(request.getCardIds(), request.getOperatorUserId()) ? 
                    CommonResult.success() : CommonResult.failed("Failed to recycle SIM cards");
        }
    }

    /**
     * Activate SIM Card
     * 【分表兼容性警告】建议提供ICCID参数以确保分表环境下的数据准确性
     */
//    @PutMapping("/{id}/activate")
//    @Operation(summary = "Activate SIM Card")
//    public CommonResult<Void> activateCard(@PathVariable Long id, @RequestParam Long operatorUserId,
//                                          @RequestParam(required = false) String iccid) {
//        // 分表环境兼容性处理：如果提供了ICCID，先验证ID和ICCID的匹配性
//        if (iccid != null && !iccid.trim().isEmpty()) {
//            SimCardDTO cardDTO = simCardService.getCardByIccid(iccid.trim());
//            if (cardDTO == null) {
//                return CommonResult.failed("SIM card with specified ICCID not found");
//            }
//            if (!cardDTO.getId().equals(id)) {
//                return CommonResult.failed("ID and ICCID do not match");
//            }
//        }
//
//        return simCardService.activateCard(id, operatorUserId) ?
//                CommonResult.success() : CommonResult.failed("Failed to activate SIM card");
//    }

    /**
     * Deactivate SIM Card
     * 【分表兼容性警告】建议提供ICCID参数以确保分表环境下的数据准确性
     */
//    @PutMapping("/{id}/deactivate")
//    @Operation(summary = "Deactivate SIM Card")
//    public CommonResult<Void> deactivateCard(@PathVariable Long id, @RequestParam Long operatorUserId,
//                                            @RequestParam(required = false) String iccid) {
//        // 分表环境兼容性处理：如果提供了ICCID，先验证ID和ICCID的匹配性
//        if (iccid != null && !iccid.trim().isEmpty()) {
//            SimCardDTO cardDTO = simCardService.getCardByIccid(iccid.trim());
//            if (cardDTO == null) {
//                return CommonResult.failed("SIM card with specified ICCID not found");
//            }
//            if (!cardDTO.getId().equals(id)) {
//                return CommonResult.failed("ID and ICCID do not match");
//            }
//        }
//
//        return simCardService.deactivateCard(id, operatorUserId) ?
//                CommonResult.success() : CommonResult.failed("Failed to deactivate SIM card");
//    }

    /**
     * Count SIM Cards by Status
     */
    @PostMapping("/count/status")
    @Operation(summary = "Count SIM Cards by Status")
    public CommonResult<Map<String, Object>> countByStatus(@RequestBody SimCardQuery query) {
        Map<String, Object> countMap = simCardService.countByStatus(convertToParams(query));
        return CommonResult.success(countMap);
    }

    /**
     * Count SIM Cards by Card Type
     */
    @PostMapping("/count/type")
    @Operation(summary = "Count SIM Cards by Card Type")
    public CommonResult<Map<String, Object>> countByCardType(@RequestBody SimCardQuery query) {
        Map<String, Object> countMap = simCardService.countByCardType(query);
        return CommonResult.success(countMap);
    }

    /**
     * Count SIM Cards by Organization
     */
    @PostMapping("/count/org")
    @Operation(summary = "Count SIM Cards by Organization")
    public CommonResult<Map<String, Object>> countByOrganization(@RequestBody SimCardQuery query) {
        Map<String, Object> countMap = simCardService.countByOrganization(query);
        return CommonResult.success(countMap);
    }
    
    /**
     * Convert Query Conditions to Map Parameters
     */
    private Map<String, Object> convertToParams(SimCardQuery query) {
        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            if (query.getIccid() != null) {
                params.put("iccid", query.getIccid());
            }
            if (query.getImsi() != null) {
                params.put("imsi", query.getImsi());
            }
            if (query.getBatchId() != null) {
                params.put("batchId", query.getBatchId());
            }
            if (query.getOrgId() != null) {
                params.put("orgId", query.getOrgId());
            }
            if (query.getStatus() != null) {
                params.put("status", query.getStatus());
            }
        }
        return params;
    }

    // ========== Excel导入导出功能 ==========

    /**
     * Export Excel
     *
     * @param queryParams Query parameters
     * @param response HTTP response
     */
    @Operation(summary = "Export SIM Card Excel")
    @PostMapping("/export-excel")
    public void exportToExcel(@RequestBody SimCardQuery queryParams, HttpServletResponse response) {
        try {
            // Query data
            List<SimCard> dataList = simCardService.queryForExport(queryParams);

            // Export Excel
            ExcelUtils.exportExcel(dataList, "SIM Card Export", "SIM Cards", SimCard.class, response);
        } catch (Exception e) {
            log.error("Failed to export SIM card Excel", e);
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }

    /**
     * Excel Batch Import SIM Cards
     *
     * @param file Excel file
     * @param batchId Batch ID
     * @return Import result
     */
    @PostMapping("/import-excel")
    @Operation(summary = "Excel Batch Import SIM Cards")
    public CommonResult<String> importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("batchId") Long batchId) {
        try {
            List<SimCard> dataList = ExcelUtils.importExcel(file, SimCard.class);
            if (dataList == null || dataList.isEmpty()) {
                return CommonResult.failed("No data found in Excel file");
            }
            
            // Get current operator ID (simplified handling, should be obtained from security context in practice)
            Long operatorUserId = 1L;
            
            Map<String, Object> result = simCardService.batchImportFromExcel(dataList, batchId, operatorUserId);
            String message = result != null ? "Import successful, processed " + dataList.size() + " records" : "Import failed";
            return CommonResult.success(message);
        } catch (Exception e) {
            log.error("Failed to import SIM cards from Excel", e);
            return CommonResult.failed("Import failed: " + e.getMessage());
        }
    }

    /**
     * Download Import Template
     *
     * @param response HTTP response
     */
    @GetMapping("/download-template")
    @Operation(summary = "Download SIM Card Import Template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            ExcelUtils.downloadTemplate("SIM Card Import Template", "SIM Cards", SimCard.class, response);
        } catch (Exception e) {
            log.error("Failed to download template", e);
            throw new RuntimeException("Template download failed: " + e.getMessage());
        }
    }
}