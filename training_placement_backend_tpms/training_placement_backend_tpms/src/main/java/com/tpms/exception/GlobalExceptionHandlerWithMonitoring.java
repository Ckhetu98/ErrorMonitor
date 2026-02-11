package com.tpms.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandlerWithMonitoring {

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${error.monitoring.api.url}")
    private String errorMonitoringApi;
    
    @Value("${error.monitoring.application.id}")
    private String applicationId;
    
    @Value("${error.monitoring.application.name}")
    private String applicationName;
    
    @Value("${error.monitoring.api.key}")
    private String apiKey;
    
    @Value("${error.monitoring.backend.type:dotnet}")
    private String backendType;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request,
                                                 HttpServletRequest httpRequest) {
        
        logErrorToMonitoringSystem(ex, request, httpRequest);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", 500);
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, 
                                                           WebRequest request,
                                                           HttpServletRequest httpRequest) {
        logErrorToMonitoringSystem(ex, request, httpRequest);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", 404);
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException ex, WebRequest request,
                                              HttpServletRequest httpRequest) {
        logErrorToMonitoringSystem(ex, request, httpRequest);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", 400);
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getDescription(false));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    private void logErrorToMonitoringSystem(Exception ex, WebRequest request, 
                                          HttpServletRequest httpRequest) {
        try {
            System.out.println("🔑 API Key from config: '" + apiKey + "'");
            
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("applicationName", applicationName);
            errorData.put("errorMessage", ex.getMessage());
            errorData.put("stackTrace", getStackTraceAsString(ex));
            errorData.put("severity", determineSeverity(ex));
            errorData.put("apiEndpoint", extractEndpoint(request.getDescription(false)));
            errorData.put("httpMethod", httpRequest.getMethod());
            errorData.put("userAgent", httpRequest.getHeader("User-Agent"));
            errorData.put("ipAddress", getClientIpAddress(httpRequest));
            errorData.put("apiKey", apiKey);
            errorData.put("environment", "PRODUCTION");
            
            System.out.println("🔑 Sending API key: '" + apiKey + "'");
            System.out.println("📤 Sending data: " + errorData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(errorData, headers);
            
            restTemplate.postForEntity(errorMonitoringApi, entity, String.class);
            System.out.println("✅ Error logged to monitoring system: " + ex.getMessage());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to log to monitoring system: " + e.getMessage());
        }
    }
    
    private String determineSeverity(Exception ex) {
        if (ex instanceof SecurityException || ex.getClass().getSimpleName().contains("Auth")) {
            return "Critical";
        } else if (ex instanceof NullPointerException || ex instanceof RuntimeException) {
            return "High";
        } else if (ex instanceof IllegalArgumentException || ex.getClass().getSimpleName().contains("Validation")) {
            return "Medium";
        } else {
            return "Low";
        }
    }
    
    private Integer determineStatusCode(Exception ex) {
        if (ex instanceof ResourceNotFoundException) return 404;
        if (ex instanceof ApiException) return 400;
        if (ex instanceof SecurityException) return 401;
        return 500;
    }
    
    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
        
        StackTraceElement[] elements = ex.getStackTrace();
        int limit = Math.min(elements.length, 20);
        
        for (int i = 0; i < limit; i++) {
            sb.append("\tat ").append(elements[i].toString()).append("\n");
        }
        
        return sb.toString();
    }
    
    private String extractEndpoint(String description) {
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}