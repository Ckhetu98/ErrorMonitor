package com.errormonitoring.repository;

import com.errormonitoring.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    Page<AuditLog> findByUserId(Integer userId, Pageable pageable);
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.userId = ?1")
    void deleteByUserId(Integer userId);
}

