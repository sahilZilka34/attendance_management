package com.university.attendance.service;

import com.university.attendance.entity.User;
import com.university.attendance.entity.UserRole;
import com.university.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor  // Lombok: auto-creates constructor for final fields
@Transactional  // All methods run in database transactions
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Create a new user
     * Business Rule: Email must be unique
     */
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        return userRepository.save(user);
    }
    
    /**
     * Find user by ID
     */
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }
    
    /**
     * Find user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find user by Microsoft ID (for SSO)
     */
    public Optional<User> getUserByMicrosoftId(String microsoftId) {
        return userRepository.findByMicrosoftId(microsoftId);
    }
    
    /**
     * Get all users by role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Get all active students
     */
    public List<User> getAllStudents() {
        return userRepository.findByRole(UserRole.STUDENT);
    }
    
    /**
     * Get all active teachers
     */
    public List<User> getAllTeachers() {
        return userRepository.findByRole(UserRole.TEACHER);
    }
    
    /**
     * Update user
     */
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User not found with ID: " + user.getId());
        }
        return userRepository.save(user);
    }
    
    /**
     * Deactivate user (soft delete)
     * Business Rule: We don't delete users, just mark as inactive
     */
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setActive(false);
        userRepository.save(user);
    }
    
    /**
     * Check if user exists by email
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}