package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.UserDto;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getById(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserDto dto = modelMapper.map(u, UserDto.class);
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> update(@PathVariable Long id, @RequestBody UserDto userDto) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setFullName(userDto.getFullName());
        u.setPhone(userDto.getPhone());
        userRepository.save(u);
        UserDto dto = modelMapper.map(u, UserDto.class);
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(u);
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null));
    }
}
