package com.errormonitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_logs", indexes = {
    @Index(name = "idx_application_id", columnList = "application_id"),
    @Index(name = "idx_severity", columnList = "severity"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ErrorLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "application_id", nullable = true)
    private Integer applicationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;
    
    @Column(nullable = false, length = 2000, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "stack_trace", length = 5000, columnDefinition = "TEXT")
    private String stackTrace;
    
    @Column(length = 200)
    private String source;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ErrorSeverity severity;
    
    @Column(length = 50)
    private String status = "Open";
    
    @Column(length = 50)
    private String environment;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    @Column(name = "error_type", length = 100)
    private String errorType;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    public enum ErrorSeverity {
        Low, Medium, High, Critical
    }
}

