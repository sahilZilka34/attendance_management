package com.university.attendance.controller;

import com.university.attendance.dto.CreateSessionRequest;
import com.university.attendance.dto.SessionDTO;
import com.university.attendance.entity.AttendanceSession;
import com.university.attendance.entity.Module;
import com.university.attendance.entity.User;
import com.university.attendance.service.AttendanceSessionService;
import com.university.attendance.service.ModuleService;
import com.university.attendance.service.QRCodeService;
import com.university.attendance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceSessionController {
    
    private final AttendanceSessionService sessionService;
    private final ModuleService moduleService;
    private final UserService userService;
    private final QRCodeService qrCodeService;
    
    /**
     * Create a new session
     * POST /api/v1/sessions
     */
    @PostMapping
public ResponseEntity<SessionDTO> createSession(
        @Valid @RequestBody CreateSessionRequest request,
        @RequestParam UUID teacherId) {
    
    Module module = moduleService.getModuleById(request.getModuleId())
        .orElseThrow(() -> new RuntimeException("Module not found"));
    
    User teacher = userService.getUserById(teacherId)
        .orElseThrow(() -> new RuntimeException("Teacher not found"));
    
    AttendanceSession session = new AttendanceSession();
    session.setModule(module);
    session.setTeacher(teacher);
    session.setSessionDate(request.getSessionDate());
    session.setStartTime(request.getStartTime());
    session.setEndTime(request.getEndTime());
    session.setClassroom(request.getClassroom());
    session.setQrValidityMinutes(request.getQrValidityMinutes());
    
    // NEW FIELDS
    session.setLocationRequired(request.getLocationRequired());
    session.setCampusLatitude(request.getCampusLatitude());
    session.setCampusLongitude(request.getCampusLongitude());
    session.setCampusRadiusMeters(request.getCampusRadiusMeters());
    session.setMandatoryAttendance(request.getMandatoryAttendance());
    
    AttendanceSession created = sessionService.createSession(session);
    return new ResponseEntity<>(SessionDTO.fromEntity(created), HttpStatus.CREATED);
}
    
    /**
     * Get session by ID
     * GET /api/v1/sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSessionById(@PathVariable UUID id) {
        AttendanceSession session = sessionService.getSessionById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        return ResponseEntity.ok(SessionDTO.fromEntity(session));
    }
    
    /**
     * Get sessions by teacher
     * GET /api/v1/sessions/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByTeacher(@PathVariable UUID teacherId) {
        List<SessionDTO> sessions = sessionService.getSessionsByTeacher(teacherId)
            .stream()
            .map(SessionDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Get today's sessions for a teacher
     * GET /api/v1/sessions/teacher/{teacherId}/today
     */
    @GetMapping("/teacher/{teacherId}/today")
    public ResponseEntity<List<SessionDTO>> getTodaySessionsForTeacher(@PathVariable UUID teacherId) {
        List<SessionDTO> sessions = sessionService.getTodaySessionsForTeacher(teacherId)
            .stream()
            .map(SessionDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Get sessions by module
     * GET /api/v1/sessions/module/{moduleId}
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<SessionDTO>> getSessionsByModule(@PathVariable UUID moduleId) {
        List<SessionDTO> sessions = sessionService.getSessionsByModule(moduleId)
            .stream()
            .map(SessionDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Get sessions by date
     * GET /api/v1/sessions/date/{date}
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<SessionDTO>> getSessionsByDate(@PathVariable LocalDate date) {
        List<SessionDTO> sessions = sessionService.getSessionsByDate(date)
            .stream()
            .map(SessionDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * Start a session (activate QR code)
     * PUT /api/v1/sessions/{id}/start
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<SessionDTO> startSession(@PathVariable UUID id) {
        AttendanceSession session = sessionService.startSession(id);
        return ResponseEntity.ok(SessionDTO.fromEntity(session));
    }
    
    /**
     * Complete a session
     * PUT /api/v1/sessions/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionDTO> completeSession(@PathVariable UUID id) {
        AttendanceSession session = sessionService.completeSession(id);
        return ResponseEntity.ok(SessionDTO.fromEntity(session));
    }
    
    /**
     * Cancel a session
     * PUT /api/v1/sessions/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionDTO> cancelSession(@PathVariable UUID id) {
        AttendanceSession session = sessionService.cancelSession(id);
        return ResponseEntity.ok(SessionDTO.fromEntity(session));
    }
    
    /**
     * Get QR code for a session
     * GET /api/v1/sessions/{id}/qr
     * Returns PNG image
     */
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> getQRCode(@PathVariable UUID id) {
        // Generate encrypted QR data
        String qrData = qrCodeService.generateQRCodeData(id);
        
        // Generate QR code image (512x512 pixels)
        byte[] qrImage = qrCodeService.generateQRCodeImage(qrData, 512, 512);
        
        // Return as PNG image
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        
        return new ResponseEntity<>(qrImage, headers, HttpStatus.OK);
    }

    /**
 * Get QR code data (for testing - shows encrypted string)
 * GET /api/v1/sessions/{id}/qr-data
 */
    @GetMapping("/{id}/qr-data")
    public ResponseEntity<Map<String, String>> getQRData(@PathVariable UUID id) {
        String qrData = qrCodeService.generateQRCodeData(id);
        return ResponseEntity.ok(Map.of("qrToken", qrData));
    }
}
