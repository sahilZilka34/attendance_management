package com.university.attendance.repository;

import com.university.attendance.entity.Module;
import com.university.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {
    
    Optional<Module> findByModuleCode(String moduleCode);
    
    List<Module> findByTeacher(User teacher);
    
    List<Module> findByActiveTrue();
    
    // FIXED: Remove the Boolean parameter
    List<Module> findByTeacherAndActiveTrue(User teacher);
    
    boolean existsByModuleCode(String moduleCode);
}