package com.tpms.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class JobDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String employmentType; // Full-time, Part-time, Internship
    private String status; // OPEN, CLOSED
    private Instant postedAt;
    private Long postedBy;
    private Long companyId;
    
    // Additional frontend fields
    private String salary;
    private String skillsRequired;
    private String deadline; // stored as yyyy-MM-dd string
    
    // Company and application details
    private String companyName;
    private Integer applicationCount;
}
