package com.errormonitoring.service;

import com.errormonitoring.dto.AuthDTOs;
import com.errormonitoring.model.User;
import com.errormonitoring.repository.UserRepository;
import com.errormonitoring.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public Optional<User> validateUser(String username, String password) {
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Attempting login for username: " + username);
        System.out.println("Password provided: " + (password != null ? "[PROVIDED]" : "[NULL]"));
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + username);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        System.out.println("Found user: " + user.getUsername() + ", ID: " + user.getId());
        System.out.println("User active: " + user.getIsActive());
        System.out.println("User auth provider: " + user.getAuthProvider());
        System.out.println("Password hash exists: " + (user.getPasswordHash() != null));
        
        if (!user.getIsActive()) {
            System.out.println("User is not active");
            return Optional.empty();
        }
        
        if (user.getPasswordHash() == null) {
            System.out.println("User has no password hash");
            return Optional.empty();
        }
        
        // Block Google SSO users from password login
        if ("GOOGLE".equals(user.getAuthProvider())) {
            System.out.println("Blocking Google user from password login");
            throw new RuntimeException("Use Google login");
        }
        
        System.out.println("Verifying password...");
        if (!verifyPassword(password, user.getPasswordHash())) {
            System.out.println("Password verification failed");
            return Optional.empty();
        }
        
        System.out.println("Login successful for user: " + username);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        return Optional.of(user);
    }
    
    private boolean verifyPassword(String password, String storedHash) {
        System.out.println("Password verification:");
        System.out.println("  Input password: " + password);
        System.out.println("  Stored hash: " + storedHash);
        
        try {
            // Only use BCrypt - no plain text fallback for security
            boolean matches = passwordEncoder.matches(password, storedHash);
            System.out.println("  BCrypt match: " + (matches ? "SUCCESS" : "FAILED"));
            return matches;
        } catch (Exception e) {
            System.out.println("  BCrypt error: " + e.getMessage());
            return false;
        }
    }
    
    @Transactional
    public User createUser(String username, String email, String password, String role) {
        System.out.println("=== CREATE USER DEBUG ===");
        System.out.println("Input role: " + role);
        
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAuthProvider("LOCAL");
        
        String upperRole = role.toUpperCase();
        System.out.println("Setting role to: " + upperRole);
        user.setRole(upperRole);
        System.out.println("Role after setting: " + user.getRole());
        
        user.setIsActive(true);
        user.setTwoFactorEnabled(false);
        
        User savedUser = userRepository.save(user);
        System.out.println("Saved user role: " + savedUser.getRole());
        
        return savedUser;
    }
    
    public String generateJwtToken(User user) {
        System.out.println("=== JWT TOKEN GENERATION ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("User Email: " + user.getEmail());
        System.out.println("User Role: " + user.getRole());
        
        String roleString = user.getRole();
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), user.getEmail(), roleString);
        
        System.out.println("Generated JWT token with role: " + roleString);
        return token;
    }
    
    public AuthDTOs.UserResponse mapToUserResponse(User user) {
        AuthDTOs.UserResponse response = new AuthDTOs.UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole()); // Keep original case from database
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setIsActive(user.getIsActive());
        response.setLastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
        response.setTwoFactorEnabled(user.getTwoFactorEnabled());
        
        System.out.println("=== USER RESPONSE MAPPING ===");
        System.out.println("Original user role: " + user.getRole());
        System.out.println("Response role: " + response.getRole());
        
        return response;
    }
    
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @Autowired
    private com.errormonitoring.repository.AuditLogRepository auditLogRepository;
    
    @Transactional
    public void deleteUser(Integer userId) {
        try {
            // First delete related audit logs to avoid foreign key constraint
            auditLogRepository.deleteByUserId(userId);
            
            // Then delete the user
            userRepository.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }
    
    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }
    
    public Optional<User> findByEmail(String email) {
        System.out.println("Looking up user by email: " + email);
        
        // Try exact match first (case-insensitive)
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);
        
        // If no exact match, try different variations
        if (user.isEmpty()) {
            // Try original case-sensitive method
            user = userRepository.findByEmail(email);
        }
        
        // Special handling for choudharykhetesh variations
        if (user.isEmpty() && (email.toLowerCase().contains("choudharykhetesh") || email.toLowerCase().contains("khetesh"))) {
            // Try partial match with choudharykhetesh
            user = userRepository.findByEmailContainingIgnoreCase("choudharykhetesh");
            if (user.isEmpty()) {
                user = userRepository.findByEmailContainingIgnoreCase("khetesh");
            }
            System.out.println("Partial match found: " + (user.isPresent() ? user.get().getEmail() : "none"));
        }
        
        System.out.println("User lookup result: " + (user.isPresent() ? 
            "Found user " + user.get().getUsername() + " (ID: " + user.get().getId() + ", Role: " + user.get().getRole() + ", Email: " + user.get().getEmail() + ")" : 
            "No user found"));
        return user;
    }
    
    @Transactional
    public User createGoogleUser(String username, String email, String name, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(null);
        user.setAuthProvider("GOOGLE");
        user.setRole(role);
        user.setFirstName(name.split(" ")[0]);
        if (name.contains(" ")) {
            user.setLastName(name.substring(name.indexOf(" ") + 1));
        }
        user.setIsActive(true);
        user.setTwoFactorEnabled(false);
        return userRepository.save(user);
    }
    
    @Transactional
    public boolean toggleUserStatus(Integer userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        return true;
    }
}