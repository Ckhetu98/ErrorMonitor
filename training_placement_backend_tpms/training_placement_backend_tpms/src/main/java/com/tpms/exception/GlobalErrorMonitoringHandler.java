package com.tpms.exception;

import com.tpms.service.ErrorMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@ConditionalOnProperty(name = "error.monitoring.enabled", havingValue = "true")
public class GlobalErrorMonitoringHandler {

    @Autowired(required = false)
    private ErrorMonitoringService errorMonitoringService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        
        // Log to monitoring service
        if (errorMonitoringService != null) {
            String endpoint = extractEndpoint(request);
            String severity = determineSeverity(ex);
            errorMonitoringService.logError(ex, endpoint, severity);
        }

        // Return original error response (don't interfere with existing error handling)
        return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String extractEndpoint(WebRequest request) {
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return "/unknown";
    }

    private String determineSeverity(Exception ex) {
        String exceptionName = ex.getClass().getSimpleName().toLowerCase();
        
        if (exceptionName.contains("security") || exceptionName.contains("authentication")) {
            return "Critical";
        } else if (exceptionName.contains("sql") || exceptionName.contains("database")) {
            return "High";
        } else if (exceptionName.contains("validation") || exceptionName.contains("illegal")) {
            return "Medium";
        } else {
            return "High"; // Default to High for unknown exceptions
        }
    }
}