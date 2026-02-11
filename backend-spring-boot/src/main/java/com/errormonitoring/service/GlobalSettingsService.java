package com.errormonitoring.service;

import org.springframework.stereotype.Service;

@Service
public class GlobalSettingsService {
    
    private boolean global2FAEnabled = false; // Default disabled
    
    public boolean isGlobal2FAEnabled() {
        return global2FAEnabled;
    }
    
    public void setGlobal2FAEnabled(boolean enabled) {
        this.global2FAEnabled = enabled;
    }
}