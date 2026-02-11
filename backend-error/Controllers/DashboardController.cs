using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using ErrorMonitoringAPI.Services;
using System.Security.Claims;
using Microsoft.Data.SqlClient;

namespace ErrorMonitoringAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class DashboardController : ControllerBase
    {
        private readonly IDashboardService _dashboardService;
        private readonly string _connectionString;

        public DashboardController(IDashboardService dashboardService, IConfiguration configuration)
        {
            _dashboardService = dashboardService;
            _connectionString = configuration.GetConnectionString("DefaultConnection");
        }

        private int? GetCurrentUserId()
        {
            var userIdClaim = HttpContext.User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            return int.TryParse(userIdClaim, out int userId) ? userId : null;
        }

        private string? GetCurrentUserRole()
        {
            return HttpContext.User.FindFirst(ClaimTypes.Role)?.Value;
        }

        [HttpGet("stats")]
        public async Task<IActionResult> GetDashboardStats()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var stats = await _dashboardService.GetDashboardStatsAsync(userId, userRole);
                return Ok(stats);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch dashboard stats", error = ex.Message });
            }
        }

        [HttpGet("recent-errors")]
        public async Task<IActionResult> GetRecentErrors()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var recentErrors = await _dashboardService.GetRecentErrorsAsync(userId, userRole);
                return Ok(recentErrors);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch recent errors", error = ex.Message });
            }
        }

        [HttpGet("application-health")]
        public async Task<IActionResult> GetApplicationHealth()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var applicationHealth = await _dashboardService.GetApplicationHealthAsync(userId, userRole);
                return Ok(applicationHealth);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch application health", error = ex.Message });
            }
        }

        [HttpGet("active-alerts")]
        public async Task<IActionResult> GetActiveAlerts()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var activeAlerts = await _dashboardService.GetActiveAlertsAsync(userId, userRole);
                return Ok(activeAlerts);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to fetch active alerts", error = ex.Message });
            }
        }

        [HttpGet("trend-analysis")]
        public async Task<IActionResult> GetTrendAnalysis([FromQuery] string dateRange = "6m")
        {
            try
            {
                var currentUserId = GetCurrentUserId();
                var currentUserRole = GetCurrentUserRole();
                
                if (currentUserId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();

                // Get actual error data grouped by month
                var errorQuery = currentUserRole == "ADMIN" 
                    ? @"SELECT 
                        FORMAT(CreatedAt, 'yyyy-MM') as MonthKey,
                        FORMAT(CreatedAt, 'MMM yyyy') as MonthName,
                        COUNT(*) as ErrorCount,
                        SUM(CASE WHEN Severity = 'Critical' THEN 1 ELSE 0 END) as CriticalCount
                      FROM ErrorLogs 
                      WHERE CreatedAt >= DATEADD(month, -5, GETDATE())
                      GROUP BY FORMAT(CreatedAt, 'yyyy-MM'), FORMAT(CreatedAt, 'MMM yyyy')
                      ORDER BY MonthKey"
                    : @"SELECT 
                        FORMAT(e.CreatedAt, 'yyyy-MM') as MonthKey,
                        FORMAT(e.CreatedAt, 'MMM yyyy') as MonthName,
                        COUNT(*) as ErrorCount,
                        SUM(CASE WHEN e.Severity = 'Critical' THEN 1 ELSE 0 END) as CriticalCount
                      FROM ErrorLogs e 
                      INNER JOIN Applications a ON e.ApplicationId = a.Id 
                      WHERE a.CreatedBy = @UserId AND e.CreatedAt >= DATEADD(month, -5, GETDATE())
                      GROUP BY FORMAT(e.CreatedAt, 'yyyy-MM'), FORMAT(e.CreatedAt, 'MMM yyyy')
                      ORDER BY MonthKey";
                
                using var errorCommand = new SqlCommand(errorQuery, connection);
                if (currentUserRole != "ADMIN")
                {
                    errorCommand.Parameters.AddWithValue("@UserId", currentUserId.Value);
                }

                var monthlyData = new Dictionary<string, (string monthName, int errors, int critical)>();
                using var reader = await errorCommand.ExecuteReaderAsync();
                while (await reader.ReadAsync())
                {
                    var monthKey = reader.GetString(0);  // MonthKey
                    var monthName = reader.GetString(1); // MonthName
                    var errorCount = reader.GetInt32(2); // ErrorCount
                    var criticalCount = reader.GetInt32(3); // CriticalCount
                    monthlyData[monthKey] = (monthName, errorCount, criticalCount);
                }
                reader.Close();

                // Get total error count
                var totalCountQuery = currentUserRole == "ADMIN" 
                    ? "SELECT COUNT(*) FROM ErrorLogs"
                    : "SELECT COUNT(*) FROM ErrorLogs e INNER JOIN Applications a ON e.ApplicationId = a.Id WHERE a.CreatedBy = @UserId";
                
                using var totalCommand = new SqlCommand(totalCountQuery, connection);
                if (currentUserRole != "ADMIN")
                {
                    totalCommand.Parameters.AddWithValue("@UserId", currentUserId.Value);
                }
                var totalErrors = (int)await totalCommand.ExecuteScalarAsync();

                // Create 6-month trend data
                var trends = new List<object>();
                var currentDate = DateTime.Now.AddMonths(-5);
                
                for (int i = 0; i < 6; i++)
                {
                    var monthKey = currentDate.ToString("yyyy-MM");
                    var monthName = currentDate.ToString("MMM yyyy");
                    
                    if (monthlyData.ContainsKey(monthKey))
                    {
                        var data = monthlyData[monthKey];
                        trends.Add(new
                        {
                            date = monthKey,
                            month = data.monthName,
                            errors = data.errors,
                            critical = data.critical
                        });
                    }
                    else
                    {
                        trends.Add(new
                        {
                            date = monthKey,
                            month = monthName,
                            errors = 0,
                            critical = 0
                        });
                    }
                    currentDate = currentDate.AddMonths(1);
                }

                return Ok(new
                {
                    trends = trends,
                    summary = new
                    {
                        totalErrors = totalErrors,
                        trend = totalErrors > 5 ? "increasing" : "stable"
                    }
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error in GetTrendAnalysis: {ex.Message}");
                // Return empty data structure matching Spring Boot format
                var emptyMonths = new List<object>();
                var currentDate = DateTime.Now.AddMonths(-5);
                
                for (int i = 0; i < 6; i++)
                {
                    emptyMonths.Add(new
                    {
                        date = currentDate.ToString("yyyy-MM"),
                        month = currentDate.ToString("MMM yyyy"),
                        errors = 0,
                        critical = 0
                    });
                    currentDate = currentDate.AddMonths(1);
                }

                return Ok(new
                {
                    trends = emptyMonths,
                    summary = new { totalErrors = 0, trend = "stable" }
                });
            }
        }
    }
}
