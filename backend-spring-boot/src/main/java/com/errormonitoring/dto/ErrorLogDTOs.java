package com.errormonitoring.dto;

import com.errormonitoring.model.ErrorLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ErrorLogDTOs {
    
    @Data
    public static class ErrorLogRequest {
        @NotBlank(message = "Application name is required")
        private String applicationName;
        
        @NotBlank(message = "Error message is required")
        private String errorMessage;
        
        private String stackTrace;
        private String source;
        
        @NotBlank(message = "Severity is required")
        private String severity; // Low, Medium, High, Critical
        
        private String status;
        private String environment;
        private String userAgent;
        private String ipAddress;
        private String httpMethod;
        private String apiEndpoint;
        private String errorType;
        private String apiKey; // Optional API key for authentication
    }
    
    @Data
    public static class ErrorLogResponse {
        private Long id;
        private ApplicationDTO application;
        private String endpoint;
        private String errorMessage;
        private String severity;
        private String status;
        private String errorType;
        private String stackTrace;
        private String createdAt;
    }
    
    @Data
    public static class ApplicationDTO {
        private Integer id;
        private String name;
    }
    
    @Data
    public static class ResolveErrorRequest {
        @NotNull(message = "Error ID is required")
        private Integer id;
    }
}

