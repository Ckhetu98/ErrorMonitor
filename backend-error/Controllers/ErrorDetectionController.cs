using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ErrorDetectionController(IConfiguration configuration) : ControllerBase
{
    private readonly string _connectionString = configuration.GetConnectionString("DefaultConnection");

    [HttpPost("analyze")]
    public async Task<IActionResult> AnalyzeError([FromBody] ErrorAnalysisRequest request)
    {
        try
        {
            var severity = DetermineSeverity(request.ErrorMessage, request.StackTrace);
            var isAnomaly = DetectAnomalyInternal(request);
            var similarErrors = await GetSimilarErrorsAsync(request.ErrorMessage);

            var response = new
            {
                IsAnomaly = isAnomaly,
                SeverityLevel = severity,
                RecommendedAction = GetRecommendedAction(severity),
                SimilarErrors = similarErrors
            };

            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500, new
            {
                message = "Error analysis failed",
                error = ex.Message
            });
        }
    }

    [HttpPost("detect-anomaly")]
    public IActionResult DetectAnomaly([FromBody] ErrorAnalysisRequest request)
    {
        try
        {
            bool isAnomaly = DetectAnomalyInternal(request);
            return Ok(new { isAnomaly });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new
            {
                message = "Anomaly detection failed",
                error = ex.Message
            });
        }
    }

    [HttpGet("similar/{errorMessage}")]
    public async Task<IActionResult> GetSimilarErrors(string errorMessage)
    {
        var errors = await GetSimilarErrorsAsync(errorMessage);
        return Ok(errors);
    }

    [HttpGet("patterns")]
    public async Task<IActionResult> GetErrorPatterns()
    {
        using var connection = new SqlConnection(_connectionString);
        await connection.OpenAsync();

        var query = """
            SELECT ErrorType, COUNT(*) AS Count, Severity,
                   MIN(CreatedAt) AS FirstOccurrence,
                   MAX(CreatedAt) AS LastOccurrence
            FROM ErrorLogs
            WHERE CreatedAt >= DATEADD(day, -7, GETDATE())
            GROUP BY ErrorType, Severity
            ORDER BY Count DESC
        """;

        using var command = new SqlCommand(query, connection);
        using var reader = await command.ExecuteReaderAsync();

        var patterns = new List<object>();

        while (await reader.ReadAsync())
        {
            patterns.Add(new
            {
                ErrorType = reader.GetString(reader.GetOrdinal("ErrorType")),
                Count = reader.GetInt32(reader.GetOrdinal("Count")),
                Severity = reader.GetString(reader.GetOrdinal("Severity")),
                FirstOccurrence = reader.GetDateTime(reader.GetOrdinal("FirstOccurrence")),
                LastOccurrence = reader.GetDateTime(reader.GetOrdinal("LastOccurrence"))
            });
        }

        return Ok(patterns);
    }

    private static bool DetectAnomalyInternal(ErrorAnalysisRequest request)
    {
        var keywords = new[]
        {
            "timeout", "connection", "database",
            "payment", "security", "authentication"
        };

        var message = request.ErrorMessage?.ToLower() ?? "";
        var stack = request.StackTrace?.ToLower() ?? "";

        return keywords.Any(k => message.Contains(k) || stack.Contains(k));
    }

    private static string DetermineSeverity(string errorMessage, string stackTrace)
    {
        var msg = errorMessage?.ToLower() ?? "";
        var stk = stackTrace?.ToLower() ?? "";

        if (msg.Contains("fatal") || msg.Contains("security") || msg.Contains("payment"))
            return "Critical";

        if (msg.Contains("timeout") || msg.Contains("database") || stk.Contains("sqlexception"))
            return "High";

        if (msg.Contains("validation") || msg.Contains("not found"))
            return "Medium";

        return "Low";
    }

    private async Task<List<string>> GetSimilarErrorsAsync(string errorMessage)
    {
        using var connection = new SqlConnection(_connectionString);
        await connection.OpenAsync();

        var query = """
            SELECT TOP 5 ErrorMessage, CreatedAt
            FROM ErrorLogs
            WHERE ErrorMessage LIKE @Pattern
            ORDER BY CreatedAt DESC
        """;

        using var command = new SqlCommand(query, connection);
        command.Parameters.AddWithValue("@Pattern", $"%{errorMessage.Split(' ')[0]}%");

        using var reader = await command.ExecuteReaderAsync();

        var result = new List<string>();
        while (await reader.ReadAsync())
        {
            var timeAgo = DateTime.Now - reader.GetDateTime(1);
            result.Add($"Similar error occurred {timeAgo.Days} days ago");
        }

        return result;
    }

    private static string GetRecommendedAction(string severity)
    {
        return severity switch
        {
            "Critical" => "Immediate investigation required. Alert on-call team.",
            "High" => "Investigate within 1 hour.",
            "Medium" => "Monitor during business hours.",
            "Low" => "Log and monitor.",
            _ => "Monitor."
        };
    }
}

public class ErrorAnalysisRequest
{
    public string ApplicationName { get; set; } = string.Empty;
    public string ApiEndpoint { get; set; } = string.Empty;
    public string ErrorMessage { get; set; } = string.Empty;
    public string StackTrace { get; set; } = string.Empty;
    public string Severity { get; set; } = string.Empty;
    public int? HttpStatusCode { get; set; }
    public string UserAgent { get; set; } = string.Empty;
    public string IpAddress { get; set; } = string.Empty;
}
