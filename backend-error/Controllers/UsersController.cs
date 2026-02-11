using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.Data.SqlClient;
using BCrypt.Net;
using System.Security.Claims;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/users")]
[Authorize]
public class UsersController(IConfiguration configuration) : ControllerBase
{
    private readonly string _connectionString = configuration.GetConnectionString("DefaultConnection") ?? string.Empty;

    private string? GetCurrentUserRole()
    {
        return HttpContext.User.FindFirst(ClaimTypes.Role)?.Value;
    }

    private bool IsAdmin()
    {
        var role = GetCurrentUserRole();
        return role?.ToUpper() == "ADMIN";
    }

    [HttpGet]
    public async Task<IActionResult> GetUsers()
    {
        if (!IsAdmin())
        {
            return StatusCode(403, new { message = "Only administrators can view users" });
        }
        
        try
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var query = @"
                SELECT Id, Username, Email, Role, TwoFactorEnabled, CreatedAt, LastLoginAt, IsActive
                FROM Users 
                ORDER BY CreatedAt DESC";

            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            var users = new List<object>();

            while (await reader.ReadAsync())
            {
                users.Add(new
                {
                    id = reader.GetInt32(reader.GetOrdinal("Id")),
                    username = reader.GetString(reader.GetOrdinal("Username")),
                    email = reader.GetString(reader.GetOrdinal("Email")),
                    role = reader.GetString(reader.GetOrdinal("Role")),
                    twoFactorEnabled = reader.GetBoolean(reader.GetOrdinal("TwoFactorEnabled")),
                    createdAt = reader.GetDateTime(reader.GetOrdinal("CreatedAt")).ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                    lastLoginAt = reader.IsDBNull(reader.GetOrdinal("LastLoginAt")) 
                        ? null 
                        : reader.GetDateTime(reader.GetOrdinal("LastLoginAt")).ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                    isActive = reader.GetBoolean(reader.GetOrdinal("IsActive")),
                    status = reader.GetBoolean(reader.GetOrdinal("IsActive")) ? "Active" : "Inactive"
                });
            }

            return Ok(users);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to fetch users", error = ex.Message });
        }
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateUser(int id, [FromBody] UpdateUserRequest request)
    {
        if (!IsAdmin())
        {
            return StatusCode(403, new { message = "Only administrators can update users" });
        }
        
        try
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var query = @"
                UPDATE Users 
                SET Username = @Username, Email = @Email, Role = @Role, TwoFactorEnabled = @TwoFactorEnabled
                WHERE Id = @Id";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);
            command.Parameters.AddWithValue("@Username", request.Username);
            command.Parameters.AddWithValue("@Email", request.Email);
            command.Parameters.AddWithValue("@Role", request.Role);
            command.Parameters.AddWithValue("@TwoFactorEnabled", request.TwoFactorEnabled);

            var rows = await command.ExecuteNonQueryAsync();
            if (rows == 0)
                return NotFound(new { message = "User not found" });

            return Ok(new { message = "User updated successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to update user", error = ex.Message });
        }
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteUser(int id)
    {
        if (!IsAdmin())
        {
            return StatusCode(403, new { message = "Only administrators can delete users" });
        }
        
        try
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var command = new SqlCommand("DELETE FROM Users WHERE Id = @Id", connection);
            command.Parameters.AddWithValue("@Id", id);

            var rows = await command.ExecuteNonQueryAsync();
            if (rows == 0)
                return NotFound(new { message = "User not found" });

            return Ok(new { message = "User deleted successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to delete user", error = ex.Message });
        }
    }

    [HttpPut("{id}/toggle-status")]
    public async Task<IActionResult> ToggleUserStatus(int id)
    {
        if (!IsAdmin())
        {
            return StatusCode(403, new { message = "Only administrators can toggle user status" });
        }
        
        try
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var query = @"
                UPDATE Users 
                SET IsActive = CASE WHEN IsActive = 1 THEN 0 ELSE 1 END
                WHERE Id = @Id;
                SELECT IsActive FROM Users WHERE Id = @Id;";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@Id", id);

            var newStatus = await command.ExecuteScalarAsync();
            if (newStatus == null)
                return NotFound(new { message = "User not found" });

            return Ok(new { 
                message = "User status updated successfully", 
                isActive = Convert.ToBoolean(newStatus)
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to update user status", error = ex.Message });
        }
    }
}

public class UpdateUserRequest
{
    public string Username { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public bool TwoFactorEnabled { get; set; } = false;
}