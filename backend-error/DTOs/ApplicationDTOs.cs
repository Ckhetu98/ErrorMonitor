namespace ErrorMonitoringAPI.DTOs;

public class ApplicationDto
{
    public int Id { get; set; }
    public string name { get; set; } = string.Empty;
    public string description { get; set; } = string.Empty;
    public string technology { get; set; } = string.Empty;
    public string version { get; set; } = string.Empty;
    public string baseUrl { get; set; } = string.Empty;
    public string apiKey { get; set; } = string.Empty;
    public string status { get; set; } = string.Empty;
    public bool isActive { get; set; }
    public bool isPaused { get; set; }
    public string healthCheckUrl { get; set; } = string.Empty;
    public string createdAt { get; set; } = string.Empty;
    public int errorCount { get; set; }
    public int openErrors { get; set; }
    public int criticalErrors { get; set; }
    public string healthStatus { get; set; } = string.Empty;
}

public class ApplicationRequest
{
    public string name { get; set; } = string.Empty;
    public string description { get; set; } = string.Empty;
    public string technology { get; set; } = string.Empty;
    public string version { get; set; } = string.Empty;
    public string baseUrl { get; set; } = string.Empty;
    public string status { get; set; } = string.Empty;
    public bool isActive { get; set; } = true;
    public string healthCheckUrl { get; set; } = string.Empty;
}
