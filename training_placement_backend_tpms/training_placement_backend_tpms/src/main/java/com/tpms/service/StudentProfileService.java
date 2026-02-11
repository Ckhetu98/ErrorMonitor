package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.StudentProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface StudentProfileService {
    ApiResponse<StudentProfileDto> getByUserId(Long userId);
    ApiResponse<StudentProfileDto> update(Long userId, StudentProfileDto dto);
    ApiResponse<Void> deleteByUserId(Long userId);
    ApiResponse<Map<String, String>> uploadResume(Long userId, MultipartFile file) throws IOException;
    Map<String, Object> getResume(Long userId) throws Exception;
}
