package com.tpms.repository;

import com.tpms.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(com.tpms.enums.JobStatus status);
    List<Job> findByCompanyId(Long companyId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Application a WHERE a.job.id = :jobId")
    void deleteApplicationsByJobId(@Param("jobId") Long jobId);
}
