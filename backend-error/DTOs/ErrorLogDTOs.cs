namespace ErrorMonitoringAPI.DTOs;

public class ErrorLogDto
{
    public int Id { get; set; }
    public string Application { get; set; } = string.Empty;
    public string? Endpoint { get; set; }
    public string? HttpMethod { get; set; }
    public string ErrorMessage { get; set; } = string.Empty;
    public string? StackTrace { get; set; }
    public string? ErrorType { get; set; }
    public string? ErrorSource { get; set; }
    public string Severity { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public string CreatedAt { get; set; } = string.Empty;
    public string? ResolvedAt { get; set; }
    public int? HttpStatusCode { get; set; }
    public string? UserAgent { get; set; }
    public string? IpAddress { get; set; }
}

public class ErrorLogRequest
{
    public string ApplicationName { get; set; } = string.Empty;
    public string? ApiKey { get; set; }
    public string? ApiEndpoint { get; set; }
    public string? HttpMethod { get; set; }
    public string ErrorMessage { get; set; } = string.Empty;
    public string? StackTrace { get; set; }
    public string? ErrorType { get; set; }
    public string? ErrorSource { get; set; }
    public string Severity { get; set; } = "Medium";
    public int? HttpStatusCode { get; set; }
    public string? UserAgent { get; set; }
    public string? IpAddress { get; set; }
}
