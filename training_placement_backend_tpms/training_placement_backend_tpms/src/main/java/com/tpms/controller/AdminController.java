package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.ContactDto;
import com.tpms.dto.JobDto;
import com.tpms.dto.TrainingDto;
import com.tpms.dto.UserDto;
import com.tpms.entity.Job;
import com.tpms.entity.Training;
import com.tpms.enums.InquiryStatus;
import com.tpms.enums.JobStatus;
import com.tpms.repository.JobRepository;
import com.tpms.repository.TrainingRepository;
import com.tpms.service.AdminService;
import com.tpms.service.ContactService;

import org.modelmapper.ModelMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final ContactService contactService;
    private final JobRepository jobRepository;
    private final TrainingRepository trainingRepository;
    private final ModelMapper modelMapper;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @GetMapping("/recruiters")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllRecruiters() {
        return ResponseEntity.ok(adminService.getAllRecruiters());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, status));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deleteUser(id));
    }

    // Admin job management
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<?>> getAllJobs() {
        var list = jobRepository.findAll().stream().map(j -> {
            JobDto dto = modelMapper.map(j, JobDto.class);
            if (j.getCompany() != null) dto.setCompanyId(j.getCompany().getId());
            return dto;
        }).toList();
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list));
    }

    @PutMapping("/jobs/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateJobStatus(@PathVariable Long id, @RequestParam String status) {
        Job j = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        j.setStatus(JobStatus.valueOf(status.toUpperCase()));
        jobRepository.save(j);
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", null));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        Job j = jobRepository.findById(id).orElseThrow(() -> new RuntimeException("Job not found"));
        // Delete applications first to avoid foreign key constraint issues
        try {
            // Use native query to delete applications
            jobRepository.deleteApplicationsByJobId(id);
        } catch (Exception e) {
            // Continue if no applications exist
        }
        jobRepository.delete(j);
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null));
    }

    // Admin training management
    @GetMapping("/trainings")
    public ResponseEntity<ApiResponse<?>> getAllTrainings() {
        var list = trainingRepository.findAll().stream().map(t -> {
            TrainingDto dto = modelMapper.map(t, TrainingDto.class);
            if (t.getTrainer() != null) dto.setTrainerId(t.getTrainer().getId());
            return dto;
        }).toList();
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list));
    }

    @PostMapping("/trainings")
    public ResponseEntity<ApiResponse<TrainingDto>> createTraining(@RequestBody TrainingDto dto) {
        Training t = modelMapper.map(dto, Training.class);
        Training saved = trainingRepository.save(t);
        TrainingDto result = modelMapper.map(saved, TrainingDto.class);
        if (saved.getTrainer() != null) result.setTrainerId(saved.getTrainer().getId());
        return ResponseEntity.status(201).body(new ApiResponse<>(java.time.Instant.now(), "Created", "SUCCESS", result));
    }

    @PutMapping("/trainings/{id}")
    public ResponseEntity<ApiResponse<TrainingDto>> updateTraining(@PathVariable Long id, @RequestBody TrainingDto dto) {
        Training t = trainingRepository.findById(id).orElseThrow(() -> new RuntimeException("Training not found"));
        t.setTitle(dto.getTitle()); 
        t.setDescription(dto.getDescription()); 
        t.setStartDate(dto.getStartDate()); 
        t.setEndDate(dto.getEndDate());
        trainingRepository.save(t);
        TrainingDto result = modelMapper.map(t, TrainingDto.class);
        if (t.getTrainer() != null) result.setTrainerId(t.getTrainer().getId());
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", result));
    }

    @DeleteMapping("/trainings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTraining(@PathVariable Long id) {
        Training t = trainingRepository.findById(id).orElseThrow(() -> new RuntimeException("Training not found"));
        trainingRepository.delete(t);
        return ResponseEntity.ok(new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null));
    }
    


    @GetMapping("/activities/recent")
    public ResponseEntity<ApiResponse<List<Object>>> getRecentActivities() {
        return ResponseEntity.ok(adminService.getRecentActivities());
    }
    

    


    // Contact inquiries management
    @GetMapping("/inquiries")
    public ResponseEntity<ApiResponse<List<ContactDto>>> getAllInquiries() {
        return ResponseEntity.ok(contactService.getAll());
    }

    @GetMapping("/inquiries/status/{status}")
    public ResponseEntity<ApiResponse<List<ContactDto>>> getInquiriesByStatus(@PathVariable InquiryStatus status) {
        return ResponseEntity.ok(contactService.getByStatus(status));
    }

    @PutMapping("/inquiries/{id}/status")
    public ResponseEntity<ApiResponse<ContactDto>> updateInquiryStatus(@PathVariable Long id, @RequestParam InquiryStatus status) {
        return ResponseEntity.ok(contactService.updateStatus(id, status));
    }

    @DeleteMapping("/inquiries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.delete(id));
    }
}
