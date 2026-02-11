package com.university.attendance.controller;

import com.university.attendance.dto.CreateModuleRequest;
import com.university.attendance.dto.ModuleDTO;
import com.university.attendance.entity.Module;
import com.university.attendance.entity.User;
import com.university.attendance.service.ModuleService;
import com.university.attendance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ModuleController {
    
    private final ModuleService moduleService;
    private final UserService userService;
    
    /**
     * Create a new module
     * POST /api/v1/modules
     */
    @PostMapping
    public ResponseEntity<ModuleDTO> createModule(@Valid @RequestBody CreateModuleRequest request) {
        User teacher = userService.getUserById(request.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        Module module = new Module();
        module.setModuleCode(request.getModuleCode());
        module.setModuleName(request.getModuleName());
        module.setDescription(request.getDescription());
        module.setTeacher(teacher);
        module.setActive(true);
        
        Module created = moduleService.createModule(module);
        return new ResponseEntity<>(ModuleDTO.fromEntity(created), HttpStatus.CREATED);
    }
    
    /**
     * Get module by ID
     * GET /api/v1/modules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable UUID id) {
        Module module = moduleService.getModuleById(id)
            .orElseThrow(() -> new RuntimeException("Module not found with ID: " + id));
        return ResponseEntity.ok(ModuleDTO.fromEntity(module));
    }
    
    /**
     * Get module by code
     * GET /api/v1/modules/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ModuleDTO> getModuleByCode(@PathVariable String code) {
        Module module = moduleService.getModuleByCode(code)
            .orElseThrow(() -> new RuntimeException("Module not found with code: " + code));
        return ResponseEntity.ok(ModuleDTO.fromEntity(module));
    }
    
    /**
     * Get all active modules
     * GET /api/v1/modules
     */
    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllActiveModules() {
        List<ModuleDTO> modules = moduleService.getAllActiveModules()
            .stream()
            .map(ModuleDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(modules);
    }
    
    /**
     * Get modules by teacher
     * GET /api/v1/modules/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByTeacher(@PathVariable UUID teacherId) {
        List<ModuleDTO> modules = moduleService.getActiveModulesByTeacher(teacherId)
            .stream()
            .map(ModuleDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(modules);
    }
    
    /**
     * Deactivate module
     * DELETE /api/v1/modules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateModule(@PathVariable UUID id) {
        moduleService.deactivateModule(id);
        return ResponseEntity.noContent().build();
    }
}