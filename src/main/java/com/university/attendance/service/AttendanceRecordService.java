package com.university.attendance.service;

import com.university.attendance.entity.AttendanceRecord;
import com.university.attendance.entity.AttendanceSession;
import com.university.attendance.entity.AttendanceStatus;
import com.university.attendance.entity.SessionStatus;
import com.university.attendance.entity.User;
import com.university.attendance.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceRecordService {
    
    private final AttendanceRecordRepository attendanceRepository;
    private final AttendanceSessionService sessionService;
    private final UserService userService;
    
    /**
     * Mark attendance for a student
     * Business Rules:
     * - Session must be ACTIVE
     * - Student cannot mark attendance twice for same session
     * - Attendance marked within time limit is PRESENT, otherwise LATE
     */
    public AttendanceRecord markAttendance(
        UUID sessionId, 
        UUID studentId, 
        String deviceInfo,
        Double latitude,
        Double longitude) {
    
    // Validate session exists and is active
    AttendanceSession session = sessionService.getSessionById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
    
    if (session.getStatus() != SessionStatus.ACTIVE) {
        throw new RuntimeException("Session is not active. Cannot mark attendance.");
    }
    
    // Validate student exists
    User student = userService.getUserById(studentId)
        .orElseThrow(() -> new RuntimeException("Student not found"));
    
    // Check if student already marked attendance
    if (attendanceRepository.existsBySessionAndStudent(session, student)) {
        throw new RuntimeException("Attendance already marked for this session");
    }
    
    // NEW: Validate location if required
    if (session.getLocationRequired()) {
        if (latitude == null || longitude == null) {
            throw new RuntimeException("Location is required for this session");
        }
        
        if (!isWithinCampus(
            latitude, 
            longitude, 
            session.getCampusLatitude(), 
            session.getCampusLongitude(), 
            session.getCampusRadiusMeters())) {
            throw new RuntimeException("You must be on campus to mark attendance. Distance: " 
                + calculateDistance(latitude, longitude, session.getCampusLatitude(), session.getCampusLongitude()) 
                + " meters");
        }
    }
    
    // Create attendance record
    AttendanceRecord record = new AttendanceRecord();
    record.setSession(session);
    record.setStudent(student);
    record.setDeviceInfo(deviceInfo);
    record.setLatitude(latitude);
    record.setLongitude(longitude);
    
    // Determine if PRESENT or LATE based on time
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime sessionStart = LocalDateTime.of(
        session.getSessionDate(), 
        session.getStartTime()
    );
    LocalDateTime lateThreshold = sessionStart.plusMinutes(session.getQrValidityMinutes());
    
    if (now.isBefore(lateThreshold)) {
        record.setStatus(AttendanceStatus.PRESENT);
    } else {
        record.setStatus(AttendanceStatus.LATE);
    }
    
    return attendanceRepository.save(record);
}

// NEW: Helper method to check if student is within campus
private boolean isWithinCampus(Double studentLat, Double studentLng, 
                               Double campusLat, Double campusLng, 
                               Integer radiusMeters) {
    double distance = calculateDistance(studentLat, studentLng, campusLat, campusLng);
    return distance <= radiusMeters;
}

// NEW: Calculate distance between two GPS coordinates (Haversine formula)
private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
    final int R = 6371000; // Earth's radius in meters
    
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    return R * c; // Distance in meters
}
    
    /**
     * Get all attendance records for a session
     */
    public List<AttendanceRecord> getAttendanceBySession(UUID sessionId) {
        AttendanceSession session = sessionService.getSessionById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        return attendanceRepository.findBySession(session);
    }
    
    /**
     * Get all attendance records for a student
     */
    public List<AttendanceRecord> getAttendanceByStudent(UUID studentId) {
        User student = userService.getUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        return attendanceRepository.findByStudent(student);
    }
    
    /**
     * Get attendance record for a specific student in a specific session
     */
    public Optional<AttendanceRecord> getAttendanceBySessionAndStudent(
            UUID sessionId, 
            UUID studentId) {
        AttendanceSession session = sessionService.getSessionById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        User student = userService.getUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        return attendanceRepository.findBySessionAndStudent(session, student);
    }
    
    /**
     * Get attendance records for a student in a specific module
     */
    public List<AttendanceRecord> getStudentAttendanceInModule(
            UUID studentId, 
            UUID moduleId) {
        User student = userService.getUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        return attendanceRepository.findByStudentAndModule(student, moduleId);
    }
    
    /**
     * Calculate attendance percentage for a student
     */
    public Double calculateAttendancePercentage(UUID studentId) {
        User student = userService.getUserById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        
        Long totalSessions = (long) attendanceRepository.findByStudent(student).size();
        Long presentCount = attendanceRepository.countPresentByStudent(student);
        
        if (totalSessions == 0) {
            return 0.0;
        }
        
        return (presentCount.doubleValue() / totalSessions.doubleValue()) * 100.0;
    }
    
    /**
     * Update attendance status (for manual corrections)
     */
    public AttendanceRecord updateAttendanceStatus(
            UUID recordId, 
            AttendanceStatus newStatus) {
        AttendanceRecord record = attendanceRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        
        record.setStatus(newStatus);
        return attendanceRepository.save(record);
    }
}