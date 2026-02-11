package com.tpms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tpms.dto.ApiResponse;
import com.tpms.dto.ApplicationDto;
import com.tpms.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import java.util.List;

// REST Controller for managing job applications
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    // Get all applications for a specific student
    @GetMapping("/student/{sid}")
    public ResponseEntity<ApiResponse<List<ApplicationDto>>> getByStudent(@PathVariable("sid") Long sid) {
        return ResponseEntity.ok(applicationService.getByStudent(sid));
    }

    // Get all applications for jobs posted by a specific recruiter
    @GetMapping("/recruiter/{rid}")
    public ResponseEntity<ApiResponse<List<ApplicationDto>>> getByRecruiter(@PathVariable("rid") Long rid) {
        return ResponseEntity.ok(applicationService.getByRecruiter(rid));
    }

    // Get applications for a recruiter filtered by status
    @GetMapping("/recruiter/{rid}/status/{status}")
    public ResponseEntity<ApiResponse<List<ApplicationDto>>> getByRecruiterAndStatus(@PathVariable Long rid, @PathVariable String status) {
        return ResponseEntity.ok(applicationService.getByRecruiterAndStatus(rid, status));
    }

    // Submit a new job application
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<?>> apply(@RequestParam Long studentId, @RequestParam Long jobId) {
        return ResponseEntity.status(201).body(applicationService.apply(studentId, jobId));
    }

    // Update application status (recruiter action)
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<ApiResponse<?>> updateStatus(@PathVariable Long id, @PathVariable String status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    // Delete/withdraw an application
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.delete(id));
    }
    
    // Health check endpoint
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Application API is working!");
    }
    
    // Send selection email to shortlisted candidate
    @PostMapping("/{id}/send-email")
    public ResponseEntity<ApiResponse<String>> sendEmail(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.sendSelectionEmail(id));
    }
}
