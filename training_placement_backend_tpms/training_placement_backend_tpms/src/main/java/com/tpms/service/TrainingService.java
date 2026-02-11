package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.TrainingDto;

import java.util.List;

public interface TrainingService {
    ApiResponse<List<TrainingDto>> getAll();
    ApiResponse<List<TrainingDto>> getByStudent(Long studentId); // assuming enrollments exist later
    ApiResponse<TrainingDto> create(TrainingDto dto);
    ApiResponse<TrainingDto> update(Long id, TrainingDto dto);
    ApiResponse<Void> delete(Long id);
    ApiResponse<Void> enroll(Long studentId, Long trainingId);
    ApiResponse<List<Object>> getEnrolledStudents(Long trainingId);
}
