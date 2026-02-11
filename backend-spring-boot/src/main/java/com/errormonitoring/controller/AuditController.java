package com.errormonitoring.controller;

import com.errormonitoring.model.AuditLog;
import com.errormonitoring.service.AuditLogService;
import com.errormonitoring.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    
    @Autowired
    private com.errormonitoring.security.JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<?> getAuditLogs(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            System.out.println("Token extracted: " + (token != null ? "Yes" : "No"));
            
            if (token == null) {
                System.out.println("No token found, returning empty logs");
                return ResponseEntity.ok(Map.of("logs", List.of()));
            }
            
            Integer userId;
            String userRole;
            
            try {
                userId = jwtTokenProvider.getUserIdFromToken(token);
                userRole = jwtTokenProvider.getRoleFromToken(token);
                System.out.println("Fetching audit logs for user: " + userId + ", role: " + userRole);
            } catch (Exception e) {
                System.err.println("JWT token validation failed: " + e.getMessage());
                return ResponseEntity.ok(Map.of("logs", List.of()));
            }
            
            if (userId == null || userRole == null) {
                System.out.println("UserId or userRole is null after token extraction");
                return ResponseEntity.ok(Map.of("logs", List.of()));
            }
            
            List<AuditLog> auditLogs;
            
            if ("ADMIN".equals(userRole)) {
                auditLogs = auditLogService.getAllAuditLogs();
            } else if ("DEVELOPER".equals(userRole)) {
                auditLogs = auditLogService.getUserAuditLogs(userId);
            } else if ("VIEWER".equals(userRole)) {
                auditLogs = auditLogService.getAllAuditLogs();
            } else {
                return ResponseEntity.ok(Map.of("logs", List.of()));
            }
            
            System.out.println("Found " + auditLogs.size() + " audit logs");
            
            List<Map<String, Object>> formattedLogs = auditLogs.stream()
                .map(this::convertToFrontendFormat)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of("logs", formattedLogs));
        } catch (Exception e) {
            System.err.println("Error in getAuditLogs: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("logs", List.of()));
        }
    }
    
    private Map<String, Object> convertToFrontendFormat(AuditLog auditLog) {
        Map<String, Object> formatted = new HashMap<>();
        formatted.put("id", auditLog.getId());
        formatted.put("action", auditLog.getAction());
        formatted.put("entityType", auditLog.getEntityType());
        formatted.put("entityId", auditLog.getEntityId());
        formatted.put("userId", auditLog.getUserId());
        formatted.put("createdAt", auditLog.getCreatedAt().toString());
        formatted.put("ipAddress", auditLog.getIpAddress());
        formatted.put("userAgent", auditLog.getUserAgent());
        formatted.put("oldValues", auditLog.getOldValues());
        formatted.put("newValues", auditLog.getNewValues());
        
        // Get username from auth service
        try {
            var userOpt = authService.getUserById(auditLog.getUserId());
            if (userOpt.isPresent()) {
                formatted.put("userName", userOpt.get().getUsername());
            } else {
                formatted.put("userName", "Unknown User");
            }
        } catch (Exception e) {
            formatted.put("userName", "System");
        }
        
        return formatted;
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            System.out.println("Request is null");
            return null;
        }
        
        String bearerToken = request.getHeader("Authorization");
        System.out.println("Authorization header: " + bearerToken);
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            System.out.println("Extracted token length: " + token.length());
            return token;
        }
        
        System.out.println("No valid Bearer token found");
        return null;
    }
}