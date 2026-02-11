package com.tpms.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class RecruiterProfileDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String companyName;
    private String designation;
    private String officialEmail;
    private String officialPhone;
    private String companyWebsite;
    private String companyAddress;
    private String companyDescription;
    private Instant createdAt;
    private Instant updatedAt;
}