using ErrorMonitoringAPI.Models;
using Microsoft.EntityFrameworkCore;
using ErrorMonitoringAPI.Data;

namespace ErrorMonitoringAPI.Services;

public class AlertService : IAlertService
{
    private readonly ApplicationDbContext _context;
    private readonly IEmailSenderService _emailService;

    public AlertService(ApplicationDbContext context, IEmailSenderService emailService)
    {
        _context = context;
        _emailService = emailService;
    }

    public async Task<List<Alert>> GetAllAlertsAsync(int? userId = null, string userRole = null)
    {
        var query = _context.Alerts
            .Include(a => a.Application)
            .AsQueryable();
        
        // Role-based filtering - EXACT match with Spring Boot
        if (userRole == "ADMIN")
        {
            // Admin sees all alerts
        }
        else if (userRole == "DEVELOPER" && userId.HasValue)
        {
            // Developer sees alerts for applications they created OR alerts sent to their email
            var user = await _context.Users.FindAsync(userId.Value);
            var userEmail = user?.Email;
            
            var userAppIds = await _context.Applications
                .Where(a => a.CreatedBy == userId.Value)
                .Select(a => a.Id)
                .ToListAsync();
            
            // Show alerts if: user owns the application OR alert is sent to user's email
            query = query.Where(a => 
                (a.ApplicationId.HasValue && userAppIds.Contains(a.ApplicationId.Value)) ||
                (userEmail != null && a.Recipients.Contains(userEmail))
            );
        }
        else if (userRole == "VIEWER")
        {
            // VIEWER sees all alerts but read-only
        }
        
        return await query
            .OrderByDescending(a => a.CreatedAt)
            .ToListAsync();
    }

    public async Task<Alert> GetAlertByIdAsync(int id)
    {
        return await _context.Alerts.FindAsync(id);
    }

    public async Task<int> CreateAlertAsync(Alert alert)
    {
        alert.IsActive = true;
        alert.CreatedAt = DateTime.UtcNow;
        
        _context.Alerts.Add(alert);
        await _context.SaveChangesAsync();
        return alert.Id;
    }

    public async Task<bool> UpdateAlertAsync(Alert alert)
    {
        _context.Alerts.Update(alert);
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> ResolveAlertAsync(int id)
    {
        var alert = await _context.Alerts.FindAsync(id);
        if (alert == null) return false;

        alert.IsActive = false;
        alert.IsResolved = true;
        alert.ResolvedAt = DateTime.UtcNow;
        
        var success = await _context.SaveChangesAsync() > 0;
        
        if (success)
        {
            try
            {
                await _emailService.SendEmailAsync(
                    "choudharykhetesh8@gmail.com",
                    "✅ Alert Resolved - Error Monitoring",
                    $"<h3>Alert Resolved</h3><p><strong>Alert:</strong> {alert.Name}</p><p><strong>Description:</strong> {alert.Description}</p><p><strong>Resolved At:</strong> {DateTime.UtcNow}</p>"
                );
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Failed to send resolution email: {ex.Message}");
            }
        }
        
        return success;
    }

    public async Task<List<Alert>> GetUnresolvedAlertsAsync(int? userId = null, string userRole = null)
    {
        var query = _context.Alerts
            .Include(a => a.Application)
            .Where(a => !a.IsResolved)
            .AsQueryable();
        
        // Role-based filtering - EXACT match with Spring Boot
        if (userRole == "ADMIN")
        {
            // Admin sees all unresolved alerts
        }
        else if (userRole == "DEVELOPER" && userId.HasValue)
        {
            // Developer sees unresolved alerts for applications they created OR alerts sent to their email
            var user = await _context.Users.FindAsync(userId.Value);
            var userEmail = user?.Email;
            
            var userAppIds = await _context.Applications
                .Where(a => a.CreatedBy == userId.Value)
                .Select(a => a.Id)
                .ToListAsync();
            
            // Show alerts if: user owns the application OR alert is sent to user's email
            query = query.Where(a => 
                (a.ApplicationId.HasValue && userAppIds.Contains(a.ApplicationId.Value)) ||
                (userEmail != null && a.Recipients.Contains(userEmail))
            );
        }
        else if (userRole == "VIEWER")
        {
            // VIEWER sees no unresolved alerts (read-only)
            return new List<Alert>();
        }
        
        return await query
            .OrderByDescending(a => a.CreatedAt)
            .ToListAsync();
    }

    public async Task<object> GetAlertStatsAsync(int? userId = null, string userRole = null)
    {
        var query = _context.Alerts.AsQueryable();
        
        // Role-based filtering
        if (userRole == "DEVELOPER" && userId.HasValue)
        {
            var userAppIds = await _context.Applications
                .Where(a => a.CreatedBy == userId.Value)
                .Select(a => a.Id)
                .ToListAsync();
            
            query = query.Where(a => userAppIds.Contains(a.ApplicationId ?? 0));
        }
        // ADMIN can see all alert stats
        
        var totalAlerts = await query.CountAsync();
        var unresolvedAlerts = await query.CountAsync(a => a.IsActive);
        var resolvedAlerts = await query.CountAsync(a => !a.IsActive);
        var criticalAlerts = await query.CountAsync(a => a.Name.Contains("Critical"));

        return new
        {
            TotalAlerts = totalAlerts,
            UnresolvedAlerts = unresolvedAlerts,
            ResolvedAlerts = resolvedAlerts,
            CriticalAlerts = criticalAlerts
        };
    }

    public async Task<bool> DeleteAlertAsync(int id)
    {
        var alert = await _context.Alerts.FindAsync(id);
        if (alert == null) return false;

        _context.Alerts.Remove(alert);
        return await _context.SaveChangesAsync() > 0;
    }
}