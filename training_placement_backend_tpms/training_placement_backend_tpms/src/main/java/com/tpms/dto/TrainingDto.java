package com.tpms.dto;

import lombok.Data;

@Data
public class TrainingDto {
    private Long id;
    private String title;
    private String description;
    private String startDate;
    private String endDate;
    private Integer maxParticipants;
    private Integer enrolledCount;
    private String status;
    private Long trainerId; // optional
}
