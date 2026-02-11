package com.university.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_sessions")
@Data
public class AttendanceSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Column(nullable = false)
    private LocalDate sessionDate;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private String classroom;
    
    @Column(nullable = false)
    private Integer qrValidityMinutes = 15;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // NEW FIELDS FOR LOCATION-BASED ATTENDANCE
    @Column(nullable = false)
    private Boolean locationRequired = false;
    
    @Column
    private Double campusLatitude;
    
    @Column
    private Double campusLongitude;
    
    @Column
    private Integer campusRadiusMeters = 500; // Default 500 meters
    
    // NEW FIELD FOR MANDATORY ATTENDANCE
    @Column(nullable = false)
    private Boolean mandatoryAttendance = true; // Default: mandatory
}