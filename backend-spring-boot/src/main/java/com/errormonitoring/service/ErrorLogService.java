package com.errormonitoring.service;

import com.errormonitoring.model.ErrorLog;
import com.errormonitoring.repository.ErrorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ErrorLogService {

    @Autowired
    private ErrorLogRepository errorLogRepository;
    
    @Autowired
    private com.errormonitoring.repository.ApplicationRepository applicationRepository;
    
    @Autowired
    private com.errormonitoring.service.ApplicationService applicationService;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;

    public List<Map<String, Object>> getErrorLogs(String severity, String status, int page, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            return errorLogRepository.findAll(pageable).getContent().stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return errorLogRepository.findAll().stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
        }
    }

    public List<Map<String, Object>> getAllErrorLogs() {
        try {
            return errorLogRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt")).stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching all error logs: " + e.getMessage());
            return List.of();
        }
    }
    
    public List<Map<String, Object>> getErrorLogsByRole(Integer userId, String userRole) {
        try {
            org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
                
            if ("ADMIN".equals(userRole)) {
                // Admin sees ALL errors
                return errorLogRepository.findAll(sort).stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList());
            } else if ("DEVELOPER".equals(userRole)) {
                // Developer sees ONLY errors from their applications
                List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
                List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
                
                System.out.println("Developer " + userId + " app IDs: " + userAppIds);
                
                List<Map<String, Object>> filteredErrors = errorLogRepository.findAll(sort).stream()
                        .filter(error -> {
                            boolean matches = error.getApplicationId() != null && userAppIds.contains(error.getApplicationId());
                            if (matches) {
                                System.out.println("Error " + error.getId() + " matches app " + error.getApplicationId());
                            }
                            return matches;
                        })
                        .map(this::convertToMap)
                        .collect(Collectors.toList());
                        
                System.out.println("Developer " + userId + " filtered errors: " + filteredErrors.size());
                return filteredErrors;
            } else {
                // VIEWER sees read-only errors (same as admin but read-only)
                return errorLogRepository.findAll(sort).stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error fetching error logs by role: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public Map<String, Object> logError(Map<String, Object> errorData) {
        try {
            ErrorLog errorLog = new ErrorLog();
            // Get application ID from error data or use default
            Integer applicationId = null;
            if (errorData.get("applicationId") != null) {
                applicationId = Integer.parseInt(errorData.get("applicationId").toString());
            } else if (errorData.get("applicationName") != null) {
                // Try to find application by name
                String appName = (String) errorData.get("applicationName");
                var app = applicationService.getAllApplications().stream()
                    .filter(a -> a.getName().equals(appName))
                    .findFirst();
                applicationId = app.map(com.errormonitoring.model.Application::getId).orElse(null);
            }
            
            errorLog.setApplicationId(applicationId);
            errorLog.setMessage((String) errorData.get("errorMessage"));
            errorLog.setStackTrace((String) errorData.get("stackTrace"));
            
            // Handle severity conversion with proper logic
            String severityStr = (String) errorData.get("severity");
            ErrorLog.ErrorSeverity severity;
            
            if (severityStr != null) {
                switch (severityStr.toLowerCase()) {
                    case "critical":
                        severity = ErrorLog.ErrorSeverity.Critical;
                        break;
                    case "high":
                        severity = ErrorLog.ErrorSeverity.High;
                        break;
                    case "medium":
                        severity = ErrorLog.ErrorSeverity.Medium;
                        break;
                    case "low":
                        severity = ErrorLog.ErrorSeverity.Low;
                        break;
                    default:
                        severity = ErrorLog.ErrorSeverity.Medium;
                        System.out.println("Unknown severity '" + severityStr + "', defaulting to Medium");
                        break;
                }
            } else {
                severity = ErrorLog.ErrorSeverity.Medium;
                System.out.println("No severity provided, defaulting to Medium");
            }
            
            errorLog.setSeverity(severity);
            errorLog.setStatus("Open");
            errorLog.setApiEndpoint((String) errorData.get("apiEndpoint"));
            errorLog.setErrorType((String) errorData.get("errorType"));
            errorLog.setCreatedAt(LocalDateTime.now());
            
            ErrorLog saved = errorLogRepository.save(errorLog);
            
            // Auto-create alert for any error (all severities) - matching .NET behavior
            try {
                System.out.println("🚨 Creating alert for error ID: " + saved.getId());
                
                Map<String, Object> alertData = Map.of(
                    "errorLogId", saved.getId().toString(),
                    "alertType", "EMAIL",
                    "alertLevel", severity.toString().toUpperCase(),
                    "alertMessage", (String) errorData.get("errorMessage"),
                    "applicationName", errorData.get("applicationName") != null ? 
                        errorData.get("applicationName").toString() : "Unknown Application",
                    "applicationId", applicationId != null ? applicationId.toString() : null
                );
                
                com.errormonitoring.model.Alert createdAlert = alertService.createAlert(alertData, 1);
                System.out.println("✅ Alert created with ID: " + createdAlert.getId());
                
                // Send email notification automatically to appropriate recipients
                String recipients = alertService.getErrorRecipients(applicationId != null ? applicationId : 1);
                if (recipients != null && !recipients.isEmpty()) {
                    String[] recipientEmails = recipients.split(",\\s*");
                    for (String email : recipientEmails) {
                        emailNotificationService.sendErrorNotification(
                            email.trim(),
                            (String) errorData.get("errorMessage"),
                            errorData.get("applicationName") != null ? 
                                errorData.get("applicationName").toString() : "Unknown Application"
                        );
                    }
                }
                System.out.println("✅ Alert created and email sent for error ID: " + saved.getId());
            } catch (Exception e) {
                System.err.println("Failed to create alert or send email: " + e.getMessage());
                e.printStackTrace();
            }
            
            return convertToMap(saved);
        } catch (Exception e) {
            System.err.println("Error saving error log: " + e.getMessage());
            throw e;
        }
    }

    public boolean resolveError(Integer id) {
        try {
            Optional<ErrorLog> errorOpt = errorLogRepository.findById(id);
            if (errorOpt.isPresent()) {
                ErrorLog error = errorOpt.get();
                error.setStatus("Resolved");
                error.setResolvedAt(LocalDateTime.now());
                errorLogRepository.save(error);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error resolving error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteError(Integer id) {
        try {
            if (errorLogRepository.existsById(id)) {
                errorLogRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting error: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getRecentErrors(Integer userId, String userRole, int limit) {
        try {
            org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
            Pageable pageable = PageRequest.of(0, limit, sort);
            
            if ("ADMIN".equals(userRole)) {
                // Admin sees ALL recent errors
                return errorLogRepository.findAll(pageable).getContent().stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList());
            } else if ("DEVELOPER".equals(userRole)) {
                // Developer sees ONLY errors from their applications
                List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
                List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
                
                return errorLogRepository.findAll(pageable).getContent().stream()
                        .filter(error -> error.getApplicationId() != null && userAppIds.contains(error.getApplicationId()))
                        .map(this::convertToMap)
                        .collect(Collectors.toList());
            } else {
                // VIEWER sees read-only recent errors
                return errorLogRepository.findAll(pageable).getContent().stream()
                        .map(this::convertToMap)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            return List.of();
        }
    }

    public Long getTotalErrorsCount(Integer userId, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return errorLogRepository.count();
        } else if ("DEVELOPER".equals(userRole)) {
            List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
            List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
            return errorLogRepository.findAll().stream()
                .filter(error -> error.getApplicationId() != null && userAppIds.contains(error.getApplicationId()))
                .count();
        } else {
            return errorLogRepository.count();
        }
    }

    public Long getCriticalErrorsCount(Integer userId, String userRole) {
        try {
            if ("ADMIN".equals(userRole)) {
                return errorLogRepository.findAll().stream()
                    .filter(e -> e.getSeverity() == ErrorLog.ErrorSeverity.Critical)
                    .count();
            } else if ("DEVELOPER".equals(userRole)) {
                List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
                List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
                return errorLogRepository.findAll().stream()
                    .filter(e -> e.getSeverity() == ErrorLog.ErrorSeverity.Critical)
                    .filter(error -> error.getApplicationId() != null && userAppIds.contains(error.getApplicationId()))
                    .count();
            } else {
                return errorLogRepository.findAll().stream()
                    .filter(e -> e.getSeverity() == ErrorLog.ErrorSeverity.Critical)
                    .count();
            }
        } catch (Exception e) {
            return 0L;
        }
    }
    
    public Long getResolvedErrorsCount(Integer userId, String userRole) {
        try {
            if ("ADMIN".equals(userRole)) {
                return errorLogRepository.findAll().stream()
                    .filter(e -> "Resolved".equals(e.getStatus()))
                    .count();
            } else if ("DEVELOPER".equals(userRole)) {
                List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
                List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
                return errorLogRepository.findAll().stream()
                    .filter(e -> "Resolved".equals(e.getStatus()))
                    .filter(error -> error.getApplicationId() != null && userAppIds.contains(error.getApplicationId()))
                    .count();
            } else {
                return errorLogRepository.findAll().stream()
                    .filter(e -> "Resolved".equals(e.getStatus()))
                    .count();
            }
        } catch (Exception e) {
            return 0L;
        }
    }
    
    public List<Map<String, Object>> getSeverityBreakdown(Integer userId, String userRole) {
        try {
            List<ErrorLog> errors;
            if ("ADMIN".equals(userRole)) {
                errors = errorLogRepository.findAll();
            } else if ("DEVELOPER".equals(userRole)) {
                List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
                List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
                errors = errorLogRepository.findAll().stream()
                    .filter(error -> error.getApplicationId() != null && userAppIds.contains(error.getApplicationId()))
                    .collect(Collectors.toList());
            } else {
                errors = errorLogRepository.findAll();
            }
            
            Map<String, Long> counts = errors.stream()
                .collect(Collectors.groupingBy(
                    error -> error.getSeverity().toString(),
                    Collectors.counting()
                ));
            
            return List.of(
                Map.of("severity", "Critical", "count", counts.getOrDefault("Critical", 0L)),
                Map.of("severity", "High", "count", counts.getOrDefault("High", 0L)),
                Map.of("severity", "Medium", "count", counts.getOrDefault("Medium", 0L)),
                Map.of("severity", "Low", "count", counts.getOrDefault("Low", 0L))
            );
        } catch (Exception e) {
            return List.of(
                Map.of("severity", "Critical", "count", 0),
                Map.of("severity", "High", "count", 0),
                Map.of("severity", "Medium", "count", 0),
                Map.of("severity", "Low", "count", 0)
            );
        }
    }
    
    public Integer getApplicationIdByName(String applicationName) {
        try {
            if (applicationName == null) return null;
            
            var app = applicationService.getAllApplications().stream()
                .filter(a -> a.getName().equals(applicationName))
                .findFirst();
            return app.map(com.errormonitoring.model.Application::getId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error getting application ID by name: " + e.getMessage());
            return null;
        }
    }
    
    public boolean isApplicationPaused(String applicationName) {
        try {
            System.out.println("🔍 Looking for application: '" + applicationName + "'");
            
            // Get all applications and check if any match (case-insensitive partial match)
            List<com.errormonitoring.model.Application> allApps = applicationRepository.findAll();
            
            for (com.errormonitoring.model.Application app : allApps) {
                // Check exact match first
                if (app.getName().equals(applicationName)) {
                    System.out.println("📊 Exact match found: " + app.getName() + ", isPaused: " + app.getIsPaused());
                    return app.getIsPaused() != null && app.getIsPaused();
                }
                
                // Check case-insensitive partial match
                if (app.getName().toLowerCase().contains(applicationName.toLowerCase()) || 
                    applicationName.toLowerCase().contains(app.getName().toLowerCase())) {
                    System.out.println("📊 Partial match found: " + app.getName() + ", isPaused: " + app.getIsPaused());
                    return app.getIsPaused() != null && app.getIsPaused();
                }
            }
            
            System.out.println("⚠️ No matching application found for: '" + applicationName + "'");
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error checking pause status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public Map<String, Object> getTrendAnalysis(Integer userId, String userRole, String dateRange) {
        try {
            List<ErrorLog> errors;
            if ("ADMIN".equals(userRole)) {
                errors = errorLogRepository.findAll();
            } else if ("DEVELOPER".equals(userRole)) {
                List<com.errormonitoring.model.Application> userApps = applicationService.getApplicationsByUserId(userId);
                List<Integer> userAppIds = userApps.stream().map(app -> app.getId()).collect(Collectors.toList());
                errors = errorLogRepository.findAll().stream()
                    .filter(error -> error.getApplicationId() != null && userAppIds.contains(error.getApplicationId()))
                    .collect(Collectors.toList());
            } else {
                errors = errorLogRepository.findAll();
            }
            
            // Group errors by month
            Map<String, Long> monthlyErrors = errors.stream()
                .filter(error -> error.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                    error -> error.getCreatedAt().getYear() + "-" + 
                             String.format("%02d", error.getCreatedAt().getMonthValue()),
                    Collectors.counting()
                ));
            
            Map<String, Long> monthlyCritical = errors.stream()
                .filter(error -> error.getCreatedAt() != null && error.getSeverity() == ErrorLog.ErrorSeverity.Critical)
                .collect(Collectors.groupingBy(
                    error -> error.getCreatedAt().getYear() + "-" + 
                             String.format("%02d", error.getCreatedAt().getMonthValue()),
                    Collectors.counting()
                ));
            
            // Get last 6 months
            java.time.LocalDate now = java.time.LocalDate.now();
            List<Map<String, Object>> trends = new java.util.ArrayList<>();
            
            for (int i = 5; i >= 0; i--) {
                java.time.LocalDate date = now.minusMonths(i);
                String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                String monthName = date.getMonth().toString().substring(0, 3) + " " + date.getYear();
                
                trends.add(Map.of(
                    "date", monthKey,
                    "month", monthName,
                    "errors", monthlyErrors.getOrDefault(monthKey, 0L),
                    "critical", monthlyCritical.getOrDefault(monthKey, 0L)
                ));
            }
            
            long totalErrors = getTotalErrorsCount(userId, userRole);
            
            return Map.of(
                "trends", trends,
                "summary", Map.of(
                    "totalErrors", totalErrors,
                    "trend", totalErrors > 5 ? "increasing" : "stable"
                )
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                "trends", List.of(),
                "summary", Map.of("totalErrors", 0, "trend", "stable")
            );
        }
    }
    
    private Map<String, Object> convertToMap(ErrorLog error) {
        String applicationName = "Unknown App";
        if (error.getApplicationId() != null) {
            try {
                var app = applicationService.getApplicationById(error.getApplicationId());
                if (app.isPresent()) {
                    applicationName = app.get().getName();
                }
            } catch (Exception e) {
                System.err.println("Error getting application name: " + e.getMessage());
            }
        }
        
        return Map.of(
            "id", error.getId(),
            "application", applicationName,
            "endpoint", error.getApiEndpoint() != null ? error.getApiEndpoint() : "N/A",
            "errorMessage", error.getMessage() != null ? error.getMessage() : "No message",
            "severity", error.getSeverity().toString(),
            "status", error.getStatus(),
            "errorType", error.getErrorType() != null ? error.getErrorType() : "SystemError",
            "stackTrace", error.getStackTrace() != null ? error.getStackTrace() : "",
            "createdAt", error.getCreatedAt() != null ? error.getCreatedAt().toString() : ""
        );
    }
}