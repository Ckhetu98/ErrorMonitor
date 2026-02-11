using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Security.Claims;

namespace ErrorMonitoringAPI.Controllers
{
    [ApiController]
    [Route("api/audit")]
    public class AuditController : ControllerBase
    {
        private readonly string _connectionString;

        public AuditController(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection");
        }

        [HttpGet]
        public async Task<IActionResult> GetAuditLogs()
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();

                // Ensure AuditLogs table exists
                var createTableQuery = @"
                    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AuditLogs')
                    BEGIN
                        CREATE TABLE [AuditLogs] (
                            [Id] int IDENTITY(1,1) NOT NULL,
                            [UserId] int NOT NULL,
                            [Action] nvarchar(100) NOT NULL,
                            [EntityType] nvarchar(100) NOT NULL,
                            [EntityId] nvarchar(100) NOT NULL,
                            [OldValues] nvarchar(max) NULL,
                            [NewValues] nvarchar(max) NULL,
                            [IpAddress] nvarchar(45) NULL,
                            [UserAgent] nvarchar(500) NULL,
                            [CreatedAt] datetime2 NOT NULL,
                            CONSTRAINT [PK_AuditLogs] PRIMARY KEY ([Id])
                        )
                        
                        INSERT INTO AuditLogs (UserId, Action, EntityType, EntityId, NewValues, CreatedAt)
                        VALUES 
                        (1, 'CREATE', 'ErrorLog', '1', 'Created new error log', DATEADD(day, -5, GETDATE())),
                        (1, 'UPDATE', 'Application', '1', 'Updated application settings', DATEADD(day, -3, GETDATE())),
                        (1, 'DELETE', 'ErrorLog', '2', 'Deleted resolved error', DATEADD(day, -1, GETDATE()))
                    END";
                
                using var createCommand = new SqlCommand(createTableQuery, connection);
                await createCommand.ExecuteNonQueryAsync();

                var query = @"
                    SELECT TOP 100
                        al.*
                    FROM AuditLogs al
                    ORDER BY al.CreatedAt DESC";

                using var command = new SqlCommand(query, connection);
                using var reader = await command.ExecuteReaderAsync();

                var auditLogs = new List<object>();

                while (await reader.ReadAsync())
                {
                    auditLogs.Add(new
                    {
                        id = SafeConvertToInt(reader["Id"]),
                        userId = SafeConvertToInt(reader["UserId"]),
                        action = reader["Action"]?.ToString() ?? "",
                        entityType = reader["EntityType"]?.ToString() ?? "",
                        entityId = reader["EntityId"]?.ToString(),
                        oldValues = reader["OldValues"]?.ToString(),
                        newValues = reader["NewValues"]?.ToString(),
                        ipAddress = reader["IpAddress"]?.ToString(),
                        userAgent = reader["UserAgent"]?.ToString(),
                        createdAt = SafeConvertToDateTime(reader["CreatedAt"]).ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                        userName = "System"
                    });
                }

                return Ok(new { logs = auditLogs });
            }
            catch (Exception ex)
            {
                return Ok(new { logs = new List<object>(), error = ex.Message });
            }
        }

        [HttpPost("log")]
        public async Task<IActionResult> CreateAuditLog([FromBody] CreateAuditLogRequest request)
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();

                var query = @"
                    INSERT INTO AuditLogs 
                    (UserId, Action, EntityType, EntityId, OldValues, NewValues, IpAddress, UserAgent, CreatedAt)
                    VALUES 
                    (@UserId, @Action, @EntityType, @EntityId, @OldValues, @NewValues, @IpAddress, @UserAgent, @CreatedAt)";

                using var command = new SqlCommand(query, connection);

                command.Parameters.AddWithValue("@UserId", request.UserId);
                command.Parameters.AddWithValue("@Action", request.Action);
                command.Parameters.AddWithValue("@EntityType", request.EntityType);
                command.Parameters.AddWithValue("@EntityId", (object?)request.EntityId ?? DBNull.Value);
                command.Parameters.AddWithValue("@OldValues", (object?)request.OldValues ?? DBNull.Value);
                command.Parameters.AddWithValue("@NewValues", (object?)request.NewValues ?? DBNull.Value);
                command.Parameters.AddWithValue("@IpAddress", (object?)request.IpAddress ?? DBNull.Value);
                command.Parameters.AddWithValue("@UserAgent", (object?)request.UserAgent ?? DBNull.Value);
                command.Parameters.AddWithValue("@CreatedAt", DateTime.UtcNow);

                await command.ExecuteNonQueryAsync();

                return Ok(new { message = "Audit log created successfully" });
            }
            catch
            {
                return Ok(new { message = "Audit log creation failed" });
            }
        }

        [HttpGet("debug")]
        public async Task<IActionResult> DebugAuditLogs()
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();

                var tableExistsQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AuditLogs'";
                using var tableCommand = new SqlCommand(tableExistsQuery, connection);
                var tableExists = Convert.ToInt32(await tableCommand.ExecuteScalarAsync()) > 0;

                if (!tableExists)
                {
                    return Ok(new { tableExists = false, message = "AuditLogs table does not exist" });
                }

                var countQuery = "SELECT COUNT(*) FROM AuditLogs";
                using var countCommand = new SqlCommand(countQuery, connection);
                var count = SafeConvertToInt(await countCommand.ExecuteScalarAsync());

                return Ok(new { tableExists = true, count });
            }
            catch (Exception ex)
            {
                return Ok(new { error = ex.Message });
            }
        }

        [HttpPost("test-audit")]
        public async Task<IActionResult> TestAuditLog()
        {
            try
            {
                await LogAuditAsync(1, "TEST_ACTION", "Test", "123", null, "Test audit log entry");
                return Ok(new { message = "Test audit log created" });
            }
            catch (Exception ex)
            {
                return Ok(new { error = ex.Message });
            }
        }

        private static int SafeConvertToInt(object? value)
        {
            if (value == null) return 0;
            if (value is int intVal) return intVal;
            if (value is long longVal) return (int)longVal;
            if (int.TryParse(value.ToString(), out int result)) return result;
            return 0;
        }

        private static DateTime SafeConvertToDateTime(object? value)
        {
            if (value == null) return DateTime.Now;
            if (value is DateTime dateVal) return dateVal;
            if (DateTime.TryParse(value.ToString(), out DateTime result)) return result;
            return DateTime.Now;
        }

        private int? GetCurrentUserId()
        {
            var userIdClaim = HttpContext.User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            return int.TryParse(userIdClaim, out int userId) ? userId : null;
        }

        private async Task LogAuditAsync(int? userId, string action, string entityType, string? entityId, string? oldValues, string? newValues)
        {
            try
            {
                using var connection = new SqlConnection(_connectionString);
                await connection.OpenAsync();

                var query = @"
                    INSERT INTO AuditLogs 
                    (UserId, Action, EntityType, EntityId, OldValues, NewValues, IpAddress, UserAgent, CreatedAt)
                    VALUES 
                    (@UserId, @Action, @EntityType, @EntityId, @OldValues, @NewValues, @IpAddress, @UserAgent, @CreatedAt)";

                using var command = new SqlCommand(query, connection);

                command.Parameters.AddWithValue("@UserId", (object?)userId ?? DBNull.Value);
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
                // Ignore audit logging errors to not break main functionality
            }
        }
    }

    public class CreateAuditLogRequest
    {
        public int UserId { get; set; }
        public string Action { get; set; } = string.Empty;
        public string EntityType { get; set; } = string.Empty;
        public string? EntityId { get; set; }
        public string? OldValues { get; set; }
        public string? NewValues { get; set; }
        public string? IpAddress { get; set; }
        public string? UserAgent { get; set; }
    }
}