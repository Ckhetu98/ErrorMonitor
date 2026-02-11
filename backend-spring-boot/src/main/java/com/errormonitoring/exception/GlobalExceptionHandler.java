package com.errormonitoring.exception;

import com.errormonitoring.service.AlertService;
import com.errormonitoring.service.EmailNotificationService;
import com.errormonitoring.service.ErrorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ErrorLogService errorLogService;
    
    @Autowired
    private AlertService alertService;
    
    @Autowired
    private EmailNotificationService emailService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        try {
            // Log error and create alert
            Map<String, Object> errorData = Map.of(
                "applicationName", "TPMS Backend",
                "apiEndpoint", request.getDescription(false).replace("uri=", ""),
                "errorMessage", ex.getMessage(),
                "severity", "High",
                "errorType", ex.getClass().getSimpleName(),
                "stackTrace", ex.toString()
            );
            
            Map<String, Object> savedError = errorLogService.logError(errorData);
            
            // Auto-create alert
            Map<String, Object> alertData = Map.of(
                "applicationId", 1,
                "errorLogId", savedError.get("id").toString(),
                "alertType", "EMAIL",
                "alertLevel", "HIGH",
                "alertMessage", ex.getMessage()
            );
            
            alertService.createAlert(alertData, 1);
            
            // Send email to appropriate recipients
            String recipients = alertService.getErrorRecipients(25); // TPMS application ID
            if (recipients != null && !recipients.isEmpty()) {
                String[] recipientEmails = recipients.split(",\\s*");
                for (String email : recipientEmails) {
                    emailService.sendErrorNotification(email.trim(), 
                        "🚨 High Alert - TPMS Backend", 
                        "Alert: TPMS Backend has error\n\nError: " + ex.getMessage());
                }
            }
            
            System.out.println("✅ Alert created for exception: " + ex.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to create alert: " + e.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "Internal server error"));
    }
}