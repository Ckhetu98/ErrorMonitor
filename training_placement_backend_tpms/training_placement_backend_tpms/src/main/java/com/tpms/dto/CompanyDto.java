package com.tpms.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class CompanyDto {
    private Long id;
    private String name;
    private String website;
    private String address;
    private String description;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private Integer jobCount;
    private Integer studentsHired;
    private Instant createdAt;
    private Instant updatedAt;
}
