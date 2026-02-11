package com.errormonitoring.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "application_id", nullable = true)
    private Integer applicationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "alert_condition")
    private String condition;
    
    @Column(name = "recipients")
    private String recipients;
    
    @Column(name = "error_log_id")
    private String errorLogId;
    
    @Column(name = "alert_type")
    private String alertType;
    
    @Column(name = "alert_level")
    private String alertLevel;
    
    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_resolved")
    private Boolean isResolved = false;
    
    @Column(name = "created_by")
    private Integer createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}