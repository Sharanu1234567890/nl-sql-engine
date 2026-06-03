package com.nlsql.nl_sql_engine.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    public byte[] exportToExcel(List<Map<String, Object>> results, String sql) throws Exception {

        XSSFWorkbook workbook = new XSSFWorkbook();

        // ── Results Sheet ──────────────────────────────────────────
        Sheet dataSheet = workbook.createSheet("Results");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // Alternate row style
        CellStyle altStyle = workbook.createCellStyle();
        altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        altStyle.setBorderBottom(BorderStyle.THIN);
        altStyle.setBorderTop(BorderStyle.THIN);
        altStyle.setBorderLeft(BorderStyle.THIN);
        altStyle.setBorderRight(BorderStyle.THIN);

        if (results != null && !results.isEmpty()) {

            // Header row
            Row headerRow = dataSheet.createRow(0);
            List<String> columns = List.copyOf(results.get(0).keySet());
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).toUpperCase());
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int rowIdx = 0; rowIdx < results.size(); rowIdx++) {
                Row row = dataSheet.createRow(rowIdx + 1);
                Map<String, Object> rowData = results.get(rowIdx);
                CellStyle style = rowIdx % 2 == 0 ? dataStyle : altStyle;
                for (int colIdx = 0; colIdx < columns.size(); colIdx++) {
                    Cell cell = row.createCell(colIdx);
                    Object value = rowData.get(columns.get(colIdx));
                    cell.setCellValue(value != null ? value.toString() : "");
                    cell.setCellStyle(style);
                }
            }

            for (int i = 0; i < columns.size(); i++) {
                dataSheet.autoSizeColumn(i);
            }
        }

        // ── SQL Sheet ──────────────────────────────────────────────
        Sheet sqlSheet = workbook.createSheet("Generated SQL");
        Row sqlLabelRow = sqlSheet.createRow(0);
        sqlLabelRow.createCell(0).setCellValue("Generated SQL:");
        Row sqlValueRow = sqlSheet.createRow(1);
        sqlValueRow.createCell(0).setCellValue(sql != null ? sql : "");
        sqlSheet.autoSizeColumn(0);

        // ── Write to bytes ─────────────────────────────────────────
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}