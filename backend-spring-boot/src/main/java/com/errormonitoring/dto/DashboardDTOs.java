package com.errormonitoring.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class DashboardDTOs {
    
    @Data
    public static class DashboardStatsResponse {
        private Long totalApplications;
        private Long totalErrors;
        private Long totalAlerts;
        private Long criticalErrors;
        private Long highErrors;
        private Long mediumErrors;
        private Long lowErrors;
        private Map<String, Long> errorsByDay;
        private List<ErrorLogDTOs.ErrorLogResponse> recentErrors;
        private List<ApplicationHealthResponse> applicationHealth;
        private List<AlertDTOs.AlertResponse> activeAlerts;
    }
    
    @Data
    public static class ApplicationHealthResponse {
        private Integer id;
        private String name;
        private Long totalErrors;
        private Long criticalErrors;
        private String healthScore;
        private Boolean isActive;
    }
}

