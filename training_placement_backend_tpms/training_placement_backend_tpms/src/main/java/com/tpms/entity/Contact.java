package com.tpms.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.tpms.enums.InquiryStatus;

@Entity
@Table(name = "contacts")
@Data
public class Contact extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String subject;

    @Column(length = 2000, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.NEW;
}