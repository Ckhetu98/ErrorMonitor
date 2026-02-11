using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using Microsoft.Data.SqlClient;
using System.Security.Claims;
using ErrorMonitoringAPI.Controllers;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize]
public class SettingsController(IConfiguration configuration) : ControllerBase
{
    private readonly string _connectionString = configuration.GetConnectionString("DefaultConnection")!;

    private string? GetCurrentUserRole()
    {
        return HttpContext.User.FindFirst(ClaimTypes.Role)?.Value;
    }

    private bool IsAdmin()
    {
        var role = GetCurrentUserRole();
        return role?.ToUpper() == "ADMIN";
    }



    [HttpGet("security")]
    public async Task<IActionResult> GetSecuritySettings()
    {
        try
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var createTableQuery = @"
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='SecuritySettings' AND xtype='U')
                BEGIN
                    CREATE TABLE SecuritySettings (
                        Id INT IDENTITY(1,1) PRIMARY KEY,
                        SessionTimeoutMinutes INT NOT NULL DEFAULT 30,
                        MaxLoginAttempts INT NOT NULL DEFAULT 5,
                        CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
                        UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
                    )
                END";

            using var createCommand = new SqlCommand(createTableQuery, connection);
            await createCommand.ExecuteNonQueryAsync();

            var query = "SELECT TOP 1 * FROM SecuritySettings ORDER BY UpdatedAt DESC";
            using var command = new SqlCommand(query, connection);
            using var reader = await command.ExecuteReaderAsync();

            if (await reader.ReadAsync())
            {
                return Ok(new
                {
                    sessionTimeoutMinutes = Convert.ToInt32(reader["SessionTimeoutMinutes"]),
                    maxLoginAttempts = Convert.ToInt32(reader["MaxLoginAttempts"])
                });
            }

            return Ok(new { sessionTimeoutMinutes = 30, maxLoginAttempts = 5 });
        }
        catch
        {
            return StatusCode(500, new { message = "Failed to get security settings" });
        }
    }

    [HttpPost("security")]
    public async Task<IActionResult> SaveSecuritySettings([FromBody] SecuritySettingsRequest request)
    {
        if (!IsAdmin())
        {
            return StatusCode(403, new { message = "Only administrators can modify security settings" });
        }
        
        try
        {
            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var createTableQuery = @"
                IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='SecuritySettings' AND xtype='U')
                BEGIN
                    CREATE TABLE SecuritySettings (
                        Id INT IDENTITY(1,1) PRIMARY KEY,
                        SessionTimeoutMinutes INT NOT NULL DEFAULT 30,
                        MaxLoginAttempts INT NOT NULL DEFAULT 5,
                        CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
                        UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
                    )
                END";

            using var createCommand = new SqlCommand(createTableQuery, connection);
            await createCommand.ExecuteNonQueryAsync();

            var insertQuery = @"
                INSERT INTO SecuritySettings (SessionTimeoutMinutes, MaxLoginAttempts, CreatedAt, UpdatedAt)
                VALUES (@SessionTimeoutMinutes, @MaxLoginAttempts, GETDATE(), GETDATE())";

            using var command = new SqlCommand(insertQuery, connection);
            command.Parameters.AddWithValue("@SessionTimeoutMinutes", request.SessionTimeoutMinutes);
            command.Parameters.AddWithValue("@MaxLoginAttempts", request.MaxLoginAttempts);

            await command.ExecuteNonQueryAsync();
            return Ok(new { message = "Security settings saved successfully" });
        }
        catch
        {
            return StatusCode(500, new { message = "Failed to save security settings" });
        }
    }
}

public class SecuritySettingsRequest
{
    public int SessionTimeoutMinutes { get; set; }
    public int MaxLoginAttempts { get; set; }
}

