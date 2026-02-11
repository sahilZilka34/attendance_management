package com.university.attendance.service;

import com.university.attendance.entity.AttendanceSession;
import com.university.attendance.entity.Module;
import com.university.attendance.entity.SessionStatus;
import com.university.attendance.entity.User;
import com.university.attendance.repository.AttendanceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceSessionService {
    
    private final AttendanceSessionRepository sessionRepository;
    private final ModuleService moduleService;
    private final UserService userService;
    
    /**
     * Create a new attendance session
     * Business Rule: Teacher must be assigned to the module
     */
    public AttendanceSession createSession(AttendanceSession session) {
        // Validate module exists
        Module module = moduleService.getModuleById(session.getModule().getId())
            .orElseThrow(() -> new RuntimeException("Module not found"));
        
        // Validate teacher exists
        User teacher = userService.getUserById(session.getTeacher().getId())
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        // Business Rule: Teacher must be assigned to this module
        if (!module.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Teacher is not assigned to this module");
        }
        
        // Set initial status
        session.setStatus(SessionStatus.SCHEDULED);
        
        return sessionRepository.save(session);
    }
    
    /**
     * Get session by ID
     */
    public Optional<AttendanceSession> getSessionById(UUID id) {
        return sessionRepository.findById(id);
    }
    
    /**
     * Get all sessions for a module
     */
    public List<AttendanceSession> getSessionsByModule(UUID moduleId) {
        Module module = moduleService.getModuleById(moduleId)
            .orElseThrow(() -> new RuntimeException("Module not found"));
        return sessionRepository.findByModule(module);
    }
    
    /**
     * Get all sessions for a teacher
     */
    public List<AttendanceSession> getSessionsByTeacher(UUID teacherId) {
        User teacher = userService.getUserById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return sessionRepository.findByTeacher(teacher);
    }
    
    /**
     * Get sessions by date
     */
    public List<AttendanceSession> getSessionsByDate(LocalDate date) {
        return sessionRepository.findBySessionDate(date);
    }
    
    /**
     * Get today's sessions for a teacher
     */
    public List<AttendanceSession> getTodaySessionsForTeacher(UUID teacherId) {
        User teacher = userService.getUserById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return sessionRepository.findByTeacherAndSessionDate(teacher, LocalDate.now());
    }
    
    /**
     * Get sessions by date range for a module
     */
    public List<AttendanceSession> getSessionsByModuleAndDateRange(
            UUID moduleId, 
            LocalDate startDate, 
            LocalDate endDate) {
        Module module = moduleService.getModuleById(moduleId)
            .orElseThrow(() -> new RuntimeException("Module not found"));
        return sessionRepository.findByModuleAndSessionDateBetween(module, startDate, endDate);
    }
    
    /**
     * Start a session (activate QR code)
     * Business Rule: Only scheduled sessions can be started
     */
    public AttendanceSession startSession(UUID sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled sessions can be started");
        }
        
        session.setStatus(SessionStatus.ACTIVE);
        return sessionRepository.save(session);
    }
    
    /**
     * Complete a session
     * Business Rule: Only active sessions can be completed
     */
    public AttendanceSession completeSession(UUID sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new RuntimeException("Only active sessions can be completed");
        }
        
        session.setStatus(SessionStatus.COMPLETED);
        return sessionRepository.save(session);
    }
    
    /**
     * Cancel a session
     */
    public AttendanceSession cancelSession(UUID sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed session");
        }
        
        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);
    }
}