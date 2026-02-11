package com.errormonitoring.controller;

import com.errormonitoring.dto.AlertDTOs;
import com.errormonitoring.model.Alert;
import com.errormonitoring.service.AlertService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
public class AlertsController {
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private com.errormonitoring.security.JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private com.errormonitoring.service.EmailNotificationService emailNotificationService;
    
    @Autowired
    private com.errormonitoring.service.AuditLogService auditLogService;
    
    @GetMapping("/stats")
    public ResponseEntity<?> getAlertStats(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            String userRole = getUserRoleFromRequest(request);
            
            List<AlertDTOs.AlertResponse> alerts = alertService.getAlerts(userId, userRole);
            
            long totalAlerts = alerts.size();
            long unresolvedAlerts = alerts.stream().filter(alert -> !alert.getIsResolved()).count();
            long resolvedAlerts = alerts.stream().filter(alert -> alert.getIsResolved()).count();
            long criticalAlerts = alerts.stream().filter(alert -> "CRITICAL".equals(alert.getAlertLevel())).count();
            
            return ResponseEntity.ok(Map.of(
                "totalAlerts", totalAlerts,
                "unresolvedAlerts", unresolvedAlerts,
                "resolvedAlerts", resolvedAlerts,
                "criticalAlerts", criticalAlerts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch alert stats"));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAlerts(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            String userRole = getUserRoleFromRequest(request);
            
            List<AlertDTOs.AlertResponse> alerts = alertService.getAlerts(userId, userRole);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch alerts"));
        }
    }
    
    @GetMapping("/unresolved")
    public ResponseEntity<?> getUnresolvedAlerts(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            String userRole = getUserRoleFromRequest(request);
            
            List<AlertDTOs.AlertResponse> alerts = alertService.getUnresolvedAlerts(userId, userRole);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch unresolved alerts"));
        }
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateAlert(@RequestBody Map<String, Object> alertData) {
        try {
            String errorMessage = (String) alertData.get("errorMessage");
            String severity = (String) alertData.get("severity");
            String application = (String) alertData.get("applicationName");
            
            System.out.println("🚨 ALERT GENERATED:");
            System.out.println("Application: " + application);
            System.out.println("Severity: " + severity);
            System.out.println("Error: " + errorMessage);
            
            // Send email
            try {
                String recipients = alertService.getErrorRecipients(null); // For general alerts
                String subject = "🚨 " + severity + " Alert - " + application;
                String body = "Alert Details:\n\n" +
                    "Application: " + application + "\n" +
                    "Severity: " + severity + "\n" +
                    "Error: " + errorMessage + "\n" +
                    "Time: " + java.time.LocalDateTime.now() + "\n\n" +
                    "Please investigate this issue.";
                    
                if (recipients != null && !recipients.isEmpty()) {
                    String[] recipientEmails = recipients.split(",\\s*");
                    for (String email : recipientEmails) {
                        emailNotificationService.sendEmail(email.trim(), subject, body);
                    }
                }
                System.out.println("📧 EMAIL SENT: Alert notification sent to " + recipients);
            } catch (Exception e) {
                System.err.println("Failed to send email: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Alert generated and email sent successfully",
                "alertId", System.currentTimeMillis(),
                "emailSent", true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to generate alert"));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertDTOs.CreateAlertRequest request, HttpServletRequest httpRequest) {
        try {
            Integer userId = getUserIdFromRequest(httpRequest);
            String userRole = getUserRoleFromRequest(httpRequest);
            
            // Check if user can create alert for this application
            if (!"ADMIN".equals(userRole)) {
                // Non-admin users can only create alerts for their own applications
                var appOpt = alertService.getApplicationById(request.getApplicationId());
                if (appOpt.isEmpty() || !appOpt.get().getCreatedBy().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only create alerts for your own applications"));
                }
            }
            
            Alert alert = alertService.createAlert(request, userId);
            
            // Send email notification
            try {
                String recipients = alertService.getAlertRecipientsPublic(request.getApplicationId(), userId);
                String subject = "New Alert Created - " + request.getAlertLevel() + " Priority";
                String body = "Alert Details:\n\n" +
                    "Error Log ID: " + request.getErrorLogId() + "\n" +
                    "Alert Type: " + request.getAlertType() + "\n" +
                    "Alert Level: " + request.getAlertLevel() + "\n" +
                    "Message: " + request.getAlertMessage() + "\n\n" +
                    "Please take appropriate action.";
                    
                if (recipients != null && !recipients.isEmpty()) {
                    String[] recipientEmails = recipients.split(",\\s*");
                    for (String email : recipientEmails) {
                        emailNotificationService.sendEmail(email.trim(), subject, body);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to send email notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Alert rule created successfully!",
                "alert", alert
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to create alert: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlert(@PathVariable Integer id, @Valid @RequestBody AlertDTOs.UpdateAlertRequest request) {
        try {
            Alert alert = alertService.updateAlert(id, request);
            return ResponseEntity.ok(Map.of(
                "message", "Alert updated successfully",
                "alert", alert
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to update alert"));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            
            boolean deleted = alertService.deleteAlert(id);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Alert not found"));
            }
            
            // Audit log for alert deletion
            auditLogService.logActivity(userId, "ALERT_DELETED", "Alert", id.toString(), 
                null, "Alert deleted", 
                getClientIP(request), request.getHeader("User-Agent"));
                
            return ResponseEntity.ok(Map.of("message", "Alert deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to delete alert: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable Integer id, HttpServletRequest httpRequest) {
        try {
            Integer userId = getUserIdFromRequest(httpRequest);
            String userRole = getUserRoleFromRequest(httpRequest);
            
            Optional<Alert> alertOpt = alertService.getAlertById(id);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Alert not found"));
            }
            
            Alert alert = alertOpt.get();
            
            // Check permissions - only admin or application owner can resolve
            if (!"ADMIN".equals(userRole)) {
                var appOpt = alertService.getApplicationById(alert.getApplicationId());
                if (appOpt.isEmpty() || !appOpt.get().getCreatedBy().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You can only resolve alerts for your own applications"));
                }
            }
            
            boolean resolved = alertService.resolveAlert(id);
            if (!resolved) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Alert not found"));
            }
            
            // Send email notification for resolved alert
            try {
                String subject = "✅ Alert Resolved - " + alert.getAlertLevel() + " Priority";
                String body = "Alert has been resolved:\n\n" +
                    "Error Log ID: " + alert.getErrorLogId() + "\n" +
                    "Alert Type: " + alert.getAlertType() + "\n" +
                    "Alert Level: " + alert.getAlertLevel() + "\n" +
                    "Message: " + alert.getAlertMessage() + "\n" +
                    "Resolved At: " + java.time.LocalDateTime.now() + "\n" +
                    "Resolved By: " + ("ADMIN".equals(userRole) ? "Administrator" : "Developer") + "\n\n" +
                    "The issue has been successfully resolved.";
                    
                emailNotificationService.sendEmail(alert.getRecipients(), subject, body);
                System.out.println("Resolution email sent for alert ID: " + id);
            } catch (Exception e) {
                System.err.println("Failed to send resolution email notification: " + e.getMessage());
                e.printStackTrace();
            }
            
            return ResponseEntity.ok(Map.of("message", "Alert resolved successfully and email notification sent!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to resolve alert"));
        }
    }
    
    @GetMapping("/applications-with-ids")
    public ResponseEntity<?> getApplicationsWithIds(HttpServletRequest request) {
        try {
            Integer userId = getUserIdFromRequest(request);
            String userRole = getUserRoleFromRequest(request);
            
            List<com.errormonitoring.model.Application> apps = alertService.getAllApplicationsRaw();
            
            // Filter based on role
            if ("DEVELOPER".equals(userRole)) {
                apps = apps.stream()
                    .filter(app -> app.getCreatedBy().equals(userId))
                    .collect(Collectors.toList());
            }
            // ADMIN and VIEWER see all applications
            
            List<Map<String, Object>> result = apps.stream()
                .map(app -> {
                    Map<String, Object> appMap = new HashMap<>();
                    appMap.put("id", app.getId());
                    appMap.put("name", app.getName());
                    appMap.put("createdBy", app.getCreatedBy());
                    appMap.put("isActive", app.getIsActive() != null ? app.getIsActive() : true);
                    return appMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract user ID from token: " + e.getMessage());
        }
        return null; // Return null instead of default admin ID
    }
    
    private String getUserRoleFromRequest(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                return jwtTokenProvider.getRoleFromToken(token);
            }
        } catch (Exception e) {
            System.err.println("Failed to extract user role from token: " + e.getMessage());
        }
        return "VIEWER"; // Default to most restrictive role
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