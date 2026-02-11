package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.UserDto;

import java.util.List;

public interface AdminService {
    ApiResponse<?> getDashboardStats();
    ApiResponse<List<UserDto>> getAllStudents();
    ApiResponse<List<UserDto>> getAllRecruiters();
    ApiResponse<Void> updateUserStatus(Long id, String status);
    ApiResponse<Void> deleteUser(Long id);
    ApiResponse<List<Object>> getRecentActivities();
}
