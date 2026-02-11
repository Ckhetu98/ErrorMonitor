package com.errormonitoring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDTOs {
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Password is required")
        private String password;
    }
    
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;
        
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;
        
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        
        private String confirmPassword;
        private String firstName;
        private String lastName;
        private String role = "VIEWER";
    }
    
    @Data
    public static class VerifyOTPRequest {
        @NotBlank(message = "User ID is required")
        private String userId;
        
        @NotBlank(message = "OTP code is required")
        private String code;
    }
    
    @Data
    public static class Enable2FARequest {
        private Integer userId;
    }
    
    @Data
    public static class GlobalToggle2FARequest {
        private Boolean enabled;
    }
    
    @Data
    public static class ResendOTPRequest {
        @NotBlank(message = "User ID is required")
        private String userId;
    }
    
    @Data
    public static class LoginResponse {
        private String token;
        private UserResponse user;
        private String expiresAt;
        private Boolean requiresTwoFactor;
        private String userEmail;
    }
    
    @Data
    public static class UserResponse {
        private Integer id;
        private String username;
        private String email;
        private String role;
        private String firstName;
        private String lastName;
        private Boolean isActive;
        private String lastLoginAt;
        private Boolean twoFactorEnabled;
    }
}

