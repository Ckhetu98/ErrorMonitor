package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.UserDto;

public interface UserService {
    ApiResponse<UserDto> getById(Long id);
    ApiResponse<UserDto> updateProfile(Long id, UserDto dto);
    ApiResponse<Void> deleteUser(Long id);
}