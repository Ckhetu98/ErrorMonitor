package com.tpms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/error-monitoring")
@CrossOrigin(origins = "*")
public class ErrorMonitoringController {

    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${error.monitoring.api.url}")
    private String errorMonitoringApi;
    
    @Value("${error.monitoring.application.id}")
    private String applicationId;
    
    @Value("${error.monitoring.application.name}")
    private String applicationName;
    
    @PostMapping("/log-error")
    public ResponseEntity<?> logError(@RequestBody ErrorLogRequest request, 
                                    HttpServletRequest httpRequest) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("applicationId", applicationId);
            errorData.put("applicationName", applicationName);
            errorData.put("apiEndpoint", request.getApiEndpoint());
            errorData.put("errorMessage", request.getErrorMessage());
            errorData.put("stackTrace", request.getStackTrace());
            errorData.put("severity", request.getSeverity());
            errorData.put("httpStatusCode", request.getHttpStatusCode() != null ? request.getHttpStatusCode().intValue() : 500);
            errorData.put("userAgent", httpRequest.getHeader("User-Agent"));
            errorData.put("ipAddress", getClientIpAddress(httpRequest));
            errorData.put("timestamp", LocalDateTime.now().toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(errorData, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                errorMonitoringApi, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("✅ Error logged to monitoring system");
            } else {
                return ResponseEntity.status(500).body("❌ Failed to log to monitoring system");
            }
        } catch (Exception e) {
            System.err.println("Failed to log error to monitoring system: " + e.getMessage());
            return ResponseEntity.status(500).body("❌ Exception while logging: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
    
    public static class ErrorLogRequest {
        private String apiEndpoint;
        private String errorMessage;
        private String stackTrace;
        private String severity;
        private Integer httpStatusCode;
        
        public String getApiEndpoint() { return apiEndpoint; }
        public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getStackTrace() { return stackTrace; }
        public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
        
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        
        public Integer getHttpStatusCode() { return httpStatusCode; }
        public void setHttpStatusCode(Integer httpStatusCode) { this.httpStatusCode = httpStatusCode; }
    }
}