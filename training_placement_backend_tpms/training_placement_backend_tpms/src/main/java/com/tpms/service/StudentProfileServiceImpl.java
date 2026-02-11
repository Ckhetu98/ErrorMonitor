package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.StudentProfileDto;
import com.tpms.entity.StudentProfile;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.StudentProfileRepository;
import com.tpms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {
    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    
    private final String UPLOAD_DIR = "uploads/resumes/";

    @Override
    public ApiResponse<StudentProfileDto> getByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        
        StudentProfileDto dto = new StudentProfileDto();
        dto.setUserId(userId);
        
        // Set user details from signup
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        
        if (profile != null) {
            dto = modelMapper.map(profile, StudentProfileDto.class);
            dto.setUserId(userId);
            // Override with user details from signup
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            // Set gender from profile
            dto.setGender(profile.getGender() != null ? profile.getGender().toString() : null);
        }
        
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", dto);
    }

    @Override
    public ApiResponse<StudentProfileDto> update(Long userId, StudentProfileDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update user table fields if provided
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userRepository.save(user);
        
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = new StudentProfile();
            profile.setUser(user);
        }
        // map fields from dto to entity (skip id/userId)
        if (dto.getGender() != null) {
            profile.setGender(com.tpms.enums.Gender.valueOf(dto.getGender()));
        }
        profile.setEnrollmentNo(dto.getEnrollmentNo());
        profile.setDepartment(dto.getDepartment());
        profile.setPassingYear(dto.getPassingYear());
        profile.setDob(dto.getDob());
        profile.setAge(dto.getAge());
        profile.setAddressLine1(dto.getAddressLine1());
        profile.setAddressLine2(dto.getAddressLine2());
        profile.setCity(dto.getCity());
        profile.setState(dto.getState());
        profile.setPincode(dto.getPincode());
        profile.setNationality(dto.getNationality());
        profile.setProfilePhoto(dto.getProfilePhoto());
        profile.setLinkedinUrl(dto.getLinkedinUrl());
        profile.setGithubUrl(dto.getGithubUrl());
        profile.setPortfolioUrl(dto.getPortfolioUrl());
        profile.setEducationJson(dto.getEducationJson());
        profile.setExperienceJson(dto.getExperienceJson());
        profile.setSkillsJson(dto.getSkillsJson());
        profile.setProjectsJson(dto.getProjectsJson());
        profile.setResumeLinksJson(dto.getResumeLinksJson());
        profile.setCareerInterests(dto.getCareerInterests());
        profile.setPreferredJobLocation(dto.getPreferredJobLocation());
        profile.setPlacementStatus(dto.getPlacementStatus());
        profile.setProfileCompleted(dto.getProfileCompleted() != null ? dto.getProfileCompleted() : false);

        StudentProfile saved = profileRepository.save(profile);
        StudentProfileDto out = modelMapper.map(saved, StudentProfileDto.class);
        out.setUserId(saved.getUser().getId());
        return new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", out);
    }

    @Override
    public ApiResponse<Void> deleteByUserId(Long userId) {
        var p = profileRepository.findByUserId(userId);
        p.ifPresent(profileRepository::delete);
        return new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null);
    }
    
    @Override
    public ApiResponse<Map<String, String>> uploadResume(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        StudentProfile profile = profileRepository.findByUserId(userId).orElse(new StudentProfile());
        profile.setUser(user);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = userId + "_" + UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Update profile with resume info
        String resumeJson = "[{\"fileName\":\"" + uniqueFilename + "\",\"url\":\"/files/resume/" + uniqueFilename + "\"}]";
        profile.setResumeLinksJson(resumeJson);
        profileRepository.save(profile);
        
        Map<String, String> response = new HashMap<>();
        response.put("fileName", originalFilename);
        response.put("url", "/files/resume/" + uniqueFilename);
        
        return new ApiResponse<>(java.time.Instant.now(), "Resume uploaded successfully", "SUCCESS", response);
    }
    
    @Override
    public Map<String, Object> getResume(Long userId) throws Exception {
        StudentProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        
        String resumeJson = profile.getResumeLinksJson();
        if (resumeJson == null || resumeJson.isEmpty()) {
            throw new ResourceNotFoundException("No resume found");
        }
        
        // Extract file path from JSON (simple parsing)
        String filePath = resumeJson.substring(resumeJson.indexOf("url\":\"") + 6);
        filePath = filePath.substring(0, filePath.indexOf("\""));
        
        String fileName = resumeJson.substring(resumeJson.indexOf("fileName\":\"") + 11);
        fileName = fileName.substring(0, fileName.indexOf("\""));
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Resume file not found");
        }
        
        byte[] fileContent = Files.readAllBytes(path);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", fileContent);
        result.put("fileName", fileName);
        
        return result;
    }
}
