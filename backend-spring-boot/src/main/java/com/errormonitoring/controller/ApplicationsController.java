package com.errormonitoring.controller;

import com.errormonitoring.dto.ApplicationDTOs;
import com.errormonitoring.repository.ApplicationRepository;
import com.errormonitoring.security.JwtTokenProvider;
import com.errormonitoring.service.ApplicationService;
import com.errormonitoring.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationsController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping
    public ResponseEntity<?> getApplications(HttpServletRequest request) {
        try {
            System.out.println("=== GET /api/applications called ===");
            String token = getTokenFromRequest(request);
            Integer userId = null;
            String userRole = null;
            
            if (token != null) {
                try {
                    userId = jwtTokenProvider.getUserIdFromToken(token);
                    userRole = jwtTokenProvider.getRoleFromToken(token);
                    System.out.println("User: " + userId + ", Role: " + userRole);
                } catch (Exception e) {
                    System.out.println("Token extraction failed: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid or expired token"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authorization token required"));
            }
            
            List<Map<String, Object>> applications;
            if ("ADMIN".equals(userRole)) {
                // ADMIN sees ALL applications
                applications = applicationService.getAllApplicationsAsMap();
                System.out.println("ADMIN - Found " + applications.size() + " applications");
            } else if ("DEVELOPER".equals(userRole)) {
                // DEVELOPER sees ONLY their own applications
                applications = applicationService.getUserApplicationsAsMap(userId);
                System.out.println("DEVELOPER - Found " + applications.size() + " applications for user " + userId);
            } else {
                // VIEWER sees ALL applications but read-only
                applications = applicationService.getAllApplicationsAsMap();
                System.out.println("VIEWER - Found " + applications.size() + " applications (read-only)");
            }
            
            System.out.println("Returning applications: " + applications);
            return ResponseEntity.ok(applications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch applications", "error", e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createApplication(@Valid @RequestBody ApplicationDTOs.CreateApplicationRequest request,
                                                HttpServletRequest httpRequest) {
        try {
            System.out.println("Received application request: " + request.getName() + ", " + request.getDescription() + ", " + request.getTechnology());
            
            String token = getTokenFromRequest(httpRequest);
            Integer userId = 1; // Default user ID
            
            if (token != null) {
                try {
                    userId = jwtTokenProvider.getUserIdFromToken(token);
                    System.out.println("Creating application for userId: " + userId);
                } catch (Exception e) {
                    System.out.println("Failed to extract userId from token: " + e.getMessage());
                }
            }
            
            var application = applicationService.createApplication(request, userId);
            ApplicationDTOs.ApplicationResponse response = new ApplicationDTOs.ApplicationResponse();
            response.setId(application.getId());
            response.setName(application.getName());
            response.setDescription(application.getDescription());
            response.setTechnology(application.getTechnology());
            response.setVersion(application.getVersion());
            response.setBaseUrl(application.getBaseUrl());
            response.setHealthCheckUrl(application.getHealthCheckUrl());
            response.setApiKey(application.getApiKey());
            response.setIsActive(application.getIsActive());
            response.setCreatedAt(application.getCreatedAt().toString());
            response.setCreatedBy(application.getCreatedBy());
            
            try {
                auditLogService.logActivity(userId, "CREATE_APPLICATION", "Application", application.getId().toString(),
                    null, application.getName(), getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to create application", "error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(@PathVariable Integer id,
                                                @Valid @RequestBody ApplicationDTOs.UpdateApplicationRequest request,
                                                HttpServletRequest httpRequest) {
        try {
            var application = applicationService.updateApplication(id, request);
            ApplicationDTOs.ApplicationResponse response = new ApplicationDTOs.ApplicationResponse();
            response.setId(application.getId());
            response.setName(application.getName());
            response.setDescription(application.getDescription());
            response.setTechnology(application.getTechnology());
            response.setVersion(application.getVersion());
            response.setBaseUrl(application.getBaseUrl());
            response.setHealthCheckUrl(application.getHealthCheckUrl());
            response.setApiKey(application.getApiKey());
            response.setIsActive(application.getIsActive());
            response.setUpdatedAt(application.getUpdatedAt() != null ? application.getUpdatedAt().toString() : null);
            
            try {
                String token = getTokenFromRequest(httpRequest);
                Integer userId = jwtTokenProvider.getUserIdFromToken(token);
                auditLogService.logActivity(userId, "UPDATE_APPLICATION", "Application", id.toString(),
                    null, application.getName(), getClientIP(httpRequest), httpRequest.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to update application", "error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/pause")
    public ResponseEntity<?> pauseApplication(@PathVariable Integer id, HttpServletRequest request) {
        try {
            var application = applicationService.pauseApplication(id);
            
            try {
                String token = getTokenFromRequest(request);
                Integer userId = 1;
                if (token != null) {
                    try {
                        userId = jwtTokenProvider.getUserIdFromToken(token);
                    } catch (Exception e) {
                        // Use default
                    }
                }
                
                auditLogService.logActivity(userId, "PAUSE_APPLICATION", "Application", id.toString(),
                    null, application.getName(), getClientIP(request), request.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("message", "Application paused successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to pause application", "error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/resume")
    public ResponseEntity<?> resumeApplication(@PathVariable Integer id, HttpServletRequest request) {
        try {
            var application = applicationService.resumeApplication(id);
            
            try {
                String token = getTokenFromRequest(request);
                Integer userId = 1;
                if (token != null) {
                    try {
                        userId = jwtTokenProvider.getUserIdFromToken(token);
                    } catch (Exception e) {
                        // Use default
                    }
                }
                
                auditLogService.logActivity(userId, "RESUME_APPLICATION", "Application", id.toString(),
                    null, application.getName(), getClientIP(request), request.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("message", "Application resumed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to resume application", "error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Integer id, HttpServletRequest request) {
        try {
            boolean success = applicationService.deleteApplication(id);
            if (!success) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Application not found"));
            }
            
            try {
                String token = getTokenFromRequest(request);
                Integer userId = 1;
                if (token != null) {
                    try {
                        userId = jwtTokenProvider.getUserIdFromToken(token);
                    } catch (Exception e) {
                        // Use default
                    }
                }
                
                auditLogService.logActivity(userId, "DELETE_APPLICATION", "Application", id.toString(),
                    "Application ID: " + id, null, getClientIP(request), request.getHeader("User-Agent"));
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("message", "Application deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to delete application", "error", e.getMessage()));
        }
    }
    
    @PostMapping("/fix-missing-keys")
    public ResponseEntity<?> fixMissingApiKeys() {
        try {
            var applications = applicationService.getApplications(null, "ADMIN");
            int updatedCount = 0;
            
            for (var app : applications) {
                if (app.getApiKey() == null || app.getApiKey().isEmpty()) {
                    app.setApiKey(generateSimpleApiKey());
                    applicationRepository.save(app);
                    updatedCount++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Fixed missing API keys",
                "updatedCount", updatedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fix API keys", "error", e.getMessage()));
        }
    }
    
    private String generateSimpleApiKey() {
        SecureRandom random = new SecureRandom();
        int randomNumber = 100000 + random.nextInt(900000);
        int currentYear = java.time.LocalDate.now().getYear();
        return String.format("APP-%06d-%d", randomNumber, currentYear);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) return null;
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

