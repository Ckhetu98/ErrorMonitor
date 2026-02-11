package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "companies")
@Data
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String website;

    private String address;

    @Column(length = 2000)
    private String description;

    // Recruiter who owns/created this company (Optional)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs;
}
