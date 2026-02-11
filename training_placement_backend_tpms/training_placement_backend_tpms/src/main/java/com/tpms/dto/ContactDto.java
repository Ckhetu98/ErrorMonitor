package com.tpms.dto;

import lombok.Data;
import com.tpms.enums.InquiryStatus;
import java.time.Instant;

@Data
public class ContactDto {
    private Long id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private InquiryStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}