package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.AuthResponse;
import com.tpms.dto.SigninRequest;
import com.tpms.dto.SignupRequest;
import com.tpms.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@Validated @RequestBody SignupRequest req) {
        ApiResponse<?> resp = authService.signup(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> signin(@Validated @RequestBody SigninRequest req) {
        ApiResponse<AuthResponse> resp = authService.signin(req);
        return ResponseEntity.ok(resp);
    }


}
