package com.university.attendance.repository;

import com.university.attendance.entity.AttendanceSession;
import com.university.attendance.entity.Module;
import com.university.attendance.entity.SessionStatus;
import com.university.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {
    
    List<AttendanceSession> findByModule(Module module);
    
    List<AttendanceSession> findByTeacher(User teacher);
    
    List<AttendanceSession> findByStatus(SessionStatus status);
    
    List<AttendanceSession> findBySessionDate(LocalDate date);
    
    List<AttendanceSession> findByTeacherAndSessionDate(User teacher, LocalDate date);
    
    List<AttendanceSession> findByModuleAndSessionDateBetween(
        Module module, 
        LocalDate startDate, 
        LocalDate endDate
    );
}