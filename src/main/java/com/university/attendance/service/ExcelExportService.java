package com.university.attendance.service;

import com.university.attendance.entity.AttendanceRecord;
import com.university.attendance.entity.AttendanceSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {
    
    /**
     * Export attendance records for a session to Excel
     */
    public byte[] exportSessionAttendance(AttendanceSession session, List<AttendanceRecord> records) throws IOException {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Session Info Section
            int rowNum = 0;
            
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Attendance Report");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // Empty row
            
            // Session details
            createInfoRow(sheet, rowNum++, "Module:", session.getModule().getModuleCode() + " - " + session.getModule().getModuleName());
            createInfoRow(sheet, rowNum++, "Teacher:", session.getTeacher().getFirstName() + " " + session.getTeacher().getLastName());
            createInfoRow(sheet, rowNum++, "Date:", session.getSessionDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            createInfoRow(sheet, rowNum++, "Time:", session.getStartTime() + " - " + session.getEndTime());
            createInfoRow(sheet, rowNum++, "Classroom:", session.getClassroom());
            createInfoRow(sheet, rowNum++, "Total Present:", String.valueOf(records.size()));
            
            rowNum++; // Empty row
            
            // Attendance Table Header
            Row headerRow = sheet.createRow(rowNum++);
            String[] columns = {"No.", "Student Email", "First Name", "Last Name", "Status", "Marked At", "Device Info"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Attendance Data
            int serialNo = 1;
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            
            for (AttendanceRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(serialNo++);
                row.createCell(1).setCellValue(record.getStudent().getEmail());
                row.createCell(2).setCellValue(record.getStudent().getFirstName());
                row.createCell(3).setCellValue(record.getStudent().getLastName());
                row.createCell(4).setCellValue(record.getStatus().toString());
                row.createCell(5).setCellValue(record.getMarkedAt().format(timeFormatter));
                row.createCell(6).setCellValue(record.getDeviceInfo() != null ? record.getDeviceInfo() : "N/A");
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void createInfoRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
}