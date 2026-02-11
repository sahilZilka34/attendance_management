package com.university.attendance.repository;

import com.university.attendance.entity.AttendanceRecord;
import com.university.attendance.entity.AttendanceSession;
import com.university.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    
    List<AttendanceRecord> findBySession(AttendanceSession session);
    
    List<AttendanceRecord> findByStudent(User student);
    
    Optional<AttendanceRecord> findBySessionAndStudent(AttendanceSession session, User student);
    
    boolean existsBySessionAndStudent(AttendanceSession session, User student);
    
    // Custom query to get attendance count for a student
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.student = :student AND ar.status = 'PRESENT'")
    Long countPresentByStudent(@Param("student") User student);
    
    // Get all attendance records for a student in a specific module
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.student = :student AND ar.session.module.id = :moduleId")
    List<AttendanceRecord> findByStudentAndModule(@Param("student") User student, @Param("moduleId") UUID moduleId);
}