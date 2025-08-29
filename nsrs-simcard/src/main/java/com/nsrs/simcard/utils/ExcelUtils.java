package com.nsrs.simcard.utils;

import com.nsrs.simcard.dto.SimCardImportItem;
import com.nsrs.simcard.vo.SimCardVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Excel Export and Import Utility Class
 */
public class ExcelUtils {
    
    /**
     * Generate SIM Card Import Template
     *
     * @param response HTTP Response
     * @throws IOException IO Exception
     */
    public static void generateSimCardImportTemplate(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create worksheet
        Sheet sheet = workbook.createSheet("SIM Card Import Template");
            
            // Create header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ICCID");
            headerRow.createCell(1).setCellValue("IMSI");
            
            // Set header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 2; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }
            
            // Add description row
            Row noteRow = sheet.createRow(1);
            noteRow.createCell(0).setCellValue("Required, 19-20 digits");
        noteRow.createCell(1).setCellValue("Optional, 15 digits");
            
            // Set filename and response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("SIM_Card_Import_Template.xlsx", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            
            // Write to response stream
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        }
    }
    
    /**
     * Parse SIM Card Import File
     *
     * @param file Import File
     * @return SIM Card Import Item List
     * @throws IOException IO Exception
     */
    public static List<SimCardImportItem> parseSimCardImportFile(MultipartFile file) throws IOException {
        List<SimCardImportItem> importItems = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            // Get first worksheet
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header and description rows, start parsing from row 3
            int startRow = 2;
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Skip first two rows
            if (rowIterator.hasNext()) rowIterator.next();
            if (rowIterator.hasNext()) rowIterator.next();
            
            // Read data rows
            int rowNum = startRow;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                SimCardImportItem item = new SimCardImportItem();
                item.setRowNum(rowNum - startRow + 1);
                
                // Read ICCID
                Cell iccidCell = row.getCell(0);
                if (iccidCell != null) {
                    if (iccidCell.getCellType() == CellType.NUMERIC) {
                        item.setIccid(String.valueOf((long) iccidCell.getNumericCellValue()));
                    } else {
                        item.setIccid(iccidCell.getStringCellValue().trim());
                    }
                }
                
                // Read IMSI
                Cell imsiCell = row.getCell(1);
                if (imsiCell != null) {
                    if (imsiCell.getCellType() == CellType.NUMERIC) {
                        item.setImsi(String.valueOf((long) imsiCell.getNumericCellValue()));
                    } else {
                        item.setImsi(imsiCell.getStringCellValue().trim());
                    }
                }
                
                importItems.add(item);
                rowNum++;
            }
        }
        
        return importItems;
    }
    
    /**
     * Export SIM Card Data
     *
     * @param response HTTP Response
     * @param simCardVOs SIM Card VO List
     * @param fileName File Name
     * @throws IOException IO Exception
     */
    public static void exportSimCards(HttpServletResponse response, List<SimCardVO> simCardVOs, String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create worksheet
        Sheet sheet = workbook.createSheet("SIM Card List");
            
            // Create header
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ICCID");
            headerRow.createCell(1).setCellValue("IMSI");
            headerRow.createCell(2).setCellValue("Batch Code");
        headerRow.createCell(3).setCellValue("Batch Name");
        headerRow.createCell(4).setCellValue("Card Type");
        headerRow.createCell(5).setCellValue("Specification");
        headerRow.createCell(6).setCellValue("Data Type");
        headerRow.createCell(7).setCellValue("Supplier");
        headerRow.createCell(8).setCellValue("Organization");
        headerRow.createCell(9).setCellValue("Status");
        headerRow.createCell(10).setCellValue("Remark");
            
            // Set header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < 11; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            
            // Fill data rows
            int rowNum = 1;
            for (SimCardVO simCardVO : simCardVOs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(simCardVO.getIccid());
                row.createCell(1).setCellValue(simCardVO.getImsi() != null ? simCardVO.getImsi() : "");
                row.createCell(2).setCellValue(simCardVO.getBatchCode());
                row.createCell(3).setCellValue(simCardVO.getBatchName());
                row.createCell(4).setCellValue(simCardVO.getCardTypeName());
                row.createCell(5).setCellValue(simCardVO.getSpecName() != null ? simCardVO.getSpecName() : "");
                row.createCell(6).setCellValue(simCardVO.getDataTypeName() != null ? simCardVO.getDataTypeName() : "");
                row.createCell(7).setCellValue(simCardVO.getSupplierName());
                row.createCell(8).setCellValue(simCardVO.getOrgName() != null ? simCardVO.getOrgName() : "");
                row.createCell(9).setCellValue(simCardVO.getStatusName());
                row.createCell(10).setCellValue(simCardVO.getRemark() != null ? simCardVO.getRemark() : "");
            }
            
            // Set filename and response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
            response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName);
            
            // Write to response stream
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
            }
        }
    }
}