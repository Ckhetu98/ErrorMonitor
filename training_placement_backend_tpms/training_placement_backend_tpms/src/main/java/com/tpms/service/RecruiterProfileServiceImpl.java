package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.RecruiterProfileDto;
import com.tpms.entity.Company;
import com.tpms.entity.RecruiterProfile;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.CompanyRepository;
import com.tpms.repository.RecruiterProfileRepository;
import com.tpms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RecruiterProfileServiceImpl implements RecruiterProfileService {
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<RecruiterProfileDto> getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        RecruiterProfile profile = recruiterProfileRepository.findByUserId(userId)
                .orElse(null);
        
        RecruiterProfileDto dto = new RecruiterProfileDto();
        dto.setUserId(userId);
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        
        if (profile != null) {
            dto.setId(profile.getId());
            dto.setCompanyName(profile.getCompanyName());
            dto.setDesignation(profile.getDesignation());
            dto.setOfficialEmail(profile.getOfficialEmail());
            dto.setOfficialPhone(profile.getOfficialPhone());
            dto.setCompanyWebsite(profile.getCompanyWebsite());
            dto.setCompanyAddress(profile.getCompanyAddress());
            dto.setCompanyDescription(profile.getCompanyDescription());
            dto.setCreatedAt(profile.getCreatedAt());
            dto.setUpdatedAt(profile.getUpdatedAt());
        }
        
        return new ApiResponse<>(Instant.now(), "Profile retrieved successfully", "SUCCESS", dto);
    }

    @Override
    @Transactional
    public ApiResponse<RecruiterProfileDto> createOrUpdateProfile(Long userId, RecruiterProfileDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        RecruiterProfile profile = recruiterProfileRepository.findByUserId(userId)
                .orElse(new RecruiterProfile());
        
        if (profile.getId() == null) {
            profile.setUser(user);
        }
        
        profile.setCompanyName(dto.getCompanyName());
        profile.setDesignation(dto.getDesignation());
        profile.setOfficialEmail(dto.getOfficialEmail());
        profile.setOfficialPhone(dto.getOfficialPhone());
        profile.setCompanyWebsite(dto.getCompanyWebsite());
        profile.setCompanyAddress(dto.getCompanyAddress());
        profile.setCompanyDescription(dto.getCompanyDescription());
        
        RecruiterProfile saved = recruiterProfileRepository.save(profile);
        
        // Create or update company
        Company company = companyRepository.findAll().stream()
                .filter(c -> c.getOwner() != null && c.getOwner().getId().equals(userId))
                .findFirst()
                .orElse(null);
        
        if (company == null) {
            company = new Company();
            company.setOwner(user);
        }
        
        company.setName(dto.getCompanyName());
        company.setWebsite(dto.getCompanyWebsite());
        company.setAddress(dto.getCompanyAddress());
        company.setDescription(dto.getCompanyDescription());
        
        companyRepository.save(company);
        
        RecruiterProfileDto result = new RecruiterProfileDto();
        result.setId(saved.getId());
        result.setUserId(userId);
        result.setFullName(user.getFullName());
        result.setEmail(user.getEmail());
        result.setPhone(user.getPhone());
        result.setCompanyName(saved.getCompanyName());
        result.setDesignation(saved.getDesignation());
        result.setOfficialEmail(saved.getOfficialEmail());
        result.setOfficialPhone(saved.getOfficialPhone());
        result.setCompanyWebsite(saved.getCompanyWebsite());
        result.setCompanyAddress(saved.getCompanyAddress());
        result.setCompanyDescription(saved.getCompanyDescription());
        result.setCreatedAt(saved.getCreatedAt());
        result.setUpdatedAt(saved.getUpdatedAt());
        
        return new ApiResponse<>(Instant.now(), "Profile updated successfully", "SUCCESS", result);
    }

    @Override
    public ApiResponse<Boolean> hasProfile(Long userId) {
        boolean exists = recruiterProfileRepository.existsByUserId(userId);
        return new ApiResponse<>(Instant.now(), "Profile check completed", "SUCCESS", exists);
    }
}