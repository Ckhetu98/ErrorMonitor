package com.tpms.dto;

import lombok.Data;

@Data
public class StudentProfileDto {
    private Long id;
    private Long userId;
    
    // User details from signup
    private String fullName;
    private String email;
    private String phone;
    private String gender;

    private String enrollmentNo;
    private String department;
    private String passingYear;
    private String dob;
    private String age;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String nationality;

    private String profilePhoto;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;

    private String educationJson;
    private String experienceJson;
    private String skillsJson;
    private String projectsJson;
    private String resumeLinksJson;

    private String careerInterests;
    private String preferredJobLocation;
    private String placementStatus;
    private Boolean profileCompleted;
}
