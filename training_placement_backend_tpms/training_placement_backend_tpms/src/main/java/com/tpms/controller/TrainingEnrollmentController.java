package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.repository.TrainingEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingEnrollmentController {
    
    private final TrainingEnrollmentRepository enrollmentRepository;
    
    @GetMapping("/{id}/enrollments")
    public ResponseEntity<ApiResponse<?>> getEnrollments(@PathVariable Long id) {
        try {
            var enrollments = enrollmentRepository.findByTrainingId(id).stream()
                .map(e -> Map.of(
                    "studentName", e.getStudent().getFullName(),
                    "studentEmail", e.getStudent().getEmail(),
                    "department", e.getStudent().getStudentProfile() != null ? 
                        e.getStudent().getStudentProfile().getDepartment() : "N/A",
                    "enrolledAt", e.getEnrolledAt().toString()
                ))
                .toList();
            return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", enrollments));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", java.util.List.of()));
        }
    }
}