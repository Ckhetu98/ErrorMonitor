using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using ErrorMonitoringAPI.DTOs;
using ErrorMonitoringAPI.Services;
using System.Security.Claims;
using Microsoft.Data.SqlClient;

namespace ErrorMonitoringAPI.Controllers
{
    [ApiController]
    [Route("api/applications")]
    [Authorize]
    public class ApplicationsController : ControllerBase
    {
        private readonly IApplicationService _applicationService;
        private readonly IConfiguration _configuration;

        public ApplicationsController(IApplicationService applicationService, IConfiguration configuration)
        {
            _applicationService = applicationService;
            _configuration = configuration;
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

        private async Task LogAuditAsync(string action, string entityType, string? entityId, string? oldValues, string? newValues)
        {
            try
            {
                var userId = GetCurrentUserId();
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

        [HttpGet]
        public async Task<IActionResult> GetApplications()
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var applications = await _applicationService.GetAllApplicationsAsync(userId, userRole);
                return Ok(applications);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    message = "Failed to fetch applications",
                    error = ex.Message
                });
            }
        }

        [HttpPost]
        public async Task<IActionResult> CreateApplication([FromBody] ApplicationRequest request)
        {
            try
            {
                var userId = GetCurrentUserId();
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                var appId = await _applicationService.CreateApplicationAsync(request, userId.Value);
                
                // Get the created application to return the API key
                var createdApp = await _applicationService.GetApplicationByIdAsync(appId);
                
                // Log audit
                await LogAuditAsync("CREATE", "Application", appId.ToString(), null, $"Name: {request.name}, Description: {request.description}");
                
                return Ok(new
                {
                    id = appId,
                    apiKey = createdApp?.apiKey,
                    message = "Application created successfully"
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    message = "Failed to create application",
                    error = ex.Message
                });
            }
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateApplication(int id, [FromBody] ApplicationRequest request)
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                // Get old values for audit
                var oldApp = await _applicationService.GetApplicationByIdAsync(id);
                var oldValues = oldApp != null ? $"Name: {oldApp.name}, Active: {oldApp.isActive}" : null;

                // Check if user has permission to update this application
                if (userRole?.ToUpper() == "DEVELOPER")
                {
                    var isOwner = await _applicationService.IsApplicationOwnerAsync(id, userId.Value);
                    if (!isOwner)
                    {
                        return StatusCode(403, new { message = "You can only update applications you created" });
                    }
                }
                // ADMIN can update any application

                // Validate request
                if (string.IsNullOrWhiteSpace(request.name))
                {
                    return BadRequest(new { message = "Application name is required" });
                }

                var success = await _applicationService.UpdateApplicationAsync(id, request);
                if (!success)
                    return NotFound(new { message = "Application not found or update failed" });

                // Log audit
                var newValues = $"Name: {request.name}, Active: {request.isActive}";
                await LogAuditAsync("UPDATE", "Application", id.ToString(), oldValues, newValues);

                return Ok(new { message = "Application updated successfully" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    message = "Failed to update application",
                    error = ex.Message,
                    innerError = ex.InnerException?.Message
                });
            }
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteApplication(int id)
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                if (userRole?.ToUpper() != "ADMIN")
                {
                    return StatusCode(403, new { message = "Only administrators can delete applications" });
                }

                // Get app details for audit before deletion
                var app = await _applicationService.GetApplicationByIdAsync(id);
                var appDetails = app != null ? $"Name: {app.name}, ID: {app.Id}" : $"ID: {id}";

                var success = await _applicationService.DeleteApplicationAsync(id);
                if (!success)
                    return NotFound(new { message = "Application not found" });

                // Log audit
                await LogAuditAsync("DELETE", "Application", id.ToString(), appDetails, null);

                return Ok(new { message = "Application deleted successfully" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    message = "Failed to delete application",
                    error = ex.Message
                });
            }
        }

        [HttpPut("{id}/pause")]
        public async Task<IActionResult> PauseApplication(int id)
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                // Check permissions
                if (userRole?.ToUpper() == "DEVELOPER")
                {
                    var isOwner = await _applicationService.IsApplicationOwnerAsync(id, userId.Value);
                    if (!isOwner)
                    {
                        return StatusCode(403, new { message = "You can only pause applications you created" });
                    }
                }

                var success = await _applicationService.PauseApplicationAsync(id);
                if (!success)
                    return NotFound(new { message = "Application not found" });

                await LogAuditAsync("PAUSE", "Application", id.ToString(), null, "Status: INACTIVE");
                return Ok(new { message = "Application paused successfully" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to pause application", error = ex.Message });
            }
        }

        [HttpPut("{id}/resume")]
        public async Task<IActionResult> ResumeApplication(int id)
        {
            try
            {
                var userId = GetCurrentUserId();
                var userRole = GetCurrentUserRole();
                
                if (userId == null)
                {
                    return Unauthorized(new { message = "User not authenticated" });
                }

                // Check permissions
                if (userRole?.ToUpper() == "DEVELOPER")
                {
                    var isOwner = await _applicationService.IsApplicationOwnerAsync(id, userId.Value);
                    if (!isOwner)
                    {
                        return StatusCode(403, new { message = "You can only resume applications you created" });
                    }
                }

                var success = await _applicationService.ResumeApplicationAsync(id);
                if (!success)
                    return NotFound(new { message = "Application not found" });

                await LogAuditAsync("RESUME", "Application", id.ToString(), null, "Status: ACTIVE");
                return Ok(new { message = "Application resumed successfully" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Failed to resume application", error = ex.Message });
            }
        }
    }
}