package com.errormonitoring.service;

import com.errormonitoring.model.AuditLog;
import com.errormonitoring.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    public List<AuditLog> getAllAuditLogs() {
        try {
            List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            System.out.println("Retrieved " + logs.size() + " audit logs from database");
            return logs;
        } catch (Exception e) {
            System.err.println("Error retrieving audit logs: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<AuditLog> getUserAuditLogs(Integer userId) {
        return auditLogRepository.findByUserId(userId, 
            PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
    
    public void logActivity(Integer userId, String action, String entityType, String entityId, 
                           String oldValues, String newValues, String ipAddress, String userAgent) {
        try {
            System.out.println("Creating audit log: userId=" + userId + ", action=" + action);
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setOldValues(oldValues);
            auditLog.setNewValues(newValues);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            
            AuditLog saved = auditLogRepository.save(auditLog);
            System.out.println("Audit log saved with ID: " + saved.getId());
        } catch (Exception e) {
            System.err.println("Failed to log audit activity: " + e.getMessage());
            e.printStackTrace();
        }
    }
}