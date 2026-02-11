package com.university.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    
    @NotNull(message = "Module ID is required")
    private UUID moduleId;
    
    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotBlank(message = "Classroom is required")
    private String classroom;
    
    private Integer qrValidityMinutes = 15;
    
    // NEW FIELDS
    private Boolean locationRequired = false;
    
    private Double campusLatitude;
    
    private Double campusLongitude;
    
    private Integer campusRadiusMeters = 500;
    
    private Boolean mandatoryAttendance = true;
}