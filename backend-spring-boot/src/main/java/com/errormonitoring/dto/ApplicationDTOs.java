package com.errormonitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class ApplicationDTOs {
    
    @Data
    public static class CreateApplicationRequest {
        @NotBlank(message = "Application name is required")
        @Size(max = 100, message = "Application name cannot exceed 100 characters")
        private String name;
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        @Size(max = 50, message = "Technology cannot exceed 50 characters")
        private String technology;
        
        @Size(max = 20, message = "Version cannot exceed 20 characters")
        private String version;
        
        @Size(max = 200, message = "Base URL cannot exceed 200 characters")
        private String baseUrl;
        
        @Size(max = 200, message = "Health check URL cannot exceed 200 characters")
        private String healthCheckUrl;
    }
    
    @Data
    public static class UpdateApplicationRequest {
        @Size(max = 100, message = "Application name cannot exceed 100 characters")
        private String name;
        
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        @Size(max = 50, message = "Technology cannot exceed 50 characters")
        private String technology;
        
        @Size(max = 20, message = "Version cannot exceed 20 characters")
        private String version;
        
        @Size(max = 200, message = "Base URL cannot exceed 200 characters")
        private String baseUrl;
        
        @Size(max = 200, message = "Health check URL cannot exceed 200 characters")
        private String healthCheckUrl;
        
        private Boolean isActive;
    }
    
    @Data
    public static class ApplicationResponse {
        private Integer id;
        private String name;
        private String description;
        private String technology;
        private String version;
        private String baseUrl;
        private String healthCheckUrl;
        private String apiKey;
        private Boolean isActive;
        private String createdAt;
        private String updatedAt;
        private Integer createdBy;
    }
}

