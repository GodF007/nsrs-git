package com.nsrs.common.controller;

import com.nsrs.common.model.CommonResult;
import com.nsrs.common.utils.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Excel导入导出控制器基类
 * 提供通用的Excel导入导出功能
 *
 * @param <T> 实体类型
 * @param <D> DTO类型
 * @author nsrs
 */
@Slf4j
public abstract class BaseExcelController<T, D> {

    /**
     * Excel批量导入
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @Operation(summary = "Excel批量导入")
    @PostMapping("/import-excel")
    public CommonResult<String> importFromExcel(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        try {
            // 验证文件格式
            if (!ExcelUtils.isValidExcelFile(file)) {
                return CommonResult.failed("Invalid Excel file format");
            }
            
            // 导入数据
            List<T> dataList = ExcelUtils.importExcel(file, getEntityClass());
            
            if (dataList == null || dataList.isEmpty()) {
                return CommonResult.failed("No data found in Excel file");
            }
            
            // 处理导入数据
            String result = processImportData(dataList);
            
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("Excel import failed", e);
            return CommonResult.failed("Import failed: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     *
     * @param response HTTP响应
     */
    @Operation(summary = "下载导入模板")
    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            ExcelUtils.downloadTemplate(getTemplateTitle(), getSheetName(), getEntityClass(), response);
        } catch (Exception e) {
            log.error("Template download failed", e);
            throw new RuntimeException("Template download failed: " + e.getMessage());
        }
    }

    /**
     * 获取实体类Class
     *
     * @return 实体类Class
     */
    protected abstract Class<T> getEntityClass();

    /**
     * 处理导入数据
     *
     * @param dataList 导入的数据列表
     * @return 处理结果
     */
    protected abstract String processImportData(List<T> dataList);

    /**
     * 查询导出数据
     *
     * @param queryParams 查询参数
     * @return 数据列表
     */
    protected abstract List<T> queryDataForExport(D queryParams);

    /**
     * 获取导出标题
     *
     * @return 导出标题
     */
    protected abstract String getExportTitle();

    /**
     * 获取模板标题
     *
     * @return 模板标题
     */
    protected abstract String getTemplateTitle();

    /**
     * 获取工作表名称
     *
     * @return 工作表名称
     */
    protected abstract String getSheetName();
}