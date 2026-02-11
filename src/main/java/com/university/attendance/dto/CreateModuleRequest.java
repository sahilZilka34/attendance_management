package com.university.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateModuleRequest {
    
    @NotBlank(message = "Module code is required")
    private String moduleCode;
    
    @NotBlank(message = "Module name is required")
    private String moduleName;
    
    private String description;
    
    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;
}