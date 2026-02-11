package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.JobDto;

import java.util.List;

public interface JobService {
    ApiResponse<List<JobDto>> getAll();
    ApiResponse<JobDto> create(JobDto jobDto);
    ApiResponse<JobDto> update(Long id, JobDto jobDto);
    ApiResponse<Void> delete(Long id);
}
