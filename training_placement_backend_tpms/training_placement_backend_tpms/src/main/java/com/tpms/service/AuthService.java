package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.AuthResponse;
import com.tpms.dto.SigninRequest;
import com.tpms.dto.SignupRequest;

public interface AuthService {
    ApiResponse<?> signup(SignupRequest req);
    ApiResponse<AuthResponse> signin(SigninRequest req);
}
