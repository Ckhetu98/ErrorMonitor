package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.RecruiterProfileDto;
import com.tpms.service.RecruiterProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter/profile")
@RequiredArgsConstructor
public class RecruiterProfileController {
    private final RecruiterProfileService recruiterProfileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<RecruiterProfileDto>> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(recruiterProfileService.getProfile(userId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<RecruiterProfileDto>> createOrUpdateProfile(
            @PathVariable Long userId, 
            @RequestBody RecruiterProfileDto dto) {
        return ResponseEntity.ok(recruiterProfileService.createOrUpdateProfile(userId, dto));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<RecruiterProfileDto>> updateProfile(
            @PathVariable Long userId, 
            @RequestBody RecruiterProfileDto dto) {
        return ResponseEntity.ok(recruiterProfileService.createOrUpdateProfile(userId, dto));
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(recruiterProfileService.hasProfile(userId));
    }
}