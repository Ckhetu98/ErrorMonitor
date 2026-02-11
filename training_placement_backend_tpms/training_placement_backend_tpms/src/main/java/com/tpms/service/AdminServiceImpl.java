package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.UserDto;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.*;
import com.tpms.entity.Training;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final TrainingRepository trainingRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<?> getDashboardStats() {
        try {
            long totalStudents = userRepository.countByRole(com.tpms.enums.Role.STUDENT);
            long totalRecruiters = userRepository.countByRole(com.tpms.enums.Role.RECRUITER);
            long totalJobs = jobRepository.count();
            long totalApplications = applicationRepository.count();
            
            // Count students with SELECTED status
            long studentsHired = 0;
            try {
                studentsHired = applicationRepository.findAll().stream()
                    .filter(app -> app.getStatus() == com.tpms.enums.PlacementStatus.SELECTED)
                    .count();
            } catch (Exception e) {
                studentsHired = 0;
            }
            
            var stats = java.util.Map.of(
                "totalStudents", totalStudents,
                "totalRecruiters", totalRecruiters,
                "totalJobs", totalJobs,
                "totalApplications", totalApplications,
                "studentsHired", studentsHired
            );
            
            return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", stats);
        } catch (Exception e) {
            // Return default values if there's an error
            var stats = java.util.Map.of(
                "totalStudents", 0L,
                "totalRecruiters", 0L,
                "totalJobs", 0L,
                "totalApplications", 0L,
                "studentsHired", 0L
            );
            return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", stats);
        }
    }

    @Override
    public ApiResponse<List<UserDto>> getAllStudents() {
        List<UserDto> list = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.tpms.enums.Role.STUDENT)
                .map(u -> modelMapper.map(u, UserDto.class))
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    @Override
    public ApiResponse<List<UserDto>> getAllRecruiters() {
        List<UserDto> list = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.tpms.enums.Role.RECRUITER)
                .map(u -> modelMapper.map(u, UserDto.class))
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    @Override
    public ApiResponse<Void> updateUserStatus(Long id, String status) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setStatus(com.tpms.enums.Status.valueOf(status.toUpperCase()));
        userRepository.save(u);
        return new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", null);
    }

    @Override
    public ApiResponse<Void> deleteUser(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(u);
        return new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null);
    }

    @Override
    public ApiResponse<List<Object>> getRecentActivities() {
        List<Object> activities = new java.util.ArrayList<>();
        
        // Get recent trainings
        var recentTrainings = trainingRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(10)
                .map(training -> java.util.Map.of(
                    "id", training.getId(),
                    "action", "Training Created: " + training.getTitle(),
                    "entityType", "TRAINING",
                    "createdAt", training.getCreatedAt(),
                    "userName", "Admin",
                    "details", "Status: " + training.getStatus() + " | Participants: " + training.getMaxParticipants()
                ))
                .collect(java.util.stream.Collectors.toList());
        
        activities.addAll(recentTrainings);
        
        // Sort all activities by creation date
        activities.sort((a1, a2) -> {
            java.time.Instant date1 = (java.time.Instant) ((java.util.Map<?, ?>) a1).get("createdAt");
            java.time.Instant date2 = (java.time.Instant) ((java.util.Map<?, ?>) a2).get("createdAt");
            return date2.compareTo(date1);
        });
        
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", activities);
    }
}
