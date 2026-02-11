package com.errormonitoring.repository;

import com.errormonitoring.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    Optional<Application> findByApiKey(String apiKey);
    Application findByName(String name);
    List<Application> findByCreatedBy(Integer createdBy);
    List<Application> findByIsActiveTrue();
    
    @Query("SELECT a FROM Application a WHERE a.createdBy = :userId OR :role = 'ADMIN'")
    List<Application> findByUserOrAdmin(@Param("userId") Integer userId, @Param("role") String role);
}

