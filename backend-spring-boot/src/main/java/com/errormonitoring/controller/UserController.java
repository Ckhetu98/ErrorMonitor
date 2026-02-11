package com.errormonitoring.controller;

import com.errormonitoring.dto.AuthDTOs;
import com.errormonitoring.model.User;
import com.errormonitoring.service.AuthService;
import com.errormonitoring.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private com.errormonitoring.security.JwtTokenProvider jwtUtils;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = authService.getAllUsers();
            List<AuthDTOs.UserResponse> userResponses = users.stream()
                .map(authService::mapToUserResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch users"));
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody AuthDTOs.RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            String role = request.getRole();
            if (role == null || !Set.of("ADMIN", "DEVELOPER", "VIEWER").contains(role.toUpperCase())) {
                role = "VIEWER";
            }
            
            User user = authService.createUser(
                request.getUsername(),
                request.getEmail(), 
                request.getPassword(),
                role
            );
            
            try {
                Integer adminUserId = getUserIdFromRequest(httpRequest);
                auditLogService.logActivity(adminUserId, "CREATE_USER", "User", user.getId().toString(),
                    null, user.getUsername(), getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "User created successfully",
                "user", authService.mapToUserResponse(user)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to create user. Username or email may already exist."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to create user"));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            var userOpt = authService.getUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }
            
            User user = userOpt.get();
            String oldValues = user.getUsername() + "," + user.getEmail() + "," + user.getRole();
            
            // Update user fields
            if (request.containsKey("username")) {
                user.setUsername((String) request.get("username"));
            }
            if (request.containsKey("email")) {
                user.setEmail((String) request.get("email"));
            }
            if (request.containsKey("role")) {
                user.setRole((String) request.get("role"));
            }
            if (request.containsKey("firstName")) {
                user.setFirstName((String) request.get("firstName"));
            }
            if (request.containsKey("lastName")) {
                user.setLastName((String) request.get("lastName"));
            }
            if (request.containsKey("twoFactorEnabled")) {
                user.setTwoFactorEnabled((Boolean) request.get("twoFactorEnabled"));
            }
            
            User updatedUser = authService.updateUser(user);
            String newValues = updatedUser.getUsername() + "," + updatedUser.getEmail() + "," + updatedUser.getRole();
            
            try {
                Integer adminUserId = getUserIdFromRequest(httpRequest);
                auditLogService.logActivity(adminUserId, "UPDATE_USER", "User", id.toString(),
                    oldValues, newValues, getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "User updated successfully",
                "user", authService.mapToUserResponse(updatedUser)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to update user"));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id, HttpServletRequest httpRequest) {
        try {
            var userOpt = authService.getUserById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }
            
            String deletedUsername = userOpt.get().getUsername();
            authService.deleteUser(id);
            
            try {
                Integer adminUserId = getUserIdFromRequest(httpRequest);
                auditLogService.logActivity(adminUserId, "DELETE_USER", "User", id.toString(),
                    deletedUsername, null, getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to delete user"));
        }
    }
    
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Integer id, HttpServletRequest httpRequest) {
        try {
            boolean success = authService.toggleUserStatus(id);
            if (!success) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }
            
            try {
                Integer adminUserId = getUserIdFromRequest(httpRequest);
                auditLogService.logActivity(adminUserId, "TOGGLE_USER_STATUS", "User", id.toString(),
                    null, null, getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("message", "User status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to update user status"));
        }
    }
    
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                return jwtUtils.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract user ID from token: " + e.getMessage());
        }
        return 1; // Default admin user ID
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) return null;
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}