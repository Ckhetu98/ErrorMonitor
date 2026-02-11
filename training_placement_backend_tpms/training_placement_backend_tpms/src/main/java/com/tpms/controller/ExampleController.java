package com.tpms.controller;

import com.tpms.util.ErrorMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/example")
public class ExampleController {

    @Autowired
    private ErrorMonitor errorMonitor;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        try {
            // Your business logic here
            if (Math.random() > 0.5) {
                throw new RuntimeException("Random error for testing");
            }
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            // Manual error logging (optional - global handler will catch it anyway)
            errorMonitor.logError(e, "/api/example/test");
            return ResponseEntity.internalServerError().body("Error occurred");
        }
    }

    @PostMapping("/critical-operation")
    public ResponseEntity<String> criticalOperation() {
        try {
            // Critical business logic
            return ResponseEntity.ok("Operation completed");
        } catch (Exception e) {
            // Log as critical error
            errorMonitor.logCritical(e, "/api/example/critical-operation");
            throw e; // Re-throw to let global handler also catch it
        }
    }
}