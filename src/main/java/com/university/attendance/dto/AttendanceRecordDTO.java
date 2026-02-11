package com.university.attendance.dto;

import com.university.attendance.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDTO {
    private UUID id;
    private UUID sessionId;
    private UserDTO student;
    private LocalDateTime markedAt;
    private AttendanceStatus status;
    private String deviceInfo;
    private Double latitude;
    private Double longitude;
    
    public static AttendanceRecordDTO fromEntity(com.university.attendance.entity.AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO();
        dto.setId(record.getId());
        dto.setSessionId(record.getSession().getId());
        dto.setStudent(UserDTO.fromEntity(record.getStudent()));
        dto.setMarkedAt(record.getMarkedAt());
        dto.setStatus(record.getStatus());
        dto.setDeviceInfo(record.getDeviceInfo());
        dto.setLatitude(record.getLatitude());
        dto.setLongitude(record.getLongitude());
        return dto;
    }
}