using Microsoft.EntityFrameworkCore;
using ErrorMonitoringAPI.Data;

namespace ErrorMonitoringAPI.Services
{
    public class DashboardService : IDashboardService
    {
        private readonly ApplicationDbContext _context;

        public DashboardService(ApplicationDbContext context)
        {
            _context = context;
        }

        public async Task<DashboardStatsDto> GetDashboardStatsAsync(int? userId = null, string userRole = null)
        {
            try
            {
                var errorQuery = _context.ErrorLogs.AsQueryable();
                var appQuery = _context.Applications.AsQueryable();
                
                // Role-based filtering - EXACT match with Spring Boot
                if (userRole == "ADMIN")
                {
                    // Admin sees ALL data
                }
                else if (userRole == "DEVELOPER" && userId.HasValue)
                {
                    // Developer sees ONLY data for their applications
                    var userAppIds = await _context.Applications
                        .Where(a => a.CreatedBy == userId.Value)
                        .Select(a => a.Id)
                        .ToListAsync();
                    
                    errorQuery = errorQuery.Where(e => userAppIds.Contains(e.ApplicationId));
                    appQuery = appQuery.Where(a => a.CreatedBy == userId.Value);
                }
                else if (userRole == "VIEWER")
                {
                    // VIEWER sees all data but read-only
                }

                var totalErrors = await errorQuery.CountAsync();
                var criticalErrors = await errorQuery.CountAsync(e => e.Severity == "Critical");
                var resolvedErrors = await errorQuery.CountAsync(e => e.Status == "Resolved");
                var activeApplications = await appQuery.CountAsync(a => a.IsActive);
                var recentErrors = await errorQuery
                    .CountAsync(e => e.CreatedAt >= DateTime.UtcNow.AddHours(-24));

                var activeUsers = 1; // Default to 1
                var avgResponseTime = 85; // Default response time
                var errorRate = totalErrors > 0 
                    ? Math.Round((double)(totalErrors - resolvedErrors) / totalErrors * 100, 2) 
                    : 0.0;

                Console.WriteLine($"Error stats ({userRole}) - Total: {totalErrors}, Critical: {criticalErrors}, Resolved: {resolvedErrors}");

                return new DashboardStatsDto
                {
                    TotalErrors = totalErrors,
                    CriticalErrors = criticalErrors,
                    ResolvedErrors = resolvedErrors,
                    ActiveApplications = activeApplications,
                    ActiveUsers = activeUsers,
                    AvgResponseTime = avgResponseTime,
                    ErrorRate = errorRate,
                    RecentErrors = recentErrors
                };
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching dashboard stats: {ex.Message}");
                return new DashboardStatsDto
                {
                    TotalErrors = 0,
                    CriticalErrors = 0,
                    ResolvedErrors = 0,
                    ActiveApplications = 0,
                    ActiveUsers = 1,
                    AvgResponseTime = 85,
                    ErrorRate = 0.0,
                    RecentErrors = 0
                };
            }
        }

        public async Task<List<RecentErrorDto>> GetRecentErrorsAsync(int? userId = null, string userRole = null)
        {
            try
            {
                var query = _context.ErrorLogs
                    .Include(e => e.Application)
                    .AsQueryable();
                
                // Role-based filtering - EXACT match with Spring Boot
                if (userRole == "ADMIN")
                {
                    // Admin sees ALL recent errors
                }
                else if (userRole == "DEVELOPER" && userId.HasValue)
                {
                    // Developer sees ONLY errors from their applications
                    var userAppIds = await _context.Applications
                        .Where(a => a.CreatedBy == userId.Value)
                        .Select(a => a.Id)
                        .ToListAsync();
                    query = query.Where(e => userAppIds.Contains(e.ApplicationId));
                }
                else if (userRole == "VIEWER")
                {
                    // VIEWER sees read-only recent errors
                }

                var recentErrors = await query
                    .OrderByDescending(e => e.CreatedAt) // Most recent first
                    .Take(10)
                    .ToListAsync();

                Console.WriteLine($"Recent errors for {userRole} ({userId}): {recentErrors.Count} errors");

                return recentErrors.Select(e => new RecentErrorDto
                {
                    Id = e.Id,
                    Application = e.Application?.Name ?? "Unknown App",
                    Endpoint = e.Source ?? "",
                    Error = e.Message ?? "No message",
                    Severity = e.Severity,
                    Timestamp = e.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                    Status = e.Status ?? "Open"
                }).ToList();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error fetching recent errors: {ex.Message}");
                return new List<RecentErrorDto>();
            }
        }

        public async Task<List<ApplicationHealthDto>> GetApplicationHealthAsync(int? userId = null, string userRole = null)
        {
            try
            {
                var query = _context.Applications
                    .Where(a => a.IsActive)
                    .AsQueryable();
                
                // Role-based filtering
                if (userRole == "DEVELOPER" && userId.HasValue)
                {
                    query = query.Where(a => a.CreatedBy == userId.Value);
                }

                var applications = await query.ToListAsync();
                var result = new List<ApplicationHealthDto>();
                
                foreach (var app in applications)
                {
                    var errorCount = await _context.ErrorLogs.CountAsync(e => e.ApplicationId == app.Id);
                    var criticalCount = await _context.ErrorLogs.CountAsync(e => e.ApplicationId == app.Id && e.Severity == "Critical");
                    var recentErrors = await _context.ErrorLogs.CountAsync(e => e.ApplicationId == app.Id && e.CreatedAt >= DateTime.UtcNow.AddDays(-7));
                    
                    var healthScore = Math.Max(0, 100 - (errorCount * 2) - (criticalCount * 10) - (recentErrors * 3));
                    
                    result.Add(new ApplicationHealthDto
                    {
                        AppName = app.Name,
                        ErrorCount = errorCount,
                        CriticalCount = criticalCount,
                        RecentErrors = recentErrors,
                        HealthScore = Math.Min(100, healthScore)
                    });
                }

                return result.OrderByDescending(a => a.ErrorCount).ToList();
            }
            catch
            {
                return new List<ApplicationHealthDto>();
            }
        }

        public async Task<List<ActiveAlertDto>> GetActiveAlertsAsync(int? userId = null, string userRole = null)
        {
            try
            {
                var query = _context.Alerts
                    .Include(a => a.Application)
                    .Where(a => a.IsActive)
                    .AsQueryable();
                
                // Role-based filtering
                if (userRole == "DEVELOPER" && userId.HasValue)
                {
                    query = query.Where(a => a.Application.CreatedBy == userId.Value);
                }

                var activeAlerts = await query
                    .OrderByDescending(a => a.CreatedAt)
                    .ToListAsync();

                var result = new List<ActiveAlertDto>();
                
                foreach (var alert in activeAlerts)
                {
                    result.Add(new ActiveAlertDto
                    {
                        Id = alert.Id,
                        Type = "EMAIL",
                        Message = alert.Description ?? "",
                        Level = alert.Name.Contains("Critical") ? "CRITICAL" : "HIGH",
                        Timestamp = alert.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                        Application = alert.Application?.Name ?? "Unknown"
                    });
                }

                return result;
            }
            catch
            {
                return new List<ActiveAlertDto>();
            }
        }
    }
}