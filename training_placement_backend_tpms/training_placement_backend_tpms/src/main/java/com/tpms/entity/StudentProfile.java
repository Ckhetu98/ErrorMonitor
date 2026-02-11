package com.tpms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student_details")
@Data
public class StudentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    private com.tpms.enums.Gender gender;

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

    @Column(columnDefinition = "TEXT")
    private String educationJson;

    @Column(columnDefinition = "TEXT")
    private String experienceJson;

    @Column(columnDefinition = "TEXT")
    private String skillsJson;

    @Column(columnDefinition = "TEXT")
    private String projectsJson;

    @Column(columnDefinition = "TEXT")
    private String resumeLinksJson;

    @Column(columnDefinition = "TEXT")
    private String careerInterests;

    private String preferredJobLocation;
    private String placementStatus;

    private Boolean profileCompleted = false;
}
