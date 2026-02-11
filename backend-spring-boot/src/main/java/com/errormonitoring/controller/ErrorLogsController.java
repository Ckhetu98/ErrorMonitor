package com.errormonitoring.controller;

import com.errormonitoring.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/errorlogs")
public class ErrorLogsController {
    
    @Autowired
    private com.errormonitoring.security.JwtTokenProvider jwtUtils;
    
    @Autowired
    private com.errormonitoring.service.EmailNotificationService emailService;
    
    @Autowired
    private com.errormonitoring.service.AlertService alertService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private com.errormonitoring.service.ErrorLogService errorLogService;
    
    @GetMapping
    public ResponseEntity<?> getErrorLogs(HttpServletRequest request) {
        try {
            // Get user info from token
            String token = getTokenFromRequest(request);
            Integer userId = null;
            String userRole = null;
            
            if (token != null) {
                try {
                    userId = jwtUtils.getUserIdFromToken(token);
                    userRole = jwtUtils.getRoleFromToken(token);
                } catch (Exception e) {
                    System.err.println("Failed to extract user info: " + e.getMessage());
                    return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired token"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Authorization token required"));
            }
            
            // Return role-based error logs
            List<Map<String, Object>> errorLogs = errorLogService.getErrorLogsByRole(userId, userRole);
            return ResponseEntity.ok(errorLogs);
        } catch (Exception e) {
            System.err.println("Error fetching error logs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Internal server error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveError(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Use ErrorLogService to resolve the error
            boolean resolved = errorLogService.resolveError(id);
            if (resolved) {
                return ResponseEntity.ok(Map.of("message", "Error resolved successfully", "id", id));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Error not found"));
            }
        } catch (Exception e) {
            System.err.println("Error resolving error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("message", "Failed to resolve error"));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getErrorLog(@PathVariable Integer id) {
        Map<String, Object> errorLog = Map.of(
            "id", id,
            "message", "Sample error message",
            "level", "ERROR",
            "timestamp", LocalDateTime.now().toString(),
            "applicationName", "MyApp",
            "stackTrace", "Sample stack trace"
        );
        
        return ResponseEntity.ok(errorLog);
    }
    
    public ResponseEntity<?> getErrorLogs(String severity, String status, int page, int pageSize, 
                                         org.springframework.security.core.Authentication authentication, 
                                         jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(List.of());
    }
    
    private List<Map<String, Object>> getAllErrorLogs() {
        return errorLogService.getAllErrorLogs();
    }
    
    private List<Map<String, Object>> getUserErrorLogs(Integer userId) {
        return errorLogService.getAllErrorLogs(); // For now, return all errors
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteError(@PathVariable Integer id) {
        try {
            boolean deleted = errorLogService.deleteError(id);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Error deleted successfully", "id", id));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Error not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to delete error"));
        }
    }
    
    @PostMapping("/log")
    public ResponseEntity<?> logError(@RequestBody Map<String, Object> errorData, HttpServletRequest request) {
        try {
            System.out.println("📥 Received error log from external system: " + errorData);
            
            // Temporarily disable API key validation for debugging
            System.out.println("✅ API key validation disabled (debug mode)");
            
            // Check if application is paused
            String applicationName = (String) errorData.get("applicationName");
            System.out.println("🔍 Checking if application '" + applicationName + "' is paused...");
            if (applicationName != null) {
                boolean isPaused = errorLogService.isApplicationPaused(applicationName);
                System.out.println("📊 Application '" + applicationName + "' paused status: " + isPaused);
                if (isPaused) {
                    System.out.println("⏸️ Application " + applicationName + " is paused - ignoring error");
                    return ResponseEntity.ok(Map.of("message", "Application is paused - error ignored", "paused", true));
                }
            }
            
            // Use ErrorLogService to save the error
            Map<String, Object> savedError = errorLogService.logError(errorData);
            
            System.out.println("✅ Error logged successfully with ID: " + savedError.get("id"));
            return ResponseEntity.ok(Map.of("message", "Error logged successfully", "id", savedError.get("id")));
        } catch (Exception e) {
            System.err.println("❌ Failed to log error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to log error: " + e.getMessage()));
        }
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) return null;
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    @PostMapping("/test-error")
    public ResponseEntity<?> testError() {
        try {
            // Simulate an error for testing
            throw new RuntimeException("This is a test error for monitoring system");
        } catch (Exception e) {
            // This will be caught by GlobalExceptionHandler and logged to monitoring system
            throw e;
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}