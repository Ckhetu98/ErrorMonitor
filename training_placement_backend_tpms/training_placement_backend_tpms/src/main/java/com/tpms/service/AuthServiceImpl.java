package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.AuthResponse;
import com.tpms.dto.SigninRequest;
import com.tpms.dto.SignupRequest;
import com.tpms.dto.UserDto;
import com.tpms.entity.User;
import com.tpms.exception.ApiException;
import com.tpms.exception.ResourceAlreadyExistsException;
import com.tpms.repository.UserRepository;
import com.tpms.security.JwtUtils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;

    @Override
    public ApiResponse<?> signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }
        User u = modelMapper.map(req, User.class);
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(com.tpms.enums.Role.valueOf(req.getRole().toUpperCase()));
        userRepository.save(u);
        UserDto dto = modelMapper.map(u, UserDto.class);
        return new ApiResponse<>(java.time.Instant.now(), "Account created", "SUCCESS", dto);
    }

    @Override
    public ApiResponse<AuthResponse> signin(SigninRequest req) {
        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ApiException("Invalid credentials"));
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword()))
            throw new ApiException("Invalid credentials");
        String token = jwtUtils.generateToken(u);
        UserDto dto = modelMapper.map(u, UserDto.class);
        AuthResponse auth = new AuthResponse(token, dto);
        return new ApiResponse<>(java.time.Instant.now(), "Login successful", "SUCCESS", auth);
    }
}
