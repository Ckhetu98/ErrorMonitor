package com.tpms.controller;

import com.tpms.exception.ApiException;
import com.tpms.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test-errors")
@CrossOrigin(origins = "*")
public class ErrorTestController {

    @GetMapping("/database-error")
    public ResponseEntity<?> testDatabaseError() {
        throw new RuntimeException("Database connection timeout - Unable to connect to MySQL server");
    }
    
    @GetMapping("/null-pointer")
    public ResponseEntity<?> testNullPointerError() {
        String nullString = null;
        return ResponseEntity.ok(nullString.length()); // This will throw NullPointerException
    }
    
    @GetMapping("/user-not-found/{id}")
    public ResponseEntity<?> testUserNotFound(@PathVariable Long id) {
        throw new ResourceNotFoundException("User not found with ID: " + id);
    }
    
    @PostMapping("/validation-error")
    public ResponseEntity<?> testValidationError(@RequestBody Map<String, Object> request) {
        if (!request.containsKey("email")) {
            throw new ApiException("Email is required for user registration");
        }
        if (!request.containsKey("password")) {
            throw new ApiException("Password cannot be empty");
        }
        return ResponseEntity.ok("Validation passed");
    }
    
    @GetMapping("/job-application-error")
    public ResponseEntity<?> testJobApplicationError() {
        throw new IllegalStateException("Job application deadline has passed - Cannot submit application");
    }
    
    @GetMapping("/file-upload-error")
    public ResponseEntity<?> testFileUploadError() {
        throw new RuntimeException("Resume upload failed - File size exceeds 10MB limit");
    }
    
    @GetMapping("/authentication-error")
    public ResponseEntity<?> testAuthError() {
        throw new SecurityException("JWT token has expired - Please login again");
    }
    
    @GetMapping("/training-enrollment-error")
    public ResponseEntity<?> testTrainingError() {
        throw new ApiException("Training enrollment failed - Maximum capacity reached");
    }
    
    @GetMapping("/company-registration-error")
    public ResponseEntity<?> testCompanyError() {
        throw new IllegalArgumentException("Company registration invalid - GST number format incorrect");
    }
    
    @GetMapping("/placement-report-error")
    public ResponseEntity<?> testReportError() {
        throw new RuntimeException("Placement report generation failed - Insufficient data available");
    }
    
    @GetMapping("/generate-all-errors")
    public ResponseEntity<?> generateAllErrors() {
        Map<String, String> results = new HashMap<>();
        
        try { testDatabaseError(); } catch (Exception e) { results.put("database", "Generated"); }
        try { testNullPointerError(); } catch (Exception e) { results.put("nullpointer", "Generated"); }
        try { testUserNotFound(999L); } catch (Exception e) { results.put("usernotfound", "Generated"); }
        try { testJobApplicationError(); } catch (Exception e) { results.put("jobapplication", "Generated"); }
        try { testFileUploadError(); } catch (Exception e) { results.put("fileupload", "Generated"); }
        try { testAuthError(); } catch (Exception e) { results.put("authentication", "Generated"); }
        try { testTrainingError(); } catch (Exception e) { results.put("training", "Generated"); }
        try { testCompanyError(); } catch (Exception e) { results.put("company", "Generated"); }
        try { testReportError(); } catch (Exception e) { results.put("report", "Generated"); }
        
        results.put("message", "All test errors generated successfully!");
        results.put("total", "9 errors created");
        
        return ResponseEntity.ok(results);
    }
}