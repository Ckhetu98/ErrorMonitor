package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Data
public class User extends BaseEntity {

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // hashed password (BCrypt)

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.tpms.enums.Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.tpms.enums.Status status = com.tpms.enums.Status.ACTIVE;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private StudentProfile studentProfile;
}
