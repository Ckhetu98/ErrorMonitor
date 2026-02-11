package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.UserDto;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<UserDto> getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        UserDto dto = modelMapper.map(user, UserDto.class);
        return new ApiResponse<>(Instant.now(), "User retrieved successfully", "SUCCESS", dto);
    }

    @Override
    public ApiResponse<UserDto> updateProfile(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        
        User updated = userRepository.save(user);
        UserDto result = modelMapper.map(updated, UserDto.class);
        return new ApiResponse<>(Instant.now(), "User updated successfully", "SUCCESS", result);
    }

    @Override
    public ApiResponse<Void> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        return new ApiResponse<>(Instant.now(), "User deleted successfully", "SUCCESS", null);
    }
}