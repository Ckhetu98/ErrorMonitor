using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using System.Security.Claims;
using ErrorMonitoringAPI.Services;
using ErrorMonitoringAPI.Models;
using Microsoft.Data.SqlClient;

namespace ErrorMonitoringAPI.Controllers
{
    [ApiController]
    [Route("api/alerts")]
    [Authorize]
    public class AlertsController : ControllerBase
    {
        private readonly IAlertService _alertService;
        private readonly IEmailService _emailService;
        private readonly IConfiguration _configuration;

        public AlertsController(IAlertService alertService, IEmailService emailService, IConfiguration configuration)
        {
            _alertService = alertService;
            _emailService = emailService;
            _configuration = configuration;
        }

        private int? GetCurrentUserId()
        {
            var claim = HttpContext.User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            return int.TryParse(claim, out var id) ? id : null;
        }

        private string GetCurrentUserRole()
        {
            return HttpContext.User.FindFirst(ClaimTypes.Role)?.Value ?? "ADMIN";
        }

        [HttpGet("stats")]
        public async Task<IActionResult> GetAlertStats()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }
                
                var alerts = await _alertService.GetAllAlertsAsync(userId, userRole);
                
                var totalAlerts = alerts.Count;
                var unresolvedAlerts = alerts.Count(a => a.IsActive);
                var resolvedAlerts = alerts.Count(a => !a.IsActive);
                var criticalAlerts = alerts.Count(a => a.Name?.Contains("CRITICAL") == true);
                
                return Ok(new
                {
                    totalAlerts,
                    unresolvedAlerts,
                    resolvedAlerts,
                    criticalAlerts
                });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to fetch alert stats" });
            }
        }

        [HttpGet]
        public async Task<IActionResult> GetAlerts()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }
                
                var alerts = await _alertService.GetAllAlertsAsync(userId, userRole);
                return Ok(alerts.Select(MapAlertResponse));
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to fetch alerts" });
            }
        }

        [HttpGet("unresolved")]
        public async Task<IActionResult> GetUnresolvedAlerts()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }
                
                var alerts = await _alertService.GetUnresolvedAlertsAsync(userId, userRole);
                return Ok(alerts.Select(MapAlertResponse));
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to fetch unresolved alerts" });
            }
        }

        [HttpGet("my-applications")]
        public async Task<IActionResult> GetMyApplicationAlerts()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }
                
                // Force developer role filtering for this endpoint
                var alerts = await _alertService.GetAllAlertsAsync(userId, "DEVELOPER");
                return Ok(alerts.Select(MapAlertResponse));
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to fetch my application alerts" });
            }
        }

        [HttpPost]
        public async Task<IActionResult> CreateAlert([FromBody] CreateAlertRequest request)
        {
            try
            {
                var userId = GetCurrentUserId();
                
                if (userId == null)
                {
                    userId = 1; // System user for auto-generated alerts
                }

                var alert = new Alert
                {
                    Name = $"{request.AlertLevel} Alert - {request.ErrorLogId}",
                    Description = request.AlertMessage,
                    Condition = $"ErrorLogId: {request.ErrorLogId}, Level: {request.AlertLevel}",
                    Recipients = "choudharykhetesh8@gmail.com",
                    CreatedBy = userId.Value,
                    ApplicationId = request.ApplicationId ?? 1,
                    IsActive = true,
                    CreatedAt = DateTime.UtcNow
                };

                var id = await _alertService.CreateAlertAsync(alert);
                
                // Send email notification when alert is created
                try
                {
                    var subject = $"🚨 {request.AlertLevel} Alert - Application Error";
                    var body = $"Alert: This application has error\n\n" +
                              $"Application: {request.ApplicationId}\n" +
                              $"Error Log ID: {request.ErrorLogId}\n" +
                              $"Alert Level: {request.AlertLevel}\n" +
                              $"Message: {request.AlertMessage}\n\n" +
                              $"Please investigate this issue.";
                              
                    await _emailService.SendEmailAsync("choudharykhetesh8@gmail.com", subject, body);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Failed to send email notification: {ex.Message}");
                }
                
                return Ok(new
                {
                    message = "Alert created successfully!",
                    alert = new { id, alert.Name, alert.Description }
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = $"Failed to create alert: {ex.Message}" });
            }
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateAlert(int id, [FromBody] UpdateAlertRequest request)
        {
            try
            {
                var alert = await _alertService.GetAlertByIdAsync(id);
                if (alert == null)
                {
                    return NotFound(new { message = "Alert not found" });
                }

                if (!string.IsNullOrEmpty(request.AlertType))
                    alert.Name = $"{request.AlertLevel ?? "HIGH"} Alert";
                if (!string.IsNullOrEmpty(request.AlertMessage))
                    alert.Description = request.AlertMessage;
                if (request.IsActive.HasValue)
                    alert.IsActive = request.IsActive.Value;

                await _alertService.UpdateAlertAsync(alert);
                
                return Ok(new
                {
                    message = "Alert updated successfully",
                    alert = new { alert.Id, alert.Name, alert.Description }
                });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to update alert" });
            }
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteAlert(int id)
        {
            try
            {
                var success = await _alertService.DeleteAlertAsync(id);
                if (!success)
                {
                    return NotFound(new { message = "Alert not found" });
                }
                
                return Ok(new { message = "Alert deleted successfully" });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to delete alert" });
            }
        }

        [HttpPut("{id}/resolve")]
        public async Task<IActionResult> ResolveAlert(int id)
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var alert = await _alertService.GetAlertByIdAsync(id);
                if (alert == null)
                {
                    return NotFound(new { message = "Alert not found" });
                }

                var success = await _alertService.ResolveAlertAsync(id);
                if (!success)
                {
                    return NotFound(new { message = "Alert not found" });
                }
                
                // Send resolution email
                try
                {
                    var subject = $"✅ Alert Resolved - {alert.Name}";
                    var body = $"Alert: Error has been resolved\n\n" +
                              $"Alert: {alert.Name}\n" +
                              $"Description: {alert.Description}\n" +
                              $"Resolved At: {DateTime.UtcNow}\n" +
                              $"Resolved By: Administrator\n\n" +
                              $"The error has been successfully resolved.";
                              
                    await _emailService.SendEmailAsync("choudharykhetesh8@gmail.com", subject, body);
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"Failed to send resolution email: {ex.Message}");
                }
                
                return Ok(new { message = "Alert resolved successfully and email notification sent!" });
            }
            catch (Exception)
            {
                return StatusCode(500, new { message = "Failed to resolve alert" });
            }
        }

        private static object MapAlertResponse(Alert alert)
        {
            return new
            {
                id = alert.Id,
                applicationId = alert.ApplicationId,
                applicationName = alert.Name ?? "Unknown App", // Use alert.Name which contains the real application name
                errorLogId = alert.ErrorLogId ?? "", // Use the ErrorLogId field directly
                alertType = alert.AlertType ?? "EMAIL",
                alertLevel = alert.AlertLevel ?? "HIGH",
                alertMessage = alert.AlertMessage ?? "",
                isActive = alert.IsActive,
                isResolved = alert.IsResolved,
                createdAt = alert.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                resolvedAt = alert.ResolvedAt?.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                createdBy = alert.CreatedBy
            };
        }
    }

    public class CreateAlertRequest
    {
        public int? ApplicationId { get; set; }
        public string ErrorLogId { get; set; } = "";
        public string AlertType { get; set; } = "Email";
        public string AlertLevel { get; set; } = "High";
        public string AlertMessage { get; set; } = "";
    }

    public class UpdateAlertRequest
    {
        public string AlertType { get; set; }
        public string AlertLevel { get; set; }
        public string AlertMessage { get; set; }
        public bool? IsActive { get; set; }
    }
}