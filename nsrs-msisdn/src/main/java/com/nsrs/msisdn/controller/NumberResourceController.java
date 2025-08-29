package com.nsrs.msisdn.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.common.controller.BaseExcelController;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.utils.ExcelUtils;
import com.nsrs.msisdn.dto.NumberResourceDTO;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.msisdn.dto.request.NumberResourceQueryEntity;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.service.NumberResourceService;
import com.nsrs.msisdn.vo.NumberResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 号码资源管理接口
 */
@Tag(name = "号码资源管理", description = "号码资源的CRUD操作及分表功能")
@RestController
@RequestMapping("/msisdn/number")
public class NumberResourceController extends BaseExcelController<NumberResource, NumberResourceDTO> {

    @Autowired
    private NumberResourceService numberResourceService;

    /**
     * 分页查询号码资源
     */
    @Operation(summary = "分页查询号码资源")
    @PostMapping("/page")
    public CommonResult<PageResult<NumberResourceVO>> page(@Valid @RequestBody PageRequest<NumberResourceQueryEntity> request) {
                
        // 构建查询参数
        NumberResourceDTO dto = new NumberResourceDTO();
        if (request.getQuery() != null) {
            dto.setNumber(request.getQuery().getNumber());
            dto.setNumberType(request.getQuery().getNumberType());
            dto.setSegmentId(request.getQuery().getSegmentId());
            dto.setLevelId(request.getQuery().getLevelId());
            dto.setPatternId(request.getQuery().getPatternId());
            dto.setHlrSwitchId(request.getQuery().getHlrSwitchId());
            dto.setHlrId(request.getQuery().getHlrSwitchId()); // 同时设置hlrId以保持兼容性
            dto.setAttributiveOrg(request.getQuery().getAttributiveOrg());
            dto.setStatus(request.getQuery().getStatus());
        }
        
        // 构建分页参数
        Page<NumberResource> page = new Page<>(request.getCurrent(), request.getSize());
        
        // 智能选择查询方式：如果是前缀查询且长度小于11位，使用优化版本
        IPage<NumberResourceVO> result;
        if (request.getQuery() != null && StringUtils.isNotBlank(request.getQuery().getNumberPrefix())) {
            // 使用前缀查询优化版本（基于numberPrefix字段）
            dto.setNumber(request.getQuery().getNumberPrefix()); // 设置前缀作为查询条件
            result = numberResourceService.prefixQuery(page, dto);
        } else if (request.getQuery() != null && StringUtils.isNotBlank(request.getQuery().getNumber()) && request.getQuery().getNumber().length() < 11) {
            // 使用前缀查询优化版本
            result = numberResourceService.prefixQuery(page, dto);
        } else {
            // 使用普通查询
            result = numberResourceService.pageQuery(page, dto);
        }

        return CommonResult.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 根据号码查询资源
     */
    @Operation(summary = "根据号码查询详情")
    @GetMapping("/detail/{number}")
    public CommonResult<NumberResourceVO> detail(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number) {
                
        // 执行查询
        NumberResourceVO result = numberResourceService.getByNumber(number);
        
        return CommonResult.success(result);
    }

    /**
     * 新增号码资源
     */
    @Operation(summary = "新增号码资源")
    @PostMapping("/add")
    public CommonResult<Boolean> add(
            @Parameter(description = "号码资源信息") @Valid @RequestBody NumberResourceDTO dto) {
                
        // 执行新增
        boolean result = numberResourceService.add(dto);
        
        return CommonResult.success(result);
    }

    /**
     * 修改号码资源
     */
    @Operation(summary = "修改号码资源")
    @PutMapping("/update/{number}")
    public CommonResult<Boolean> update(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "号码资源信息") @Valid @RequestBody NumberResourceDTO dto) {
                
        // 执行修改
        boolean result = numberResourceService.update(number, dto);
        
        return CommonResult.success(result);
    }

    /**
     * 删除号码资源
     */
    @Operation(summary = "删除号码资源")
    @DeleteMapping("/delete/{number}")
    public CommonResult<Boolean> delete(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number) {
                
        // 执行删除
        boolean result = numberResourceService.delete(number);
        
        return CommonResult.success(result);
    }

    /**
     * 更新号码状态
    @Operation(summary = "更新号码状态")
    @PutMapping("/{number}/status/{status}")
    public CommonResult<Boolean> updateStatus(
            @Parameter(description = "号码") @PathVariable String number,
            @Parameter(description = "状态") @PathVariable Integer status) {
                
        // 执行状态更新
        boolean result = numberResourceService.updateStatus(number, status);
        
        return CommonResult.success(result);
    }*/

    /**
     * 创建号码分表
     */
    /*@Operation(summary = "创建号码分表")
    @PostMapping("/table/{prefix}")
    public CommonResult<Boolean> createTable(
            @Parameter(description = "号码前缀") @PathVariable String prefix) {
                
        // 执行创建分表
        boolean result = numberResourceService.createTable(prefix);
        
        return CommonResult.success(result);
    }*/
    
    /**
     * 自动分类号码
     */
    /*@Operation(summary = "自动分类号码")
    @PutMapping("/{number}/classify")
    public CommonResult<Boolean> autoClassify(
            @Parameter(description = "号码") @PathVariable String number) {
        
        // 执行自动分类
        boolean result = numberResourceService.autoClassify(number);
        
        return CommonResult.success(result);
    }*/
    
    /**
     * 批量自动分类号码
     */
   /* @Operation(summary = "批量自动分类号码")
    @PutMapping("/segment/{segmentId}/classify")
    public CommonResult<Integer> batchAutoClassify(
            @Parameter(description = "号码段ID") @PathVariable Long segmentId) {
        
        // 执行批量自动分类
        int count = numberResourceService.batchAutoClassify(segmentId);
        
        return CommonResult.success(count);
    }*/
    
    /**
     * 预留号码
     */
    @Operation(summary = "预留号码")
    @PutMapping("/{number}/reserve")
    public CommonResult<Boolean> reserve(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行预留
        boolean result = numberResourceService.reserve(number, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 分配号码
     */
    @Operation(summary = "分配号码")
    @PutMapping("/{number}/assign")
    public CommonResult<Boolean> assign(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "归属组织") @RequestParam @NotBlank(message = "Attributive organization cannot be blank") String attributiveOrg,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行分配
        boolean result = numberResourceService.assign(number, attributiveOrg, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 激活号码
     */
    @Operation(summary = "激活号码")
    @PutMapping("/{number}/activate")
    public CommonResult<Boolean> activate(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "ICCID") @RequestParam(required = false) String iccid,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行激活
        boolean result = numberResourceService.activate(number, iccid, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 冻结号码
     */
    @Operation(summary = "冻结号码")
    @PutMapping("/{number}/freeze")
    public CommonResult<Boolean> freeze(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行冻结
        boolean result = numberResourceService.freeze(number, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 解冻号码
     */
    @Operation(summary = "解冻号码")
    @PutMapping("/{number}/unfreeze")
    public CommonResult<Boolean> unfreeze(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行解冻
        boolean result = numberResourceService.unfreeze(number, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 释放号码
     */
    @Operation(summary = "释放号码")
    @PutMapping("/{number}/release")
    public CommonResult<Boolean> release(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行释放
        boolean result = numberResourceService.release(number, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 回收号码
     */
    @Operation(summary = "回收号码")
    @PutMapping("/{number}/recycle")
    public CommonResult<Boolean> recycle(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行回收
        boolean result = numberResourceService.recycle(number, remark);
        
        return CommonResult.success(result);
    }
    
    /**
     * 批量操作号码
     */
    @Operation(summary = "批量操作号码")
    @PutMapping("/batch-operation")
    public CommonResult<Integer> batchOperation(
            @Parameter(description = "号码列表") @RequestBody @NotNull(message = "Numbers list cannot be null") List<String> numbers,
            @Parameter(description = "操作类型：1-创建，2-预留，3-分配，4-激活，5-冻结，6-解冻，7-释放，8-回收, 10-删除")
            @RequestParam @NotNull(message = "Operation type cannot be null") Integer operationType,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        
        // 执行批量操作
        int count = numberResourceService.batchOperation(numbers, operationType, remark);
        
        return CommonResult.success(count);
    }
    
    /**
     * 获取号码详情
     */
    @Operation(summary = "获取号码基础信息")
    @GetMapping("/basic/{number}")
    public CommonResult<NumberResource> getBasicInfo(
            @Parameter(description = "号码") @PathVariable @NotBlank(message = "Number cannot be blank") String number) {
        
        // 获取号码详情
        NumberResource resource = numberResourceService.getBasicInfo(number);
        
        return CommonResult.success(resource);
    }
    
    /**
     * 获取号码统计信息
     */
    @Operation(summary = "获取号码统计信息")
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getStatistics() {
        
        // 获取统计信息
        Map<String, Object> statistics = numberResourceService.getStatistics();
        
        return CommonResult.success(statistics);
    }

    // ========== BaseExcelController抽象方法实现 ==========

    @Override
    protected Class<NumberResource> getEntityClass() {
        return NumberResource.class;
    }

    @Override
    protected String processImportData(List<NumberResource> dataList) {
        try {
            boolean result = numberResourceService.batchImport(dataList);
            return result ? "Import successful, processed " + dataList.size() + " records" 
                         : "Import failed";
        } catch (Exception e) {
            throw new RuntimeException("Import processing failed: " + e.getMessage());
        }
    }

    @Override
    protected List<NumberResource> queryDataForExport(NumberResourceDTO queryParams) {
        return numberResourceService.queryForExport(queryParams);
    }

    @Override
    protected String getExportTitle() {
        return "NumberResourceExport";
    }

    @Override
    protected String getTemplateTitle() {
        return "NumberResourceImportTemplate";
    }

    @Override
    protected String getSheetName() {
        return "Number Resources";
    }

    /**
     * 导出Excel
     *
     * @param queryParams 查询参数
     * @param response HTTP响应
     */
    @Operation(summary = "导出Excel")
    @PostMapping("/export-excel")
    public void exportToExcel(@RequestBody NumberResourceDTO queryParams, HttpServletResponse response) {
        try {
            List<NumberResource> dataList = queryDataForExport(queryParams);
            ExcelUtils.exportExcel(dataList, getExportTitle(), getSheetName(), getEntityClass(), response);
        } catch (Exception e) {
            throw new RuntimeException("Export failed: " + e.getMessage());
        }
    }

}