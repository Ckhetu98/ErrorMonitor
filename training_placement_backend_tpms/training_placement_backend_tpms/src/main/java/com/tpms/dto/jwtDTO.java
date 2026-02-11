package com.tpms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class jwtDTO {
    private Long userId;
    private String email;
    private String role;
}
