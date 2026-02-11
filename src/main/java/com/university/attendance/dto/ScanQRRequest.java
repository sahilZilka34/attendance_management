package com.university.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ScanQRRequest {
    
    @NotBlank(message = "QR token is required")
    private String qrToken;
    
    @NotNull(message = "Student ID is required")
    private UUID studentId;
    
    private String deviceInfo;
    private Double latitude;
    private Double longitude;
}