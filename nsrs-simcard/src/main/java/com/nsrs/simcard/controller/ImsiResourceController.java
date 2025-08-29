package com.nsrs.simcard.controller;

import com.nsrs.common.controller.BaseExcelController;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.utils.ExcelUtils;
import com.nsrs.simcard.entity.ImsiResource;
import com.nsrs.simcard.model.dto.ImsiResourceDTO;
import com.nsrs.simcard.model.query.ImsiResourceQuery;
import com.nsrs.simcard.model.request.ImsiGenerateRequest;
import com.nsrs.simcard.service.ImsiResourceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IMSI Resource Controller
 */
@RestController
@RequestMapping("/simcard/imsi-resource")
@Tag(name = "IMSI Resource Management")
public class ImsiResourceController extends BaseExcelController<ImsiResource, ImsiResourceQuery> {
    
    @Autowired
    private ImsiResourceService imsiResourceService;
    
    /**
     * 分页查询IMSI资源列表
     */
    @PostMapping("/page")
    @Operation(summary = "Paginated Query IMSI Resource List")
    public CommonResult<PageResult<ImsiResourceDTO>> page(@Valid @RequestBody PageRequest<ImsiResourceQuery> request) {
        PageResult<ImsiResourceDTO> pageResult = imsiResourceService.pageImsiResource(request);
        return CommonResult.success(pageResult);
    }
    
    /**
     * 根据IMSI号码获取资源详情
     */
    @GetMapping("/imsi/{imsi}")
    @Operation(summary = "根据IMSI号码获取资源详情")
    public CommonResult<ImsiResourceDTO> getByImsi(@Parameter(description = "IMSI号码") @PathVariable String imsi) {
        ImsiResourceDTO resourceDTO = imsiResourceService.getImsiResourceByImsi(imsi);
        return resourceDTO != null ? CommonResult.success(resourceDTO) : CommonResult.failed("IMSI resource not found");
    }
    
    /**
     * Add IMSI Resource
     */
    @PostMapping("/add")
    @Operation(summary = "Add IMSI Resource")
    public CommonResult<Void> add(@Valid @RequestBody ImsiResourceDTO resourceDTO) {
        return imsiResourceService.addImsiResource(resourceDTO) ? CommonResult.success() : CommonResult.failed("Failed to add IMSI resource");
    }

    /**
     * Update IMSI Resource by IMSI
     */
    @PutMapping("/imsi/{imsi}/update")
    @Operation(summary = "Update IMSI Resource by IMSI")
    public CommonResult<Void> updateByImsi(
            @Parameter(description = "IMSI号码") @PathVariable String imsi,
            @Valid @RequestBody ImsiResourceDTO resourceDTO) {
        resourceDTO.setImsi(imsi);
        return imsiResourceService.updateImsiResourceByImsi(resourceDTO) ? CommonResult.success() : CommonResult.failed("Failed to update IMSI resource");
    }
    
    /**
     * Delete IMSI Resource by IMSI
     */
    @DeleteMapping("/imsi/{imsi}")
    @Operation(summary = "Delete IMSI Resource by IMSI")
    public CommonResult<Void> deleteByImsi(@Parameter(description = "IMSI号码") @PathVariable String imsi) {
        return imsiResourceService.deleteImsiResourceByImsi(imsi) ? CommonResult.success() : CommonResult.failed("Failed to delete IMSI resource");
    }
    
    /**
     * Update IMSI Status by IMSI
     */
    @PutMapping("/imsi/{imsi}/status/{status}")
    @Operation(summary = "Update IMSI Status by IMSI")
    public CommonResult<Void> updateStatusByImsi(
            @Parameter(description = "IMSI号码") @PathVariable String imsi,
            @Parameter(description = "状态：1-空闲，2-已绑定，3-已使用，4-已锁定") @PathVariable Integer status) {
        return imsiResourceService.updateImsiStatusByImsi(imsi, status) ? CommonResult.success() : CommonResult.failed("Failed to update IMSI status");
    }
    
    /**
     * 根据IMSI号码批量更新状态
     */
    @PutMapping("/batch/imsi/{status}")
    @Operation(summary = "根据IMSI号码批量更新状态")
    public CommonResult<Void> batchUpdateStatusByImsi(
            @Parameter(description = "状态：1-空闲，2-已绑定，3-已使用，4-已锁定") @PathVariable Integer status,
            @RequestBody List<String> imsiList) {
        return imsiResourceService.batchUpdateImsiStatusByImsi(imsiList, status) ? CommonResult.success() : CommonResult.failed("Failed to batch update IMSI status by IMSI");
    }
    
    /**
     * 生成IMSI资源
     */
    @PostMapping("/generate")
    @Operation(summary = "生成IMSI资源")
    public CommonResult<List<ImsiResourceDTO>> generate(@Valid @RequestBody ImsiGenerateRequest request) {
        List<ImsiResourceDTO> generatedImsis = imsiResourceService.generateImsi(request);
        return CommonResult.success(generatedImsis, "Successfully generated " + generatedImsis.size() + " IMSI");
    }
    
    /**
     * 根据组ID获取IMSI资源列表
     */
    @GetMapping("/group/{groupId}")
    @Operation(summary = "根据组ID获取IMSI资源列表")
    public CommonResult<List<ImsiResourceDTO>> listByGroupId(@Parameter(description = "组ID") @PathVariable Long groupId) {
        List<ImsiResourceDTO> list = imsiResourceService.listImsiByGroupId(groupId);
        return CommonResult.success(list);
    }
    
    /**
     * 获取指定组的可用IMSI数量
     */
    @GetMapping("/count/available/{groupId}")
    @Operation(summary = "获取指定组的可用IMSI数量")
    public CommonResult<Integer> countAvailable(@Parameter(description = "组ID") @PathVariable Long groupId) {
        Integer count = imsiResourceService.countAvailableImsiByGroupId(groupId);
        return CommonResult.success(count);
    }

    // ==================== Excel导入导出相关方法 ====================

    @Override
    protected Class<ImsiResource> getEntityClass() {
        return ImsiResource.class;
    }

    @Override
    protected String processImportData(List<ImsiResource> dataList) {
        try {
            // 批量导入IMSI资源
            boolean success = imsiResourceService.batchImportImsiResource(dataList);
            if (success) {
                return "Successfully imported " + dataList.size() + " IMSI resources";
            } else {
                return "Failed to import IMSI resources";
            }
        } catch (Exception e) {
            return "Import failed: " + e.getMessage();
        }
    }

    /**
     * 导出Excel
     *
     * @param queryParams 查询参数
     * @param response HTTP响应
     */
    @Operation(summary = "导出Excel")
    @PostMapping("/export-excel")
    public void exportToExcel(@RequestBody ImsiResourceQuery queryParams, HttpServletResponse response) {
        try {
            // 查询数据
            List<ImsiResource> dataList = queryDataForExport(queryParams);

            // 导出Excel
            ExcelUtils.exportExcel(dataList, getExportTitle(), getSheetName(), getEntityClass(), response);
        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }

    @Override
    protected List<ImsiResource> queryDataForExport(ImsiResourceQuery queryParams) {
        // 查询需要导出的IMSI资源数据
        return imsiResourceService.queryImsiResourceForExport(queryParams);
    }

    @Override
    protected String getExportTitle() {
        return "IMSI资源导出";
    }

    @Override
    protected String getTemplateTitle() {
        return "IMSI资源导入模板";
    }

    @Override
    protected String getSheetName() {
        return "IMSI资源";
    }

}