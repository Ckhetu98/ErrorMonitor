package com.errormonitoring.repository;

import com.errormonitoring.model.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Integer> {
    
    Page<ErrorLog> findByApplicationId(Integer applicationId, Pageable pageable);
    
    List<ErrorLog> findBySeverity(ErrorLog.ErrorSeverity severity);
    
    @Query("SELECT e FROM ErrorLog e WHERE e.applicationId IN " +
           "(SELECT a.id FROM Application a WHERE a.createdBy = :userId OR :role = 'ADMIN') " +
           "AND (:severity IS NULL OR e.severity = :severity) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<ErrorLog> findFilteredErrors(
        @Param("userId") Integer userId,
        @Param("role") String role,
        @Param("severity") ErrorLog.ErrorSeverity severity,
        @Param("status") String status,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(e) FROM ErrorLog e WHERE e.applicationId IN " +
           "(SELECT a.id FROM Application a WHERE a.createdBy = :userId OR :role = 'ADMIN')")
    Long countByUserOrAdmin(@Param("userId") Integer userId, @Param("role") String role);
    
    @Query("SELECT e FROM ErrorLog e WHERE e.applicationId IN " +
           "(SELECT a.id FROM Application a WHERE a.createdBy = :userId OR :role = 'ADMIN') " +
           "ORDER BY e.createdAt DESC")
    List<ErrorLog> findRecentErrors(@Param("userId") Integer userId, @Param("role") String role, Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM ErrorLog e WHERE e.createdAt >= :startDate AND e.applicationId IN " +
           "(SELECT a.id FROM Application a WHERE a.createdBy = :userId OR :role = 'ADMIN')")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, 
                         @Param("userId") Integer userId, 
                         @Param("role") String role);
}

