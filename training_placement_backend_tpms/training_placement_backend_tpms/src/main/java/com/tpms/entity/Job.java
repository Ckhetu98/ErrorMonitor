package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "jobs")
@Data
public class Job extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    private String location;

    private String employmentType; // Full-time / Part-time / Internship

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.tpms.enums.JobStatus status = com.tpms.enums.JobStatus.OPEN;

    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt = Instant.now();
    
    // Additional fields persisted from frontend
    private String salary;

    @Column(length = 1000)
    private String skillsRequired;

    // Store application deadline as simple string (yyyy-MM-dd) from frontend date input
    private String deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
