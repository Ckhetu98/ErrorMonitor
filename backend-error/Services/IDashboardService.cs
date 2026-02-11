namespace ErrorMonitoringAPI.Services
{
    public interface IDashboardService
    {
        Task<DashboardStatsDto> GetDashboardStatsAsync(int? userId = null, string userRole = null);
        Task<List<RecentErrorDto>> GetRecentErrorsAsync(int? userId = null, string userRole = null);
        Task<List<ApplicationHealthDto>> GetApplicationHealthAsync(int? userId = null, string userRole = null);
        Task<List<ActiveAlertDto>> GetActiveAlertsAsync(int? userId = null, string userRole = null);
    }

    public class DashboardStatsDto
    {
        public int TotalErrors { get; set; }
        public int CriticalErrors { get; set; }
        public int ResolvedErrors { get; set; }
        public int ActiveApplications { get; set; }
        public int ActiveUsers { get; set; }
        public int AvgResponseTime { get; set; }
        public double ErrorRate { get; set; }
        public int RecentErrors { get; set; }
    }

    public class RecentErrorDto
    {
        public int Id { get; set; }
        public string Application { get; set; } = string.Empty;
        public string Endpoint { get; set; } = string.Empty;
        public string Error { get; set; } = string.Empty;
        public string Severity { get; set; } = string.Empty;
        public string Timestamp { get; set; } = string.Empty;
        public string Status { get; set; } = string.Empty;
    }

    public class ApplicationHealthDto
    {
        public string AppName { get; set; } = string.Empty;
        public int ErrorCount { get; set; }
        public int CriticalCount { get; set; }
        public int RecentErrors { get; set; }
        public int HealthScore { get; set; }
    }

    public class ActiveAlertDto
    {
        public int Id { get; set; }
        public string Type { get; set; } = string.Empty;
        public string Message { get; set; } = string.Empty;
        public string Level { get; set; } = string.Empty;
        public string Timestamp { get; set; } = string.Empty;
        public string Application { get; set; } = string.Empty;
    }
}