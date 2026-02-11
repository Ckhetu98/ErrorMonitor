import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

class ErrorMonitor {
  constructor(applicationName = 'Frontend-App') {
    this.applicationName = applicationName
    this.setupGlobalErrorHandlers()
  }

  setupGlobalErrorHandlers() {
    // Catch unhandled JavaScript errors
    window.addEventListener('error', (event) => {
      this.logError({
        errorMessage: event.message,
        stackTrace: event.error?.stack,
        apiEndpoint: window.location.pathname,
        severity: 'High',
        errorType: 'JavaScript Error',
        httpMethod: 'GET',
        userAgent: navigator.userAgent,
        ipAddress: 'Unknown'
      })
    })

    // Catch unhandled promise rejections
    window.addEventListener('unhandledrejection', (event) => {
      this.logError({
        errorMessage: event.reason?.message || 'Unhandled Promise Rejection',
        stackTrace: event.reason?.stack,
        apiEndpoint: window.location.pathname,
        severity: 'High',
        errorType: 'Promise Rejection',
        httpMethod: 'GET',
        userAgent: navigator.userAgent,
        ipAddress: 'Unknown'
      })
    })
  }

  async logError(errorData) {
    try {
      await axios.post(`${API_BASE_URL}/errorlogs/log`, {
        applicationName: errorData.applicationName || this.applicationName,
        ...errorData
      })
    } catch (error) {
      console.error('Failed to log error to monitoring system:', error)
    }
  }

  // Manual error logging with application context
  logManualError(message, severity = 'Medium', endpoint = null, appName = null) {
    this.logError({
      errorMessage: message,
      apiEndpoint: endpoint || window.location.pathname,
      severity,
      errorType: 'Manual Error',
      httpMethod: 'GET',
      userAgent: navigator.userAgent,
      ipAddress: 'Unknown',
      applicationName: appName || this.applicationName
    })
  }

  // Log API errors with application context
  logApiError(error, endpoint, method = 'GET', appName = null) {
    const severity = error.response?.status >= 500 ? 'Critical' : 'High'
    this.logError({
      errorMessage: error.response?.data?.message || error.message,
      stackTrace: error.stack,
      apiEndpoint: endpoint,
      severity,
      errorType: 'API Error',
      httpMethod: method,
      userAgent: navigator.userAgent,
      ipAddress: 'Unknown',
      applicationName: appName || this.applicationName
    })
  }
}

export const errorMonitor = new ErrorMonitor()
export default ErrorMonitor