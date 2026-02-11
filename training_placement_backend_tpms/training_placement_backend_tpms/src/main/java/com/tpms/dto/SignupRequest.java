package com.tpms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class SignupRequest {
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    @NotBlank(message = "Password required")
	@Pattern(regexp="((?=.*\\d)(?=.*[a-z])(?=.*[#@$*]).{5,20})",message = "Invalid password format")
    private String password;
    private String phone;
    @NotBlank private String role; // STUDENT | RECRUITER | ADMIN
}
