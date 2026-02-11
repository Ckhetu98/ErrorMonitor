package com.errormonitoring.repository;

import com.errormonitoring.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Integer> {
    List<Alert> findByErrorLogId(String errorLogId);
    List<Alert> findByIsActiveTrue();
    List<Alert> findByIsResolvedFalse();
    List<Alert> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT a FROM Alert a WHERE a.createdBy = :userId OR :role = 'ADMIN'")
    List<Alert> findActiveAlertsByUser(@Param("userId") Integer userId, @Param("role") String role);
    
    @Query("SELECT a FROM Alert a WHERE a.applicationId IN " +
           "(SELECT app.id FROM Application app WHERE app.createdBy = :userId)")
    List<Alert> findAlertsForDeveloper(@Param("userId") Integer userId);
    
    @Query("SELECT a FROM Alert a WHERE a.applicationId IN " +
           "(SELECT app.id FROM Application app WHERE app.createdBy = 1)")
    List<Alert> findAlertsForAdminApplications();
}