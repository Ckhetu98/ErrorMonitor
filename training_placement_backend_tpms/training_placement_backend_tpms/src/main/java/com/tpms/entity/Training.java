package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "trainings")
@Data
public class Training extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private User trainer;
    
    @Column(name = "max_participants")
    private Integer maxParticipants = 0;
    
    @Column(name = "enrolled_count")
    private Integer enrolledCount = 0;
    
    private String status = "ACTIVE";
}
