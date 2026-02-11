package com.errormonitoring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class ContactDTOs {
    
    @Data
    public static class ContactRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name cannot exceed 200 characters")
        private String fullName;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        private String email;
        
        @Size(max = 20, message = "Phone cannot exceed 20 characters")
        private String phone;
        
        @NotBlank(message = "Subject is required")
        @Size(max = 300, message = "Subject cannot exceed 300 characters")
        private String subject;
        
        @NotBlank(message = "Message is required")
        private String message;
    }
    
    @Data
    public static class ContactResponse {
        private Integer id;
        private String fullName;
        private String email;
        private String subject;
        private String message;
        private String status;
        private String createdAt;
    }
}

