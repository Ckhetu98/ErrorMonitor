package com.tpms.repository;

import com.tpms.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentId(Long studentId);
    List<Application> findByJobCompanyId(Long companyId);
    List<Application> findByJobId(Long jobId);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.id = :jobId")
    int countByJobId(@Param("jobId") Long jobId);
    
    @Query("SELECT a FROM Application a WHERE a.job.company.owner.id = :recruiterId")
    List<Application> findByRecruiterId(@Param("recruiterId") Long recruiterId);
    
    @Modifying
    @Transactional
    void deleteByJobId(Long jobId);
}
