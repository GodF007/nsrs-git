package com.nsrs.common.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Excel工具类
 * 基于EasyPOI实现通用的Excel导入导出功能
 *
 * @author nsrs
 */
@Slf4j
public class ExcelUtils {

    /**
     * 导入Excel数据
     *
     * @param file Excel文件
     * @param clazz 实体类
     * @param titleRows 标题行数
     * @param headRows 表头行数
     * @param <T> 泛型类型
     * @return 导入的数据列表
     */
    public static <T> List<T> importExcel(MultipartFile file, Class<T> clazz, int titleRows, int headRows) {
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(titleRows);
            params.setHeadRows(headRows);
            
            return ExcelImportUtil.importExcel(file.getInputStream(), clazz, params);
        } catch (Exception e) {
            log.error("Excel import failed", e);
            throw new RuntimeException("Excel import failed: " + e.getMessage());
        }
    }

    /**
     * 导入Excel数据（默认参数）
     *
     * @param file Excel文件
     * @param clazz 实体类
     * @param <T> 泛型类型
     * @return 导入的数据列表
     */
    public static <T> List<T> importExcel(MultipartFile file, Class<T> clazz) {
        return importExcel(file, clazz, 1, 1);
    }

    /**
     * 导出Excel数据
     *
     * @param list 数据列表
     * @param title 标题
     * @param sheetName 工作表名称
     * @param clazz 实体类
     * @param response HTTP响应
     * @param <T> 泛型类型
     */
    public static <T> void exportExcel(List<T> list, String title, String sheetName, Class<T> clazz, HttpServletResponse response) {
        try {
            // 处理null值，设置默认值
            String safeTitle = title != null ? title : "Export";
            String safeSheetName = sheetName != null ? sheetName : "Sheet1";
            
            ExportParams exportParams = new ExportParams(safeTitle, safeSheetName);
            exportParams.setType(ExcelType.XSSF);
            
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, clazz, list);
            
            // 设置响应头
            String fileName = URLEncoder.encode(safeTitle + ".xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("Excel export failed", e);
            throw new RuntimeException("Excel export failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excel export failed with unexpected error", e);
            throw new RuntimeException("Excel export failed: " + e.getMessage());
        }
    }

    /**
     * 下载Excel模板
     *
     * @param title 标题
     * @param sheetName 工作表名称
     * @param clazz 实体类
     * @param response HTTP响应
     * @param <T> 泛型类型
     */
    public static <T> void downloadTemplate(String title, String sheetName, Class<T> clazz, HttpServletResponse response) {
        try {
            // 处理null值，设置默认值
            String safeTitle = title != null ? title : "Template";
            String safeSheetName = sheetName != null ? sheetName : "Sheet1";
            
            ExportParams exportParams = new ExportParams(safeTitle, safeSheetName);
            exportParams.setType(ExcelType.XSSF);
            
            // 创建空的工作簿作为模板
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, clazz, Collections.emptyList());
            
            // 设置响应头
            String fileName = URLEncoder.encode(safeTitle + "_template.xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("Template download failed", e);
            throw new RuntimeException("Template download failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Template download failed with unexpected error", e);
            throw new RuntimeException("Template download failed: " + e.getMessage());
        }
    }

    /**
     * 下载Excel模板（带示例数据）
     *
     * @param response HTTP响应
     * @param clazz 实体类
     * @param exampleData 示例数据
     * @param title 标题
     * @param fileName 文件名（不含扩展名）
     * @param <T> 泛型类型
     */
    public static <T> void downloadTemplate(HttpServletResponse response, Class<T> clazz, List<T> exampleData, String title, String fileName) {
        try {
            // 处理null值，设置默认值
            String safeTitle = title != null ? title : "Template";
            String safeFileName = fileName != null ? fileName : "template";
            
            ExportParams exportParams = new ExportParams(safeTitle, "Sheet1");
            exportParams.setType(ExcelType.XSSF);
            
            // 使用示例数据创建模板
            List<T> templateData = exampleData != null ? exampleData : Collections.emptyList();
            Workbook workbook = ExcelExportUtil.exportExcel(exportParams, clazz, templateData);
            
            // 设置响应头
            String encodedFileName = URLEncoder.encode(safeFileName + ".xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
            
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("Template download failed", e);
            throw new RuntimeException("Template download failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Template download failed with unexpected error", e);
            throw new RuntimeException("Template download failed: " + e.getMessage());
        }
    }

    /**
     * 验证Excel文件格式
     *
     * @param file 文件
     * @return 是否为有效的Excel文件
     */
    public static boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        return fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls");
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}