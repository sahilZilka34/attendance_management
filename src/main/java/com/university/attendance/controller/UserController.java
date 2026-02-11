package com.university.attendance.controller;

import com.university.attendance.dto.CreateUserRequest;
import com.university.attendance.dto.UserDTO;
import com.university.attendance.entity.User;
import com.university.attendance.entity.UserRole;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allow frontend to access
public class UserController {
    
    private final UserService userService;
    
    /**
     * Create a new user
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setMicrosoftId(request.getMicrosoftId());
        user.setActive(true);
        
        User created = userService.createUser(user);
        return new ResponseEntity<>(UserDTO.fromEntity(created), HttpStatus.CREATED);
    }
    
    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }
    
    /**
     * Get user by email
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }
    
    /**
     * Get all students
     * GET /api/v1/users/students
     */
    @GetMapping("/students")
    public ResponseEntity<List<UserDTO>> getAllStudents() {
        List<UserDTO> students = userService.getAllStudents()
            .stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }
    
    /**
     * Get all teachers
     * GET /api/v1/users/teachers
     */
    @GetMapping("/teachers")
    public ResponseEntity<List<UserDTO>> getAllTeachers() {
        List<UserDTO> teachers = userService.getAllTeachers()
            .stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(teachers);
    }
    
    /**
     * Deactivate user
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}