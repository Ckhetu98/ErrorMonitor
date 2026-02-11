package com.errormonitoring.controller;

import com.errormonitoring.security.JwtTokenProvider;
import com.errormonitoring.service.ApplicationService;
import com.errormonitoring.service.ErrorLogService;
import com.errormonitoring.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private ErrorLogService errorLogService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider jwtUtils;
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Integer userId = null;
            String userRole = "VIEWER";
            
            if (token != null) {
                try {
                    userId = getUserIdFromToken(token);
                    userRole = getRoleFromToken(token);
                } catch (Exception e) {
                    System.out.println("Token extraction failed in dashboard stats: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired token"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authorization token required"));
            }
            
            Map<String, Object> stats = new HashMap<>();
            
            // Get applications count based on role
            long totalApplications = 0;
            long activeApplications = 0;
            
            try {
                List<com.errormonitoring.model.Application> applications;
                
                if ("ADMIN".equals(userRole)) {
                    applications = applicationService.getAllApplications();
                } else if ("DEVELOPER".equals(userRole)) {
                    applications = applicationService.getApplicationsByUserId(userId);
                } else {
                    // VIEWER sees ALL applications but read-only
                    applications = applicationService.getAllApplications();
                }
                
                totalApplications = applications.size();
                activeApplications = applications.stream()
                    .filter(app -> Boolean.TRUE.equals(app.getIsActive()))
                    .count();
                
                System.out.println("=== DASHBOARD STATS DEBUG ===");
                System.out.println("User: " + userId + ", Role: " + userRole);
                System.out.println("Total applications in DB: " + totalApplications);
                System.out.println("Active applications: " + activeApplications);
                System.out.println("Applications list:");
                applications.forEach(app -> 
                    System.out.println("  - ID: " + app.getId() + ", Name: '" + app.getName() + "', Active: " + app.getIsActive() + ", CreatedBy: " + app.getCreatedBy())
                );
                System.out.println("=== END DEBUG ===");
            } catch (Exception e) {
                System.err.println("Error fetching applications: " + e.getMessage());
                e.printStackTrace();
                totalApplications = 0;
                activeApplications = 0;
            }
            
            // Get real error statistics based on role
            long totalErrors = 0;
            long criticalErrors = 0;
            long resolvedErrors = 0;
            
            try {
                List<Map<String, Object>> userErrors;
                if ("ADMIN".equals(userRole)) {
                    userErrors = errorLogService.getAllErrorLogs();
                } else if ("DEVELOPER".equals(userRole)) {
                    userErrors = errorLogService.getErrorLogsByRole(userId, userRole);
                } else {
                    // VIEWER sees ALL errors but read-only
                    userErrors = errorLogService.getAllErrorLogs();
                }
                
                totalErrors = userErrors.size();
                
                criticalErrors = userErrors.stream()
                    .filter(error -> "Critical".equals(error.get("severity")))
                    .count();
                    
                resolvedErrors = userErrors.stream()
                    .filter(error -> "Resolved".equals(error.get("status")))
                    .count();
                    
                System.out.println("=== ERROR STATS DEBUG ===");
                System.out.println("User: " + userId + ", Role: " + userRole);
                System.out.println("Total errors: " + totalErrors);
                System.out.println("Critical errors: " + criticalErrors);
                System.out.println("Resolved errors: " + resolvedErrors);
                System.out.println("=== END ERROR DEBUG ===");
            } catch (Exception e) {
                System.err.println("Error fetching error statistics: " + e.getMessage());
                e.printStackTrace();
                totalErrors = 0;
                criticalErrors = 0;
                resolvedErrors = 0;
            }
            
            stats.put("totalErrors", totalErrors);
            stats.put("criticalErrors", criticalErrors);
            stats.put("resolvedErrors", resolvedErrors);
            stats.put("activeApplications", activeApplications);
            stats.put("avgResponseTime", 85);
            stats.put("errorRate", totalErrors > 0 ? Math.round((double)(totalErrors - resolvedErrors) / totalErrors * 100 * 100.0) / 100.0 : 0.0);
            stats.put("activeUsers", 1);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "totalErrors", 0,
                "criticalErrors", 0,
                "resolvedErrors", 0,
                "activeApplications", 0,
                "avgResponseTime", 85,
                "errorRate", 0.0
            ));
        }
    }
    
    @GetMapping("/recent-errors")
    public ResponseEntity<?> getRecentErrors(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Integer userId = null;
            String userRole = "VIEWER";
            
            if (token != null) {
                try {
                    userId = getUserIdFromToken(token);
                    userRole = getRoleFromToken(token);
                } catch (Exception e) {
                    // Token invalid, use defaults
                }
            }
            
            List<Map<String, Object>> recentErrors;
            try {
                // Get role-based recent errors
                recentErrors = errorLogService.getRecentErrors(userId, userRole, 10)
                    .stream()
                    .map(error -> {
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("id", error.get("id"));
                        errorMap.put("error", error.get("errorMessage"));
                        errorMap.put("severity", error.get("severity"));
                        errorMap.put("application", error.get("application"));
                        errorMap.put("timestamp", error.get("createdAt"));
                        errorMap.put("status", error.get("status"));
                        return errorMap;
                    })
                    .toList();
                    
                System.out.println("Recent errors for " + userRole + " (" + userId + "): " + recentErrors.size() + " errors");
            } catch (Exception e) {
                System.err.println("Error fetching recent errors: " + e.getMessage());
                e.printStackTrace();
                recentErrors = List.of();
            }
            
            return ResponseEntity.ok(recentErrors);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/active-alerts")
    public ResponseEntity<?> getActiveAlerts() {
        try {
            // Return empty list for now
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/application-health")
    public ResponseEntity<?> getApplicationHealth(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            Integer userId = null;
            String userRole = "VIEWER";
            
            if (token != null) {
                try {
                    userId = getUserIdFromToken(token);
                    userRole = getRoleFromToken(token);
                } catch (Exception e) {
                    System.out.println("Token extraction failed in dashboard: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired token"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authorization token required"));
            }
            
            List<Map<String, Object>> healthData;
            try {
                List<com.errormonitoring.model.Application> applications;
                
                if ("ADMIN".equals(userRole)) {
                    applications = applicationService.getAllApplications();
                    System.out.println("Dashboard (ADMIN) - Found " + applications.size() + " applications");
                } else if ("DEVELOPER".equals(userRole)) {
                    applications = applicationService.getApplicationsByUserId(userId);
                    System.out.println("Dashboard (DEVELOPER) - Found " + applications.size() + " applications for user " + userId);
                } else {
                    // VIEWER sees ALL applications but read-only
                    applications = applicationService.getAllApplications();
                    System.out.println("Dashboard (VIEWER) - Found " + applications.size() + " applications (read-only)");
                }
                
                healthData = applications.stream()
                    .map(app -> {
                        Map<String, Object> health = new HashMap<>();
                        health.put("appName", app.getName());
                        health.put("healthScore", app.getIsActive() ? 95 : 50);
                        health.put("status", app.getIsActive() ? "Healthy" : "Inactive");
                        return health;
                    })
                    .toList();
            } catch (Exception e) {
                System.err.println("Error fetching applications for health: " + e.getMessage());
                e.printStackTrace();
                healthData = List.of();
            }
            
            return ResponseEntity.ok(healthData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
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
    
    private Integer getUserIdFromToken(String token) {
        try {
            return jwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getRoleFromToken(String token) {
        try {
            return jwtUtils.getRoleFromToken(token);
        } catch (Exception e) {
            return "VIEWER";
        }
    }
}