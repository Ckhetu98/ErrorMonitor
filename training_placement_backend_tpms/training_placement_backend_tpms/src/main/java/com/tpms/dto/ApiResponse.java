package com.tpms.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private Instant timestamp = Instant.now();
    private String message;
    private String status; // SUCCESS | FAILURE
    private T data;
}
