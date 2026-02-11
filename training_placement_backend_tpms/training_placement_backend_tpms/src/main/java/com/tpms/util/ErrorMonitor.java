package com.tpms.util;

import com.tpms.service.ErrorMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ErrorMonitor {

    @Autowired(required = false)
    private ErrorMonitoringService errorMonitoringService;

    /**
     * Log error with default severity (High)
     */
    public void logError(Exception exception, String endpoint) {
        if (errorMonitoringService != null) {
            errorMonitoringService.logError(exception, endpoint);
        }
    }

    /**
     * Log error with custom severity
     */
    public void logError(Exception exception, String endpoint, String severity) {
        if (errorMonitoringService != null) {
            errorMonitoringService.logError(exception, endpoint, severity);
        }
    }

    /**
     * Log critical error
     */
    public void logCritical(Exception exception, String endpoint) {
        if (errorMonitoringService != null) {
            errorMonitoringService.logError(exception, endpoint, "Critical");
        }
    }

    /**
     * Log medium priority error
     */
    public void logMedium(Exception exception, String endpoint) {
        if (errorMonitoringService != null) {
            errorMonitoringService.logError(exception, endpoint, "Medium");
        }
    }
}