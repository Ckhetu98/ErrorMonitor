package com.errormonitoring.service;

import com.errormonitoring.model.Application;
import com.errormonitoring.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    public List<Application> getApplications(Integer userId, String role) {
        try {
            System.out.println("ApplicationService.getApplications called with userId: " + userId + ", role: " + role);
            
            if ("ADMIN".equals(role)) {
                List<Application> allApps = applicationRepository.findAll();
                System.out.println("ADMIN role - returning " + allApps.size() + " applications");
                return allApps;
            } else {
                // DEVELOPER and other roles see only their own applications
                List<Application> userApps = applicationRepository.findByCreatedBy(userId);
                System.out.println("DEVELOPER role - returning " + userApps.size() + " applications for user " + userId);
                return userApps;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<Map<String, Object>> getAllApplicationsAsMap() {
        try {
            List<Application> apps = applicationRepository.findAll();
            System.out.println("Found " + apps.size() + " total applications in database");
            return convertToMap(apps);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<Map<String, Object>> getUserApplicationsAsMap(Integer userId) {
        try {
            List<Application> apps = applicationRepository.findByCreatedBy(userId);
            System.out.println("Found " + apps.size() + " applications for user " + userId);
            
            // Debug: Print application details
            for (Application app : apps) {
                System.out.println("App: " + app.getName() + ", createdBy: " + app.getCreatedBy());
            }
            
            return convertToMap(apps);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    private List<Map<String, Object>> convertToMap(List<Application> applications) {
        return applications.stream()
            .map(app -> {
                Map<String, Object> appMap = new java.util.HashMap<>();
                appMap.put("id", app.getId());
                appMap.put("name", app.getName());
                appMap.put("description", app.getDescription() != null ? app.getDescription() : "");
                appMap.put("technology", app.getTechnology() != null ? app.getTechnology() : "");
                appMap.put("version", app.getVersion() != null ? app.getVersion() : "");
                appMap.put("baseUrl", app.getBaseUrl() != null ? app.getBaseUrl() : "");
                appMap.put("healthCheckUrl", app.getHealthCheckUrl() != null ? app.getHealthCheckUrl() : "");
                appMap.put("apiKey", app.getApiKey());
                appMap.put("isActive", app.getIsActive());
                appMap.put("isPaused", app.getIsPaused() != null ? app.getIsPaused() : false);
                appMap.put("createdAt", app.getCreatedAt().toString());
                appMap.put("createdBy", app.getCreatedBy());
                return appMap;
            })
            .toList();
    }
    
    @Transactional
    public Application createApplication(com.errormonitoring.dto.ApplicationDTOs.CreateApplicationRequest request, Integer userId) {
        Application app = new Application();
        app.setName(request.getName());
        app.setDescription(request.getDescription());
        app.setTechnology(request.getTechnology());
        app.setVersion(request.getVersion());
        app.setBaseUrl(request.getBaseUrl());
        app.setHealthCheckUrl(request.getHealthCheckUrl());
        app.setApiKey(generateApiKey(request.getName()));
        app.setIsActive(true);
        app.setCreatedBy(userId != null ? userId : 1);
        return applicationRepository.save(app);
    }
    
    @Transactional
    public Application updateApplication(Integer id, com.errormonitoring.dto.ApplicationDTOs.UpdateApplicationRequest request) {
        var appOpt = applicationRepository.findById(id);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            if (request.getName() != null) app.setName(request.getName());
            if (request.getDescription() != null) app.setDescription(request.getDescription());
            if (request.getTechnology() != null) app.setTechnology(request.getTechnology());
            if (request.getVersion() != null) app.setVersion(request.getVersion());
            if (request.getBaseUrl() != null) app.setBaseUrl(request.getBaseUrl());
            if (request.getHealthCheckUrl() != null) app.setHealthCheckUrl(request.getHealthCheckUrl());
            if (request.getIsActive() != null) app.setIsActive(request.getIsActive());
            return applicationRepository.save(app);
        }
        throw new RuntimeException("Application not found");
    }
    
    @Transactional
    public Application pauseApplication(Integer id) {
        var appOpt = applicationRepository.findById(id);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            app.setIsPaused(true);
            return applicationRepository.save(app);
        }
        throw new RuntimeException("Application not found");
    }
    
    @Transactional
    public Application resumeApplication(Integer id) {
        var appOpt = applicationRepository.findById(id);
        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            app.setIsPaused(false);
            return applicationRepository.save(app);
        }
        throw new RuntimeException("Application not found");
    }
    
    @Transactional
    public boolean deleteApplication(Integer id) {
        var appOpt = applicationRepository.findById(id);
        if (appOpt.isEmpty()) {
            return false;
        }
        applicationRepository.deleteById(id);
        return true;
    }
    
    public List<Application> getAllApplications() {
        try {
            return applicationRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public List<Application> getApplicationsByUserId(Integer userId) {
        try {
            if (userId == null) {
                // Return all applications when userId is null (for VIEWER role)
                return applicationRepository.findAll();
            }
            return applicationRepository.findByCreatedBy(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    public java.util.Optional<Application> getApplicationById(Integer id) {
        return applicationRepository.findById(id);
    }
    
    private String generateApiKey(String applicationName) {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int randomNumber = 100000 + random.nextInt(900000);
        int currentYear = java.time.LocalDate.now().getYear();
        
        // Clean application name: remove spaces, special chars, convert to uppercase
        String cleanName = applicationName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanName.length() > 10) {
            cleanName = cleanName.substring(0, 10); // Limit to 10 characters
        }
        
        return String.format("%s-%06d-%d", cleanName, randomNumber, currentYear);
    }
}