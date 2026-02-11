package com.university.attendance.dto;

import com.university.attendance.entity.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private UUID id;
    private ModuleDTO module;
    private UserDTO teacher;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroom;
    private Integer qrValidityMinutes;
    private SessionStatus status;
    private LocalDateTime createdAt;
    
    // NEW FIELDS
    private Boolean locationRequired;
    private Double campusLatitude;
    private Double campusLongitude;
    private Integer campusRadiusMeters;
    private Boolean mandatoryAttendance;
    
    public static SessionDTO fromEntity(com.university.attendance.entity.AttendanceSession session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setModule(ModuleDTO.fromEntity(session.getModule()));
        dto.setTeacher(UserDTO.fromEntity(session.getTeacher()));
        dto.setSessionDate(session.getSessionDate());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setClassroom(session.getClassroom());
        dto.setQrValidityMinutes(session.getQrValidityMinutes());
        dto.setStatus(session.getStatus());
        dto.setCreatedAt(session.getCreatedAt());
        
        // NEW FIELDS
        dto.setLocationRequired(session.getLocationRequired());
        dto.setCampusLatitude(session.getCampusLatitude());
        dto.setCampusLongitude(session.getCampusLongitude());
        dto.setCampusRadiusMeters(session.getCampusRadiusMeters());
        dto.setMandatoryAttendance(session.getMandatoryAttendance());
        
        return dto;
    }
}