package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "recruiter_profiles")
@Data
public class RecruiterProfile extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String designation;

    @Column(name = "official_email")
    private String officialEmail;

    @Column(name = "official_phone")
    private String officialPhone;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_description", length = 2000)
    private String companyDescription;
}