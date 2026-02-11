package com.university.attendance.service;

import com.university.attendance.entity.Module;
import com.university.attendance.entity.User;
import com.university.attendance.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ModuleService {
    
    private final ModuleRepository moduleRepository;
    private final UserService userService;
    
    /**
     * Create a new module
     * Business Rule: Module code must be unique, teacher must exist
     */
    public Module createModule(Module module) {
        // Validate module code is unique
        if (moduleRepository.existsByModuleCode(module.getModuleCode())) {
            throw new RuntimeException("Module with code " + module.getModuleCode() + " already exists");
        }
        
        // Validate teacher exists and is active
        User teacher = module.getTeacher();
        if (teacher == null || !userService.getUserById(teacher.getId()).isPresent()) {
            throw new RuntimeException("Invalid teacher");
        }
        
        return moduleRepository.save(module);
    }
    
    /**
     * Find module by ID
     */
    public Optional<Module> getModuleById(UUID id) {
        return moduleRepository.findById(id);
    }
    
    /**
     * Find module by code (e.g., "CS101")
     */
    public Optional<Module> getModuleByCode(String moduleCode) {
        return moduleRepository.findByModuleCode(moduleCode);
    }
    
    /**
     * Get all modules taught by a specific teacher
     */
    public List<Module> getModulesByTeacher(User teacher) {
        return moduleRepository.findByTeacher(teacher);
    }
    
    /**
     * Get all active modules
     */
    public List<Module> getAllActiveModules() {
        return moduleRepository.findByActiveTrue();
    }
    
    /**
     * Get active modules for a specific teacher
     */
    public List<Module> getActiveModulesByTeacher(UUID teacherId) {
        User teacher = userService.getUserById(teacherId)
            .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return moduleRepository.findByTeacherAndActiveTrue(teacher);
    }
    
    /**
     * Update module
     */
    public Module updateModule(Module module) {
        if (!moduleRepository.existsById(module.getId())) {
            throw new RuntimeException("Module not found with ID: " + module.getId());
        }
        return moduleRepository.save(module);
    }
    
    /**
     * Deactivate module (soft delete)
     */
    public void deactivateModule(UUID moduleId) {
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new RuntimeException("Module not found"));
        module.setActive(false);
        moduleRepository.save(module);
    }
}