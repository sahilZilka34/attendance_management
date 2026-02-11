package com.university.attendance.controller;

import com.university.attendance.dto.AttendanceRecordDTO;
import com.university.attendance.dto.ScanQRRequest;
import com.university.attendance.entity.AttendanceRecord;
import com.university.attendance.entity.AttendanceSession;
import com.university.attendance.service.AttendanceRecordService;
import com.university.attendance.service.AttendanceSessionService;
import com.university.attendance.service.QRCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.university.attendance.service.ExcelExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {
    
    private final AttendanceRecordService attendanceService;
    private final QRCodeService qrCodeService;
    private final ExcelExportService excelExportService;
    private final AttendanceSessionService sessionService; // Add this too
    
    /**
     * Scan QR code and mark attendance
     * POST /api/v1/attendance/scan
     * This is the main endpoint students use!
     */
    @PostMapping("/scan")
    public ResponseEntity<AttendanceRecordDTO> scanQRCode(@Valid @RequestBody ScanQRRequest request) {
        // Validate QR code and extract session info
        Map<String, String> qrPayload = qrCodeService.validateQRCode(request.getQrToken());
        UUID sessionId = UUID.fromString(qrPayload.get("sessionId"));
        
        // Mark attendance
        AttendanceRecord record = attendanceService.markAttendance(
            sessionId,
            request.getStudentId(),
            request.getDeviceInfo(),
            request.getLatitude(),
            request.getLongitude()
        );
        
        return new ResponseEntity<>(AttendanceRecordDTO.fromEntity(record), HttpStatus.CREATED);
    }
    
    /**
     * Get attendance records for a session
     * GET /api/v1/attendance/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceBySession(@PathVariable UUID sessionId) {
        List<AttendanceRecordDTO> records = attendanceService.getAttendanceBySession(sessionId)
            .stream()
            .map(AttendanceRecordDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }

    /**
 * Export attendance for a session to Excel
 * GET /api/v1/attendance/session/{sessionId}/export
 */
@GetMapping("/session/{sessionId}/export")
public ResponseEntity<byte[]> exportSessionAttendance(@PathVariable UUID sessionId) throws Exception {
    
    // Get session
    AttendanceSession session = sessionService.getSessionById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
    
    // Get attendance records
    List<AttendanceRecord> records = attendanceService.getAttendanceBySession(sessionId);
    
    // Generate Excel file
    byte[] excelBytes = excelExportService.exportSessionAttendance(session, records);
    
    // Set headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment", 
        "Attendance_" + session.getModule().getModuleCode() + "_" + session.getSessionDate() + ".xlsx");
    
    return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
}
    
    /**
     * Get attendance records for a student
     * GET /api/v1/attendance/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceByStudent(@PathVariable UUID studentId) {
        List<AttendanceRecordDTO> records = attendanceService.getAttendanceByStudent(studentId)
            .stream()
            .map(AttendanceRecordDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }
    
    /**
     * Get attendance for a student in a specific module
     * GET /api/v1/attendance/student/{studentId}/module/{moduleId}
     */
    @GetMapping("/student/{studentId}/module/{moduleId}")
    public ResponseEntity<List<AttendanceRecordDTO>> getStudentAttendanceInModule(
            @PathVariable UUID studentId,
            @PathVariable UUID moduleId) {
        List<AttendanceRecordDTO> records = attendanceService.getStudentAttendanceInModule(studentId, moduleId)
            .stream()
            .map(AttendanceRecordDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(records);
    }
    
    /**
     * Get attendance percentage for a student
     * GET /api/v1/attendance/student/{studentId}/percentage
     */
    @GetMapping("/student/{studentId}/percentage")
    public ResponseEntity<Map<String, Double>> getAttendancePercentage(@PathVariable UUID studentId) {
        Double percentage = attendanceService.calculateAttendancePercentage(studentId);
        return ResponseEntity.ok(Map.of("percentage", percentage));
    }
    
    /**
     * Check if student has marked attendance for a session
     * GET /api/v1/attendance/check?sessionId={sessionId}&studentId={studentId}
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAttendance(
            @RequestParam UUID sessionId,
            @RequestParam UUID studentId) {
        boolean hasAttended = attendanceService.getAttendanceBySessionAndStudent(sessionId, studentId).isPresent();
        return ResponseEntity.ok(Map.of("hasAttended", hasAttended));
    }
}