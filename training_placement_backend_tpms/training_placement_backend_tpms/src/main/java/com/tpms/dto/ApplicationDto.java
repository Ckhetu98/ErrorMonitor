package com.tpms.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ApplicationDto {
    private Long id;
    private Long studentId;
    private Long jobId;
    private String status; // APPLIED, SHORTLISTED, REJECTED, HIRED
    private Instant appliedAt;
    
    // Student details
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    
    // Job details
    private String jobTitle;
    private String companyName;
    private String jobLocation;
    private String jobSalary;
    
    // Resume details
    private String resumeUrl;
    private String resumeFileName;
}
