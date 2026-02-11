package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "applications")
@Data
public class Application extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.tpms.enums.PlacementStatus status = com.tpms.enums.PlacementStatus.APPLIED;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt = Instant.now();
}
