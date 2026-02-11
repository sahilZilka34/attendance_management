package com.university.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {
    private UUID id;
    private String moduleCode;
    private String moduleName;
    private String description;
    private UserDTO teacher;
    private Boolean active;
    private LocalDateTime createdAt;
    
    public static ModuleDTO fromEntity(com.university.attendance.entity.Module module) {
        ModuleDTO dto = new ModuleDTO();
        dto.setId(module.getId());
        dto.setModuleCode(module.getModuleCode());
        dto.setModuleName(module.getModuleName());
        dto.setDescription(module.getDescription());
        dto.setTeacher(UserDTO.fromEntity(module.getTeacher()));
        dto.setActive(module.getActive());
        dto.setCreatedAt(module.getCreatedAt());
        return dto;
    }
}