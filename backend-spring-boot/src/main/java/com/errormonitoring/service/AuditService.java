package com.errormonitoring.service;

import com.errormonitoring.model.AuditLog;
import com.errormonitoring.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Transactional
    public void logAuditAction(Integer userId, String action, String entityType, String entityId,
                               String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Ignore audit logging errors to not break main functionality
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }
    
    @Transactional
    public void logAuditAction(Integer userId, String action, String entityType, String entityId,
                               String oldValues, String newValues, HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValues(oldValues);
            auditLog.setNewValues(newValues);
            auditLog.setIpAddress(request != null ? getClientIpAddress(request) : null);
            auditLog.setUserAgent(request != null ? request.getHeader("User-Agent") : null);
            auditLog.setCreatedAt(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Ignore audit logging errors to not break main functionality
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    // Stub methods for compilation
    public java.util.List<AuditLog> getAuditLogs(int page, int size, String action, String entityType, Integer userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        return auditLogRepository.findAll(pageable).getContent();
    }
    
    public long getAuditLogsCount(String action, String entityType, Integer userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return auditLogRepository.count();
    }
    
    public java.util.Optional<AuditLog> getAuditLogById(Long id) {
        return auditLogRepository.findById(id.intValue());
    }
    
    public java.util.List<AuditLog> getUserActivity(Integer userId, int page, int size, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return auditLogRepository.findAll();
    }
    
    public long getUserActivityCount(Integer userId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return auditLogRepository.count();
    }
    
    public java.util.List<AuditLog> getEntityHistory(String entityType, String entityId, int page, int size) {
        return auditLogRepository.findAll();
    }
    
    public long getEntityHistoryCount(String entityType, String entityId) {
        return auditLogRepository.count();
    }
    
    public java.util.Map<String, Object> getAuditStats(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return java.util.Map.of("totalActions", auditLogRepository.count());
    }
}

