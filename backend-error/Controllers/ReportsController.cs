using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.Data.SqlClient;
using System.Security.Claims;

namespace ErrorMonitoringAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class ReportsController : ControllerBase
    {
        private readonly string _connectionString;

        public ReportsController(IConfiguration configuration)
        {
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

        [HttpGet("error-summary")]
        public async Task<IActionResult> GetErrorSummary([FromQuery] string dateRange = "7d")
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

                var days = dateRange switch
                {
                    "1d" => 1,
                    "7d" => 7,
                    "30d" => 30,
                    "90d" => 90,
                    _ => 7
                };

                string query;
                SqlCommand command;

                if (currentUserRole == "ADMIN")
                {
                    query = $@"
                        SELECT 
                            COUNT(*) as TotalErrors,
                            SUM(CASE WHEN Severity = 'Critical' THEN 1 ELSE 0 END) as CriticalErrors,
                            SUM(CASE WHEN Severity = 'High' THEN 1 ELSE 0 END) as HighErrors,
                            SUM(CASE WHEN Severity = 'Medium' THEN 1 ELSE 0 END) as MediumErrors,
                            SUM(CASE WHEN Severity = 'Low' THEN 1 ELSE 0 END) as LowErrors,
                            SUM(CASE WHEN Status = 'Resolved' THEN 1 ELSE 0 END) as ResolvedErrors,
                            COUNT(DISTINCT AppId) as AffectedApplications
                        FROM ErrorLogs 
                        WHERE CreatedAt >= DATEADD(day, -{days}, GETDATE())";
                    command = new SqlCommand(query, connection);
                }
                else
                {
                    query = $@"
                        SELECT 
                            COUNT(*) as TotalErrors,
                            SUM(CASE WHEN e.Severity = 'Critical' THEN 1 ELSE 0 END) as CriticalErrors,
                            SUM(CASE WHEN e.Severity = 'High' THEN 1 ELSE 0 END) as HighErrors,
                            SUM(CASE WHEN e.Severity = 'Medium' THEN 1 ELSE 0 END) as MediumErrors,
                            SUM(CASE WHEN e.Severity = 'Low' THEN 1 ELSE 0 END) as LowErrors,
                            SUM(CASE WHEN e.Status = 'Resolved' THEN 1 ELSE 0 END) as ResolvedErrors,
                            COUNT(DISTINCT e.AppId) as AffectedApplications
                        FROM ErrorLogs e
                        INNER JOIN Applications a ON e.AppId = a.Id
                        WHERE e.CreatedAt >= DATEADD(day, -{days}, GETDATE())
                        AND a.CreatedBy = @UserId";
                    command = new SqlCommand(query, connection);
                    command.Parameters.AddWithValue("@UserId", currentUserId.Value);
                }

                using var reader = await command.ExecuteReaderAsync();

                if (await reader.ReadAsync())
                {
                    return Ok(new
                    {
                        totalErrors = reader.GetInt32(0),
                        criticalErrors = reader.GetInt32(1),
                        highErrors = reader.GetInt32(2),
                        mediumErrors = reader.GetInt32(3),
                        lowErrors = reader.GetInt32(4),
                        resolvedErrors = reader.GetInt32(5),
                        affectedApplications = reader.GetInt32(6),
                        dateRange = dateRange
                    });
                }

                return Ok(new
                {
                    totalErrors = 0,
                    criticalErrors = 0,
                    highErrors = 0,
                    mediumErrors = 0,
                    lowErrors = 0,
                    resolvedErrors = 0,
                    affectedApplications = 0,
                    dateRange = dateRange
                });
            }
            catch
            {
                return Ok(new
                {
                    totalErrors = 0,
                    criticalErrors = 0,
                    highErrors = 0,
                    mediumErrors = 0,
                    lowErrors = 0,
                    resolvedErrors = 0,
                    affectedApplications = 0,
                    dateRange = dateRange
                });
            }
        }

        [HttpGet("application-performance")]
        public async Task<IActionResult> GetApplicationPerformance([FromQuery] string dateRange = "7d")
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

                var days = dateRange switch
                {
                    "1d" => 1,
                    "7d" => 7,
                    "30d" => 30,
                    "90d" => 90,
                    _ => 7
                };

                string query;
                SqlCommand command;

                if (currentUserRole == "ADMIN")
                {
                    query = $@"
                        SELECT 
                            a.AppName,
                            COUNT(e.Id) as ErrorCount,
                            SUM(CASE WHEN e.Severity = 'Critical' THEN 1 ELSE 0 END) as CriticalCount,
                            SUM(CASE WHEN e.Severity = 'High' THEN 1 ELSE 0 END) as HighCount,
                            SUM(CASE WHEN e.Status = 'Resolved' THEN 1 ELSE 0 END) as ResolvedCount
                        FROM Applications a
                        LEFT JOIN ErrorLogs e ON a.Id = e.AppId 
                            AND e.CreatedAt >= DATEADD(day, -{days}, GETDATE())
                        GROUP BY a.Id, a.AppName
                        ORDER BY ErrorCount DESC";
                    command = new SqlCommand(query, connection);
                }
                else
                {
                    query = $@"
                        SELECT 
                            a.AppName,
                            COUNT(e.Id) as ErrorCount,
                            SUM(CASE WHEN e.Severity = 'Critical' THEN 1 ELSE 0 END) as CriticalCount,
                            SUM(CASE WHEN e.Severity = 'High' THEN 1 ELSE 0 END) as HighCount,
                            SUM(CASE WHEN e.Status = 'Resolved' THEN 1 ELSE 0 END) as ResolvedCount
                        FROM Applications a
                        LEFT JOIN ErrorLogs e ON a.Id = e.AppId 
                            AND e.CreatedAt >= DATEADD(day, -{days}, GETDATE())
                        WHERE a.CreatedBy = @UserId
                        GROUP BY a.Id, a.AppName
                        ORDER BY ErrorCount DESC";
                    command = new SqlCommand(query, connection);
                    command.Parameters.AddWithValue("@UserId", currentUserId.Value);
                }

                using var reader = await command.ExecuteReaderAsync();
                var applications = new List<object>();
                
                while (await reader.ReadAsync())
                {
                    applications.Add(new
                    {
                        appName = reader.GetString(0),
                        errorCount = reader.GetInt32(1),
                        criticalCount = reader.GetInt32(2),
                        highCount = reader.GetInt32(3),
                        resolvedCount = reader.GetInt32(4)
                    });
                }

                return Ok(applications);
            }
            catch
            {
                return Ok(new List<object>());
            }
        }

        [HttpGet("severity-breakdown")]
        public async Task<IActionResult> GetSeverityBreakdown([FromQuery] string dateRange = "7d")
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();

                var days = dateRange switch
                {
                    "1d" => 1,
                    "7d" => 7,
                    "30d" => 30,
                    "90d" => 90,
                    _ => 7
                };

                var query = $@"
                    SELECT 
                        Severity,
                        COUNT(*) as Count
                    FROM ErrorLogs 
                    WHERE CreatedAt >= DATEADD(day, -{days}, GETDATE())
                    GROUP BY Severity
                    ORDER BY 
                        CASE Severity 
                            WHEN 'Critical' THEN 1 
                            WHEN 'High' THEN 2 
                            WHEN 'Medium' THEN 3 
                            WHEN 'Low' THEN 4 
                            ELSE 5 
                        END";

                using var command = new SqlCommand(query, connection);
                using var reader = await command.ExecuteReaderAsync();

                var severityData = new List<object>();
                while (await reader.ReadAsync())
                {
                    severityData.Add(new
                    {
                        severity = reader.GetString(0),
                        count = reader.GetInt32(1)
                    });
                }

                return Ok(severityData);
            }
            catch
            {
                return Ok(new List<object>());
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

                // Get total error count and severity breakdown
                var totalCountQuery = currentUserRole == "ADMIN" 
                    ? "SELECT COUNT(*) FROM ErrorLogs"
                    : "SELECT COUNT(*) FROM ErrorLogs e INNER JOIN Applications a ON e.ApplicationId = a.Id WHERE a.CreatedBy = @UserId";
                
                using var totalCommand = new SqlCommand(totalCountQuery, connection);
                if (currentUserRole != "ADMIN")
                {
                    totalCommand.Parameters.AddWithValue("@UserId", currentUserId.Value);
                }
                var totalErrors = (int)await totalCommand.ExecuteScalarAsync();

                // Get severity breakdown
                var severityQuery = currentUserRole == "ADMIN" 
                    ? "SELECT Severity, COUNT(*) FROM ErrorLogs GROUP BY Severity"
                    : "SELECT e.Severity, COUNT(*) FROM ErrorLogs e INNER JOIN Applications a ON e.ApplicationId = a.Id WHERE a.CreatedBy = @UserId GROUP BY e.Severity";
                
                using var severityCommand = new SqlCommand(severityQuery, connection);
                if (currentUserRole != "ADMIN")
                {
                    severityCommand.Parameters.AddWithValue("@UserId", currentUserId.Value);
                }
                
                var severityCounts = new Dictionary<string, int>
                {
                    ["Critical"] = 0,
                    ["High"] = 0,
                    ["Medium"] = 0,
                    ["Low"] = 0
                };
                
                using var severityReader = await severityCommand.ExecuteReaderAsync();
                while (await severityReader.ReadAsync())
                {
                    var severity = severityReader.GetString(0);
                    var count = severityReader.GetInt32(1);
                    if (severityCounts.ContainsKey(severity))
                    {
                        severityCounts[severity] = count;
                    }
                }

                // Get monthly trend data for last 6 months
                var trendQuery = currentUserRole == "ADMIN" 
                    ? @"SELECT 
                        FORMAT(CreatedAt, 'yyyy-MM') as Month,
                        COUNT(*) as ErrorCount,
                        SUM(CASE WHEN Severity = 'Critical' THEN 1 ELSE 0 END) as Critical,
                        SUM(CASE WHEN Severity = 'High' THEN 1 ELSE 0 END) as High,
                        SUM(CASE WHEN Severity = 'Medium' THEN 1 ELSE 0 END) as Medium,
                        SUM(CASE WHEN Severity = 'Low' THEN 1 ELSE 0 END) as Low
                      FROM ErrorLogs 
                      WHERE CreatedAt >= DATEADD(month, -6, GETDATE())
                      GROUP BY FORMAT(CreatedAt, 'yyyy-MM')
                      ORDER BY Month"
                    : @"SELECT 
                        FORMAT(e.CreatedAt, 'yyyy-MM') as Month,
                        COUNT(*) as ErrorCount,
                        SUM(CASE WHEN e.Severity = 'Critical' THEN 1 ELSE 0 END) as Critical,
                        SUM(CASE WHEN e.Severity = 'High' THEN 1 ELSE 0 END) as High,
                        SUM(CASE WHEN e.Severity = 'Medium' THEN 1 ELSE 0 END) as Medium,
                        SUM(CASE WHEN e.Severity = 'Low' THEN 1 ELSE 0 END) as Low
                      FROM ErrorLogs e 
                      INNER JOIN Applications a ON e.ApplicationId = a.Id 
                      WHERE e.CreatedAt >= DATEADD(month, -6, GETDATE()) AND a.CreatedBy = @UserId2
                      GROUP BY FORMAT(e.CreatedAt, 'yyyy-MM')
                      ORDER BY Month";
                
                using var trendCommand = new SqlCommand(trendQuery, connection);
                if (currentUserRole != "ADMIN")
                {
                    trendCommand.Parameters.AddWithValue("@UserId2", currentUserId.Value);
                }
                
                var monthlyData = new Dictionary<string, object>();
                using var trendReader = await trendCommand.ExecuteReaderAsync();
                while (await trendReader.ReadAsync())
                {
                    var month = trendReader.GetString(0);
                    monthlyData[month] = new
                    {
                        errors = trendReader.GetInt32(1),
                        critical = trendReader.GetInt32(2),
                        high = trendReader.GetInt32(3),
                        medium = trendReader.GetInt32(4),
                        low = trendReader.GetInt32(5)
                    };
                }

                // Generate last 6 months with actual data
                var trends = new List<object>();
                var currentDate = DateTime.Now.AddMonths(-5);
                
                for (int i = 0; i < 6; i++)
                {
                    var monthKey = currentDate.ToString("yyyy-MM");
                    var monthName = currentDate.ToString("MMM yyyy");
                    
                    if (monthlyData.ContainsKey(monthKey))
                    {
                        var data = (dynamic)monthlyData[monthKey];
                        trends.Add(new
                        {
                            date = monthKey,
                            month = monthName,
                            errors = data.errors,
                            critical = data.critical,
                            high = data.high,
                            medium = data.medium,
                            low = data.low
                        });
                    }
                    else
                    {
                        trends.Add(new
                        {
                            date = monthKey,
                            month = monthName,
                            errors = 0,
                            critical = 0,
                            high = 0,
                            medium = 0,
                            low = 0
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
                        criticalErrors = severityCounts["Critical"],
                        highErrors = severityCounts["High"],
                        mediumErrors = severityCounts["Medium"],
                        lowErrors = severityCounts["Low"],
                        trend = totalErrors > 0 ? "increasing" : "stable"
                    }
                });
            }
            catch
            {
                // Return empty data structure matching Spring Boot format
                var emptyMonths = new List<object>();
                var currentDate = DateTime.Now.AddMonths(-5);
                
                for (int i = 0; i < 6; i++)
                {
                    emptyMonths.Add(new
                    {
                        date = currentDate.ToString("yyyy-MM"),
                        month = currentDate.ToString("MMM yyyy"),
                        errors = 0
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