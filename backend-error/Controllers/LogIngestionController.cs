using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class LogIngestionController(IConfiguration configuration) : ControllerBase
{
    private readonly IConfiguration _configuration = configuration;
    private readonly string _connectionString = configuration.GetConnectionString("DefaultConnection");

    [HttpPost("ingest")]
    public async Task<IActionResult> IngestLog([FromBody] LogIngestionRequest request)
    {
        try
        {
            if (string.IsNullOrEmpty(request.ApplicationName) || 
                string.IsNullOrEmpty(request.ErrorMessage) || 
                string.IsNullOrEmpty(request.Severity))
            {
                return BadRequest(new { success = false, message = "Invalid log format" });
            }

            using var connection = new SqlConnection(_connectionString);
            await connection.OpenAsync();

            var appId = await GetOrCreateApplicationAsync(connection, request.ApplicationName);

            var query = @"
                INSERT INTO ErrorLogs (ApplicationId, ApiEndpoint, ErrorMessage, ErrorType, Severity, 
                                     ErrorSource, StackTrace, HttpMethod, UserAgent, IpAddress, CreatedAt, Status)
                VALUES (@ApplicationId, @ApiEndpoint, @ErrorMessage, @ErrorType, @Severity, 
                        @ErrorSource, @StackTrace, @HttpMethod, @UserAgent, @IpAddress, GETDATE(), 'Open');
                SELECT SCOPE_IDENTITY();";

            using var command = new SqlCommand(query, connection);
            command.Parameters.AddWithValue("@ApplicationId", appId);
            command.Parameters.AddWithValue("@ApiEndpoint", request.ApiEndpoint ?? "");
            command.Parameters.AddWithValue("@ErrorMessage", request.ErrorMessage);
            command.Parameters.AddWithValue("@ErrorType", request.ErrorType ?? "UnknownError");
            command.Parameters.AddWithValue("@Severity", request.Severity);
            command.Parameters.AddWithValue("@ErrorSource", request.ErrorSource ?? "Backend");
            command.Parameters.AddWithValue("@StackTrace", request.StackTrace ?? "");
            command.Parameters.AddWithValue("@HttpMethod", request.HttpMethod ?? "");
            command.Parameters.AddWithValue("@UserAgent", request.UserAgent ?? "");
            command.Parameters.AddWithValue("@IpAddress", request.IpAddress ?? "");

            var errorLogId = Convert.ToInt32(await command.ExecuteScalarAsync());

            return Ok(new { 
                success = true, 
                message = "Log ingested successfully", 
                errorLogId = errorLogId,
                timestamp = DateTime.UtcNow 
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { 
                success = false, 
                message = "Failed to ingest log", 
                error = ex.Message 
            });
        }
    }

    [HttpPost("ingest-bulk")]
    public async Task<IActionResult> IngestBulkLogs([FromBody] BulkLogIngestionRequest request)
    {
        var results = new List<object>();
        
        foreach (var log in request.ErrorLogs)
        {
            try
            {
                var result = await IngestSingleLog(log);
                results.Add(result);
            }
            catch (Exception ex)
            {
                results.Add(new { 
                    success = false, 
                    message = ex.Message,
                    errorMessage = log.ErrorMessage 
                });
            }
        }

        return Ok(results);
    }

    [HttpPost("validate")]
    public IActionResult ValidateLogFormat([FromBody] LogIngestionRequest request)
    {
        var validSeverities = new[] { "Low", "Medium", "High", "Critical" };
        
        var isValid = !string.IsNullOrEmpty(request.ApplicationName) &&
                     !string.IsNullOrEmpty(request.ErrorMessage) &&
                     !string.IsNullOrEmpty(request.Severity) &&
                     validSeverities.Contains(request.Severity);

        return Ok(new { 
            isValid, 
            message = isValid ? "Valid format" : "Invalid format" 
        });
    }

    private async Task<object> IngestSingleLog(LogIngestionRequest request)
    {
        using var connection = new SqlConnection(_connectionString);
        await connection.OpenAsync();

        var appId = await GetOrCreateApplicationAsync(connection, request.ApplicationName);

        var query = @"
            INSERT INTO ErrorLogs (ApplicationId, ApiEndpoint, ErrorMessage, ErrorType, Severity, 
                                 ErrorSource, StackTrace, HttpMethod, UserAgent, IpAddress, CreatedAt, Status)
            VALUES (@ApplicationId, @ApiEndpoint, @ErrorMessage, @ErrorType, @Severity, 
                    @ErrorSource, @StackTrace, @HttpMethod, @UserAgent, @IpAddress, GETDATE(), 'Open');
            SELECT SCOPE_IDENTITY();";

        using var command = new SqlCommand(query, connection);
        command.Parameters.AddWithValue("@ApplicationId", appId);
        command.Parameters.AddWithValue("@ApiEndpoint", request.ApiEndpoint ?? "");
        command.Parameters.AddWithValue("@ErrorMessage", request.ErrorMessage);
        command.Parameters.AddWithValue("@ErrorType", request.ErrorType ?? "UnknownError");
        command.Parameters.AddWithValue("@Severity", request.Severity);
        command.Parameters.AddWithValue("@ErrorSource", request.ErrorSource ?? "Backend");
        command.Parameters.AddWithValue("@StackTrace", request.StackTrace ?? "");
        command.Parameters.AddWithValue("@HttpMethod", request.HttpMethod ?? "");
        command.Parameters.AddWithValue("@UserAgent", request.UserAgent ?? "");
        command.Parameters.AddWithValue("@IpAddress", request.IpAddress ?? "");

        var errorLogId = Convert.ToInt32(await command.ExecuteScalarAsync());

        return new { 
            success = true, 
            message = "Log ingested successfully", 
            errorLogId = errorLogId 
        };
    }

    private async Task<int> GetOrCreateApplicationAsync(SqlConnection connection, string appName)
    {
        var checkQuery = "SELECT Id FROM Applications WHERE AppName = @AppName";
        using var checkCommand = new SqlCommand(checkQuery, connection);
        checkCommand.Parameters.AddWithValue("@AppName", appName);
        
        var existingId = await checkCommand.ExecuteScalarAsync();
        if (existingId != null)
        {
            return Convert.ToInt32(existingId);
        }

        var appKey = GenerateAppKey(appName);
        var insertQuery = @"
            INSERT INTO Applications (AppName, AppKey, Description, IsActive, CreatedAt)
            VALUES (@AppName, @AppKey, @Description, 1, GETDATE());
            SELECT SCOPE_IDENTITY();";

        using var insertCommand = new SqlCommand(insertQuery, connection);
        insertCommand.Parameters.AddWithValue("@AppName", appName);
        insertCommand.Parameters.AddWithValue("@AppKey", appKey);
        insertCommand.Parameters.AddWithValue("@Description", $"Auto-created for {appName}");

        return Convert.ToInt32(await insertCommand.ExecuteScalarAsync());
    }

    private static string GenerateAppKey(string appName)
    {
        var prefix = appName.ToUpper().Replace(" ", "").Substring(0, Math.Min(4, appName.Length));
        var year = DateTime.Now.Year;
        var random = new Random().Next(100, 999);
        return $"{prefix}-{year}-{random:D3}";
    }
}

public class LogIngestionRequest
{
    public string ApplicationName { get; set; } = string.Empty;
    public string ApiEndpoint { get; set; } = string.Empty;
    public string ErrorMessage { get; set; } = string.Empty;
    public string StackTrace { get; set; } = string.Empty;
    public string Severity { get; set; } = string.Empty;
    public int? HttpStatusCode { get; set; }
    public string UserAgent { get; set; } = string.Empty;
    public string IpAddress { get; set; } = string.Empty;
    public string HttpMethod { get; set; } = string.Empty;
    public string ErrorType { get; set; } = string.Empty;
    public string ErrorSource { get; set; } = string.Empty;
}

public class BulkLogIngestionRequest
{
    public List<LogIngestionRequest> ErrorLogs { get; set; }
}