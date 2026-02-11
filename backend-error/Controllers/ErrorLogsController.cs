using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using System.Security.Claims;
using ErrorMonitoringAPI.Services;
using ErrorMonitoringAPI.DTOs;
using ErrorMonitoringAPI.Hubs;
using ErrorMonitoringAPI.Models;
using Microsoft.Data.SqlClient;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/errorlogs")]
public class ErrorLogsController : ControllerBase
{
    private readonly IErrorLogService _errorLogService;
    private readonly IHubContext<ErrorHub> _errorHubContext;
    private readonly IConfiguration _configuration;
    private readonly IAlertService _alertService;
    private readonly IEmailService _emailService;

    public ErrorLogsController(
        IErrorLogService errorLogService,
        IHubContext<ErrorHub> errorHubContext,
        IConfiguration configuration,
        IAlertService alertService,
        IEmailService emailService)
    {
        _errorLogService = errorLogService;
        _errorHubContext = errorHubContext;
        _configuration = configuration;
        _alertService = alertService;
        _emailService = emailService;
    }

    // =========================
    // HELPERS
    // =========================
    private int? GetUserId()
    {
        var value = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        return int.TryParse(value, out var id) ? id : null;
    }

    private string? GetUserRole()
    {
        return User.FindFirst(ClaimTypes.Role)?.Value;
    }

    private async Task LogAuditAsync(string action, string entityType, string? entityId, string? oldValues, string? newValues)
    {
        try
        {
            var userId = GetUserId();
            if (userId == null) return;

            var connectionString = _configuration.GetConnectionString("DefaultConnection");
            using var connection = new SqlConnection(connectionString);
            await connection.OpenAsync();

            var query = @"
                INSERT INTO AuditLogs 
                (UserId, Action, EntityType, EntityId, OldValues, NewValues, IpAddress, UserAgent, CreatedAt)
                VALUES 
                (@UserId, @Action, @EntityType, @EntityId, @OldValues, @NewValues, @IpAddress, @UserAgent, @CreatedAt)";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@UserId", userId.Value);
            command.Parameters.AddWithValue("@Action", action);
            command.Parameters.AddWithValue("@EntityType", entityType);
            command.Parameters.AddWithValue("@EntityId", (object?)entityId ?? DBNull.Value);
            command.Parameters.AddWithValue("@OldValues", (object?)oldValues ?? DBNull.Value);
            command.Parameters.AddWithValue("@NewValues", (object?)newValues ?? DBNull.Value);
            command.Parameters.AddWithValue("@IpAddress", (object?)HttpContext.Connection.RemoteIpAddress?.ToString() ?? DBNull.Value);
            command.Parameters.AddWithValue("@UserAgent", (object?)HttpContext.Request.Headers.UserAgent.ToString() ?? DBNull.Value);
            command.Parameters.AddWithValue("@CreatedAt", DateTime.UtcNow);

            await command.ExecuteNonQueryAsync();
        }
        catch
        {
            // Ignore audit logging errors
        }
    }

    //testing
    [HttpGet("test")]
    [AllowAnonymous]
    public IActionResult TestConnection()
    {
        return Ok($"API is working at {DateTime.UtcNow:O}");
    }

    //health check
    [HttpGet("health")]
    [AllowAnonymous]
    public IActionResult HealthCheck()
    {
        return Ok(new { status = "healthy", timestamp = DateTime.UtcNow });
    }

   //error log generate
    [HttpGet]
    [Authorize]
    public async Task<IActionResult> GetErrorLogs(
        [FromQuery] string? severity = null,
        [FromQuery] string? status = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 100)
    {
        try
        {
            var userId = GetUserId();
            var userRole = GetUserRole();
            
            if (userId == null)
            {
                return Unauthorized(new { message = "User not authenticated" });
            }
            
            var logs = await _errorLogService
                .GetErrorLogsAsync(severity, status, page, pageSize, userId, userRole);

            var response = logs.Select(e => new
            {
                id = (long)e.Id,
                application = e.Application,
                endpoint = e.Endpoint ?? "/api/unknown",
                errorMessage = e.ErrorMessage,
                severity = e.Severity,
                status = NormalizeStatus(e.Status),
                errorType = e.ErrorType ?? "SystemError",
                stackTrace = e.StackTrace ?? "",
                createdAt = e.CreatedAt
            });

            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Failed to fetch error logs: {ex.Message}");
        }
    }

     [HttpPost("log")]
    [AllowAnonymous]
    public async Task<IActionResult> LogError([FromBody] ErrorLogRequest request)
    {
        try
        {
            var errorId = await _errorLogService.LogErrorAsync(request);

            if (errorId == -1)
                return Ok("Application is inactive - error not logged");
            
            if (errorId == -2)
                return Ok(new { message = "Application is paused - error ignored", paused = true });

            Console.WriteLine($"✅ Error logged with ID: {errorId}, automatic alert and email handled by ErrorLogService");

            // Send real-time notification
            await _errorHubContext.Clients.All.SendAsync("NewError", new
            {
                id = errorId,
                application = request.ApplicationName,
                endpoint = request.ApiEndpoint,
                error = request.ErrorMessage,
                severity = request.Severity,
                timestamp = DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
            });

            return Ok($"Error logged successfully with ID: {errorId}");
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Failed to log error: {ex.Message}");
        }
    }
    // Resolve error
    [HttpPut("{id}/resolve")]
    [Authorize]
    public async Task<IActionResult> ResolveError(int id)
    {
        try
        {
            var success = await _errorLogService.ResolveErrorAsync(id);
            if (!success)
                return NotFound("Error not found");

            // Log audit
            await LogAuditAsync("RESOLVE", "ErrorLog", id.ToString(), "Status: Open", "Status: Resolved");

            return Ok(new { message = "Error resolved successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Failed to resolve error: {ex.Message}");
        }
    }

  //delete error
    [HttpDelete("{id}")]
    [Authorize]
    public async Task<IActionResult> DeleteError(int id)
    {
        try
        {
            if (GetUserRole() == "VIEWER")
                return Forbid("Viewers cannot delete errors");

            var success = await _errorLogService.DeleteErrorAsync(id);
            if (!success)
                return NotFound("Error not found");

            // Log audit
            await LogAuditAsync("DELETE", "ErrorLog", id.ToString(), $"ErrorLog ID: {id}", null);

            return Ok(new { message = "Error deleted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Failed to delete error: {ex.Message}");
        }
    }
//status normalizer
    private static string NormalizeStatus(string? status)
    {
        return status switch
        {
            "OPEN" => "Open",
            "IN_PROGRESS" => "In Progress",
            "RESOLVED" or "Resolved" => "Resolved",
            "IGNORED" => "Ignored",
            _ => "Open"
        };
    }
}
