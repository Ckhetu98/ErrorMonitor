using ErrorMonitoringAPI.DTOs;
using Microsoft.EntityFrameworkCore;
using ErrorMonitoringAPI.Data;
using ErrorMonitoringAPI.Models;

namespace ErrorMonitoringAPI.Services
{
public class ErrorLogService : IErrorLogService
{
    private readonly ApplicationDbContext _context;
    private readonly IEmailSenderService _emailService;

    public ErrorLogService(ApplicationDbContext context, IEmailSenderService emailService)
    {
        _context = context;
        _emailService = emailService;
    }

    public async Task<List<ErrorLogDto>> GetErrorLogsAsync(
        string severity = null,
        string status = null,
        int page = 1,
        int pageSize = 20,
        int? userId = null,
        string userRole = null)
    {
        try
        {
            var query = _context.ErrorLogs
                .Include(e => e.Application)
                .AsQueryable();

            // Role-based filtering - EXACT match with Spring Boot
            if (userRole == "ADMIN")
            {
                // Admin sees ALL errors
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
                // VIEWER sees all errors (read-only)
            }

            if (!string.IsNullOrEmpty(severity))
                query = query.Where(e => e.Severity == severity);
            if (!string.IsNullOrEmpty(status))
                query = query.Where(e => e.Status == status);

            var errorLogs = await query
                .OrderByDescending(e => e.CreatedAt) // Most recent first
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return errorLogs.Select(e => new ErrorLogDto
            {
                Id = e.Id,
                Application = e.Application?.Name ?? "Unknown App",
                Endpoint = e.Source ?? "N/A",
                ErrorMessage = e.Message ?? "No message",
                StackTrace = e.StackTrace ?? "",
                Severity = e.Severity,
                Status = e.Status ?? "Open",
                ErrorType = "SystemError", // Default type
                CreatedAt = e.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                HttpMethod = e.HttpMethod,
                UserAgent = e.UserAgent,
                IpAddress = e.IpAddress
            }).ToList();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error in GetErrorLogsAsync: {ex.Message}");
            return new List<ErrorLogDto>();
        }
    }

    public async Task<int> LogErrorAsync(ErrorLogRequest request)
    {
        Application app;
        
        // Debug: Log what we received
        Console.WriteLine($"📥 Received error log request:");
        Console.WriteLine($"   ApplicationName: '{request.ApplicationName}'");
        Console.WriteLine($"   ApiKey: '{request.ApiKey}'");
        Console.WriteLine($"   ErrorMessage: '{request.ErrorMessage}'");
        
        // Try to find application by API key first, then by name
        if (!string.IsNullOrEmpty(request.ApiKey))
        {
            app = await _context.Applications
                .FirstOrDefaultAsync(a => a.ApiKey == request.ApiKey);
            
            if (app == null)
            {
                Console.WriteLine($"Application with API key '{request.ApiKey}' not found, creating new app");
                app = await GetOrCreateApplicationAsync(request.ApplicationName ?? "Unknown Application");
            }
            else
            {
                Console.WriteLine($"Found application by API key: '{app.Name}'");
            }
        }
        else
        {
            Console.WriteLine($"No API key provided, using application name: '{request.ApplicationName}'");
            app = await GetOrCreateApplicationAsync(request.ApplicationName ?? "Unknown Application");
        }
        
        // Check if application is paused
        if (app.IsPaused)
        {
            Console.WriteLine($"⏸️ Application '{app.Name}' is paused - ignoring error");
            return -2; // Special code for paused
        }
        
        if (!app.IsActive) return -1;

        await CleanupOldErrorsIfNeeded(app.Id);

        // Handle severity conversion with proper logic - EXACT match with Spring Boot
        string severity;
        if (!string.IsNullOrEmpty(request.Severity))
        {
            switch (request.Severity.ToLower())
            {
                case "critical":
                    severity = "Critical";
                    break;
                case "high":
                    severity = "High";
                    break;
                case "medium":
                    severity = "Medium";
                    break;
                case "low":
                    severity = "Low";
                    break;
                default:
                    severity = "Medium"; // Default to Medium for unknown severity
                    Console.WriteLine($"Unknown severity '{request.Severity}', defaulting to Medium");
                    break;
            }
        }
        else
        {
            severity = "Medium"; // Default to Medium if no severity provided
            Console.WriteLine("No severity provided, defaulting to Medium");
        }

        var errorLog = new ErrorLog
        {
            ApplicationId = app.Id,
            Message = request.ErrorMessage,
            StackTrace = request.StackTrace,
            Source = request.ApiEndpoint,
            Severity = severity,
            Status = "Open",
            HttpMethod = request.HttpMethod,
            UserAgent = request.UserAgent,
            IpAddress = request.IpAddress,
            Environment = "PRODUCTION",
            CreatedAt = DateTime.UtcNow
        };

        _context.ErrorLogs.Add(errorLog);
        await _context.SaveChangesAsync();

        // Auto-create alert for any error (all severities) with REAL application name
        Console.WriteLine($"🚨 Creating alert with application name: '{app.Name}'");
        await CreateAlertAsync(errorLog.Id, severity, request.ErrorMessage, app.Id, app.Name);
        
        Console.WriteLine($"✅ Error logged with ID: {errorLog.Id}, Alert created automatically for '{app.Name}'");

        return errorLog.Id;
    }

    public async Task<bool> ResolveErrorAsync(int id)
    {
        var errorLog = await _context.ErrorLogs.FindAsync(id);
        if (errorLog == null) return false;

        errorLog.Status = "Resolved";
        errorLog.ResolvedAt = DateTime.UtcNow;
        
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<Application> GetOrCreateApplicationAsync(string appName)
    {
        var app = await _context.Applications
            .FirstOrDefaultAsync(a => a.Name == appName);

        if (app != null) return app;

        app = new Application
        {
            Name = appName,
            Description = $"Auto-created for {appName}",
            IsActive = true,
            CreatedAt = DateTime.UtcNow,
            CreatedBy = 1
        };

        _context.Applications.Add(app);
        await _context.SaveChangesAsync();
        return app;
    }

    public async Task CreateAlertAsync(int errorLogId, string severity, string errorMessage, int applicationId, string applicationName)
    {
        try
        {
            var alert = new Alert
            {
                Name = applicationName,
                Description = errorMessage, // Use just the error message, not the full description
                Condition = $"{severity} level alert",
                Recipients = "choudharykhetesh8@gmail.com",
                IsActive = true,
                IsResolved = false,
                CreatedAt = DateTime.UtcNow,
                CreatedBy = 1,
                ApplicationId = null, // Set to null to avoid foreign key issues
                AlertType = "EMAIL",
                AlertLevel = severity.ToUpper(),
                AlertMessage = errorMessage, // Just the error message
                ErrorLogId = errorLogId.ToString()
            };

            _context.Alerts.Add(alert);
            await _context.SaveChangesAsync();
            
            Console.WriteLine($"✅ Alert created with ID: {alert.Id}");
            
            // Send email notification automatically
            await _emailService.SendAlertEmailAsync(
                "choudharykhetesh8@gmail.com",
                $"{applicationName}: {errorMessage}",
                severity
            );
            Console.WriteLine($"✅ Alert created and email sent for error ID: {errorLogId}");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Failed to create alert or send email: {ex.Message}");
            throw;
        }
    }

    private async Task CleanupOldErrorsIfNeeded(int appId)
    {
        var errorCount = await _context.ErrorLogs
            .CountAsync(e => e.ApplicationId == appId);
        
        if (errorCount >= 10)
        {
            var oldErrors = await _context.ErrorLogs
                .Where(e => e.ApplicationId == appId && e.Status == "Resolved")
                .OrderBy(e => e.ResolvedAt)
                .Take(Math.Max(0, errorCount - 5))
                .ToListAsync();
            
            _context.ErrorLogs.RemoveRange(oldErrors);
            await _context.SaveChangesAsync();
        }
    }

    public async Task<bool> DeleteErrorAsync(int id)
    {
        using var transaction = await _context.Database.BeginTransactionAsync();
        try
        {
            var alerts = await _context.Alerts
                .Where(a => a.Id == id)
                .ToListAsync();
            _context.Alerts.RemoveRange(alerts);

            var errorLog = await _context.ErrorLogs.FindAsync(id);
            if (errorLog == null) return false;
            
            _context.ErrorLogs.Remove(errorLog);
            await _context.SaveChangesAsync();
            await transaction.CommitAsync();
            return true;
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }
    }
}
}