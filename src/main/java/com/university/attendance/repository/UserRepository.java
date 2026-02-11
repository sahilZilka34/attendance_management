package com.university.attendance.repository;

import com.university.attendance.entity.User;
import com.university.attendance.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Spring Data JPA automatically implements these methods
    // based on method names!
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByMicrosoftId(String microsoftId);
    
    List<User> findByRole(UserRole role);
    
    List<User> findByActiveTrue();
    
    boolean existsByEmail(String email);
}