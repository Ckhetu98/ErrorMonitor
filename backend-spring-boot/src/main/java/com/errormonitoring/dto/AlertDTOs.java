package com.errormonitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class AlertDTOs {
    
    @Data
    public static class CreateAlertRequest {
        @NotNull(message = "Application ID is required")
        private Integer applicationId;
        
        @NotNull(message = "Error Log ID is required")
        private String errorLogId;
        
        @NotBlank(message = "Alert type is required")
        private String alertType;
        
        @NotBlank(message = "Alert level is required")
        private String alertLevel;
        
        @NotBlank(message = "Alert message is required")
        private String alertMessage;
        
        @Override
        public String toString() {
            return "CreateAlertRequest{" +
                    "applicationId=" + applicationId +
                    ", errorLogId='" + errorLogId + '\'' +
                    ", alertType='" + alertType + '\'' +
                    ", alertLevel='" + alertLevel + '\'' +
                    ", alertMessage='" + alertMessage + '\'' +
                    '}';
        }
    }
    
    @Data
    public static class UpdateAlertRequest {
        private String alertType;
        private String alertLevel;
        private String alertMessage;
        private Boolean isActive;
    }
    
    @Data
    public static class AlertResponse {
        private Integer id;
        private Integer applicationId;
        private String applicationName;
        private String errorLogId;
        private String alertType;
        private String alertLevel;
        private String alertMessage;
        private Boolean isActive;
        private Boolean isResolved;
        private String createdAt;
        private String resolvedAt;
        private Integer createdBy;
    }
}

