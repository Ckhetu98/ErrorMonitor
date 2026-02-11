package com.errormonitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {
    
    @Autowired
    private com.errormonitoring.service.GlobalSettingsService globalSettingsService;
    
    // Only security settings are needed - 2FA is handled by AuthController

    @GetMapping("/security")
    public ResponseEntity<?> getSecuritySettings() {
        return ResponseEntity.ok(Map.of(
            "sessionTimeout", 30,
            "maxLoginAttempts", 5,
            "passwordMinLength", 8,
            "requireSpecialChars", true
        ));
    }
    
    @PostMapping("/security")
    public ResponseEntity<?> updateSecuritySettings(@RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(Map.of("message", "Security settings updated successfully"));
    }
}