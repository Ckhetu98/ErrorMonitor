package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.RecruiterProfileDto;

public interface RecruiterProfileService {
    ApiResponse<RecruiterProfileDto> getProfile(Long userId);
    ApiResponse<RecruiterProfileDto> createOrUpdateProfile(Long userId, RecruiterProfileDto dto);
    ApiResponse<Boolean> hasProfile(Long userId);
}