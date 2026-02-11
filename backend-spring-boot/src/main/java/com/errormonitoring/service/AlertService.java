package com.errormonitoring.service;

import com.errormonitoring.dto.AlertDTOs;
import com.errormonitoring.model.Alert;
import com.errormonitoring.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertService {
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private com.errormonitoring.repository.ApplicationRepository applicationRepository;
    
    @Autowired
    private com.errormonitoring.repository.UserRepository userRepository;
    
    public String getAlertRecipientsPublic(Integer applicationId, Integer createdBy) {
        return getAlertRecipients(applicationId, createdBy);
    }
    
    public String getErrorRecipients(Integer applicationId) {
        try {
            if (applicationId == null) {
                // For errors without specific application, send to admin
                return "admin@errormonitoring.com";
            }
            
            // Get the application
            var app = applicationRepository.findById(applicationId);
            if (app.isEmpty()) {
                return "admin@errormonitoring.com"; // fallback
            }
            
            var application = app.get();
            if (application.getCreatedBy() == null) {
                return "admin@errormonitoring.com"; // fallback
            }
            
            // Get the user who created the application
            var appCreator = userRepository.findById(application.getCreatedBy());
            if (appCreator.isEmpty()) {
                return "admin@errormonitoring.com"; // fallback
            }
            
            var creator = appCreator.get();
            String creatorEmail = creator.getEmail();
            
            if ("DEVELOPER".equals(creator.getRole())) {
                // If developer created the app, send to developer only
                return creatorEmail;
            } else if ("ADMIN".equals(creator.getRole())) {
                // If admin created the app, send to admin only
                return creatorEmail;
            }
            
            return creatorEmail;
        } catch (Exception e) {
            System.err.println("Error getting error recipients: " + e.getMessage());
            return "admin@errormonitoring.com"; // fallback on error
        }
    }
    
    private String getAlertRecipients(Integer applicationId, Integer createdBy) {
        try {
            // Get the user who created the alert
            var alertCreator = userRepository.findById(createdBy);
            if (alertCreator.isEmpty()) {
                return "admin@errormonitoring.com"; // fallback
            }
            
            var creator = alertCreator.get();
            String creatorEmail = creator.getEmail();
            
            if ("DEVELOPER".equals(creator.getRole())) {
                return creatorEmail; // Developer gets their own email
            } else if ("ADMIN".equals(creator.getRole())) {
                // Admin: check who created the application
                if (applicationId != null) {
                    var app = applicationRepository.findById(applicationId);
                    if (app.isPresent() && app.get().getCreatedBy() != null) {
                        var appCreator = userRepository.findById(app.get().getCreatedBy());
                        if (appCreator.isPresent() && !appCreator.get().getId().equals(createdBy)) {
                            // App created by developer, send to both
                            return creatorEmail + "," + appCreator.get().getEmail();
                        }
                    }
                }
                return creatorEmail; // Admin created app, send only to admin
            }
            
            return creatorEmail;
        } catch (Exception e) {
            return "admin@errormonitoring.com"; // fallback on error
        }
    }
    
    @Transactional
    public Alert createAlert(AlertDTOs.CreateAlertRequest request, Integer createdBy) {
        Alert alert = new Alert();
        alert.setApplicationId(request.getApplicationId());
        alert.setName(request.getAlertMessage());
        alert.setDescription("Alert for error log: " + request.getErrorLogId());
        alert.setCondition(request.getAlertLevel() + " level alert");
        alert.setRecipients(getAlertRecipients(request.getApplicationId(), createdBy));
        alert.setErrorLogId(request.getErrorLogId());
        alert.setAlertType(request.getAlertType());
        alert.setAlertLevel(request.getAlertLevel());
        alert.setAlertMessage(request.getAlertMessage());
        alert.setIsActive(true);
        alert.setIsResolved(false);
        alert.setCreatedBy(createdBy);
        alert.setCreatedAt(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }
    
    @Transactional
    public Alert createAlert(Map<String, Object> alertData, Integer createdBy) {
        Alert alert = new Alert();
        
        // Get application ID from error data
        Integer appId = null;
        if (alertData.get("applicationId") != null) {
            appId = Integer.parseInt(alertData.get("applicationId").toString());
        } else if (alertData.get("applicationName") != null) {
            // Try to find application by name
            String appName = (String) alertData.get("applicationName");
            var app = applicationRepository.findAll().stream()
                .filter(a -> a.getName().equals(appName))
                .findFirst();
            appId = app.map(com.errormonitoring.model.Application::getId).orElse(null);
        }
        
        alert.setApplicationId(appId); // Set the application ID
        
        String applicationName = (String) alertData.get("applicationName");
        alert.setName(applicationName != null ? applicationName : "Auto Alert");
        alert.setDescription("Auto-generated alert for error log: " + alertData.get("errorLogId"));
        alert.setCondition(alertData.get("alertLevel") + " level alert");
        alert.setRecipients(getErrorRecipients(appId));
        alert.setErrorLogId((String) alertData.get("errorLogId"));
        alert.setAlertType((String) alertData.get("alertType"));
        alert.setAlertLevel((String) alertData.get("alertLevel"));
        alert.setAlertMessage((String) alertData.get("alertMessage"));
        alert.setIsActive(true);
        alert.setIsResolved(false);
        
        // Set createdBy to the application owner instead of system user
        if (appId != null) {
            var app = applicationRepository.findById(appId);
            if (app.isPresent() && app.get().getCreatedBy() != null) {
                alert.setCreatedBy(app.get().getCreatedBy()); // Use app owner as alert creator
            } else {
                alert.setCreatedBy(createdBy); // fallback to system user
            }
        } else {
            alert.setCreatedBy(createdBy); // fallback to system user
        }
        
        alert.setCreatedAt(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }
    
    @Transactional
    public Alert updateAlert(Integer id, AlertDTOs.UpdateAlertRequest request) {
        Optional<Alert> alertOpt = alertRepository.findById(id);
        if (alertOpt.isEmpty()) {
            throw new RuntimeException("Alert not found");
        }
        
        Alert alert = alertOpt.get();
        if (request.getAlertType() != null) {
            alert.setAlertType(request.getAlertType());
        }
        if (request.getAlertLevel() != null) {
            alert.setAlertLevel(request.getAlertLevel());
        }
        if (request.getAlertMessage() != null) {
            alert.setAlertMessage(request.getAlertMessage());
        }
        if (request.getIsActive() != null) {
            alert.setIsActive(request.getIsActive());
        }
        
        return alertRepository.save(alert);
    }
    
    public List<AlertDTOs.AlertResponse> getAlerts(Integer userId, String userRole) {
        List<Alert> alerts;
        
        System.out.println("Getting alerts for user " + userId + " with role " + userRole);
        
        if ("ADMIN".equals(userRole)) {
            // Admin sees all alerts
            alerts = alertRepository.findAllByOrderByCreatedAtDesc();
            System.out.println("ADMIN - Found " + alerts.size() + " total alerts");
        } else if ("DEVELOPER".equals(userRole)) {
            // Developer sees alerts for their applications only
            List<Alert> allAlerts = alertRepository.findAllByOrderByCreatedAtDesc();
            System.out.println("Total alerts in database: " + allAlerts.size());
            
            alerts = allAlerts.stream()
                .filter(alert -> {
                    if (alert.getApplicationId() == null) {
                        System.out.println("Alert " + alert.getId() + " has null applicationId");
                        return false;
                    }
                    var appOpt = getApplicationById(alert.getApplicationId());
                    if (appOpt.isEmpty()) {
                        System.out.println("Alert " + alert.getId() + " - application " + alert.getApplicationId() + " not found");
                        return false;
                    }
                    boolean matches = appOpt.get().getCreatedBy().equals(userId);
                    System.out.println("Alert " + alert.getId() + " - app " + alert.getApplicationId() + " created by " + appOpt.get().getCreatedBy() + ", matches user " + userId + ": " + matches);
                    return matches;
                })
                .collect(Collectors.toList());
            System.out.println("DEVELOPER - Found " + alerts.size() + " alerts for user " + userId);
        } else {
            // VIEWER sees all alerts but read-only
            alerts = alertRepository.findAllByOrderByCreatedAtDesc();
            System.out.println("VIEWER - Found " + alerts.size() + " total alerts");
        }
        
        return alerts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<AlertDTOs.AlertResponse> getUnresolvedAlerts(Integer userId, String userRole) {
        List<Alert> alerts;
        
        if ("ADMIN".equals(userRole)) {
            alerts = alertRepository.findByIsResolvedFalse();
        } else if ("DEVELOPER".equals(userRole)) {
            // Developer sees only alerts for their applications
            alerts = alertRepository.findByIsResolvedFalse().stream()
                .filter(alert -> {
                    if (alert.getApplicationId() == null) return false;
                    var appOpt = getApplicationById(alert.getApplicationId());
                    return appOpt.isPresent() && appOpt.get().getCreatedBy().equals(userId);
                })
                .collect(Collectors.toList());
        } else {
            // VIEWER sees no unresolved alerts (read-only)
            alerts = List.of();
        }
        
        return alerts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public Optional<Alert> getAlertById(Integer id) {
        return alertRepository.findById(id);
    }
    
    public Optional<com.errormonitoring.model.Application> getApplicationById(Integer applicationId) {
        return applicationRepository.findById(applicationId);
    }
    
    @Transactional
    public boolean deleteAlert(Integer id) {
        if (!alertRepository.existsById(id)) {
            return false;
        }
        alertRepository.deleteById(id);
        return true;
    }
    
    @Transactional
    public boolean resolveAlert(Integer id) {
        Optional<Alert> alertOpt = alertRepository.findById(id);
        if (alertOpt.isEmpty()) {
            return false;
        }
        
        Alert alert = alertOpt.get();
        alert.setResolvedAt(LocalDateTime.now());
        alert.setIsResolved(true);
        alert.setIsActive(false);
        alertRepository.save(alert);
        return true;
    }
    
    public List<com.errormonitoring.model.Application> getAllApplicationsRaw() {
        return applicationRepository.findAll();
    }
    
    private AlertDTOs.AlertResponse mapToResponse(Alert alert) {
        AlertDTOs.AlertResponse response = new AlertDTOs.AlertResponse();
        response.setId(alert.getId());
        response.setApplicationId(alert.getApplicationId());
        response.setApplicationName(alert.getName()); // Use alert name as application name
        response.setErrorLogId(alert.getErrorLogId());
        response.setAlertType(alert.getAlertType());
        response.setAlertLevel(alert.getAlertLevel());
        response.setAlertMessage(alert.getAlertMessage());
        response.setIsActive(alert.getIsActive());
        response.setIsResolved(alert.getIsResolved());
        response.setCreatedAt(alert.getCreatedAt().toString());
        response.setResolvedAt(alert.getResolvedAt() != null ? alert.getResolvedAt().toString() : null);
        response.setCreatedBy(alert.getCreatedBy());
        return response;
    }
    
    @Transactional
    public void updateAllAlertRecipients() {
        try {
            List<Alert> alerts = alertRepository.findAll();
            for (Alert alert : alerts) {
                if ("choudharykhetesh8@gmail.com".equals(alert.getRecipients())) {
                    String newRecipients = getErrorRecipients(alert.getApplicationId());
                    alert.setRecipients(newRecipients);
                    alertRepository.save(alert);
                    System.out.println("Updated alert " + alert.getId() + " recipients to: " + newRecipients);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating alert recipients: " + e.getMessage());
        }
    }
}

