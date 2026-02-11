package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.ApplicationDto;

import java.util.List;

public interface ApplicationService {
    ApiResponse<List<ApplicationDto>> getByStudent(Long studentId);
    ApiResponse<List<ApplicationDto>> getByRecruiter(Long recruiterId);
    ApiResponse<List<ApplicationDto>> getByRecruiterAndStatus(Long recruiterId, String status);
    ApiResponse<ApplicationDto> apply(Long studentId, Long jobId);
    ApiResponse<ApplicationDto> updateStatus(Long id, String status);
    ApiResponse<Void> delete(Long id);
    ApiResponse<String> sendSelectionEmail(Long id);
}
