package com.tpms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "error.monitoring.enabled", havingValue = "true")
public class ErrorMonitoringService {

    @Value("${error.monitoring.api.url}")
    private String apiUrl;

    @Value("${error.monitoring.application.name}")
    private String applicationName;

    @Value("${error.monitoring.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void logError(Exception exception, String endpoint) {
        logError(exception, endpoint, "High");
    }

    public void logError(Exception exception, String endpoint, String severity) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("applicationName", applicationName);
            errorData.put("apiEndpoint", endpoint);
            errorData.put("errorMessage", exception.getMessage());
            errorData.put("stackTrace", getStackTrace(exception));
            errorData.put("severity", severity);
            errorData.put("errorType", exception.getClass().getSimpleName());
            errorData.put("errorSource", "Backend");
            errorData.put("httpMethod", "POST"); // Default

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("X-API-Key", apiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(errorData, headers);
            restTemplate.postForObject(apiUrl, request, String.class);
            
        } catch (Exception e) {
            // Silent fail - don't break application if monitoring fails
            System.err.println("Failed to log error to monitoring service: " + e.getMessage());
        }
    }

    private String getStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.toString()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}