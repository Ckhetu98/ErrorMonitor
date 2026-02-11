package com.tpms.dto;

import lombok.Data;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class SigninRequest {
    @Email @NotBlank private String email;
    @NotBlank private String password;
}
