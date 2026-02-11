using System.ComponentModel.DataAnnotations;

namespace ErrorMonitoringAPI.Models;

public class ErrorLog
{
    public int Id { get; set; }
    
    [Required]
    [Range(1, int.MaxValue, ErrorMessage = "ApplicationId must be a valid application ID")]
    public int ApplicationId { get; set; }
    
    [Required(ErrorMessage = "Error message is required")]
    [StringLength(2000, ErrorMessage = "Error message cannot exceed 2000 characters")]
    public string Message { get; set; } = string.Empty;
    
    [StringLength(5000, ErrorMessage = "Stack trace cannot exceed 5000 characters")]
    public string? StackTrace { get; set; }
    
    [StringLength(200, ErrorMessage = "Source cannot exceed 200 characters")]
    public string? Source { get; set; }
    
    [Required(ErrorMessage = "Severity is required")]
    [RegularExpression("^(Low|Medium|High|Critical)$", ErrorMessage = "Severity must be Low, Medium, High, or Critical")]
    public string Severity { get; set; } = string.Empty;
    
    [StringLength(50, ErrorMessage = "Status cannot exceed 50 characters")]
    public string? Status { get; set; } = "Open";
    
    [StringLength(50, ErrorMessage = "Environment cannot exceed 50 characters")]
    public string? Environment { get; set; }
    
    [StringLength(500, ErrorMessage = "User agent cannot exceed 500 characters")]
    public string? UserAgent { get; set; }
    
    [StringLength(45, ErrorMessage = "IP address cannot exceed 45 characters")]
    public string? IpAddress { get; set; }
    
    [StringLength(10, ErrorMessage = "HTTP method cannot exceed 10 characters")]
    public string? HttpMethod { get; set; }
    
    [StringLength(500, ErrorMessage = "API endpoint cannot exceed 500 characters")]
    public string? ApiEndpoint { get; set; }
    
    [StringLength(100, ErrorMessage = "Error type cannot exceed 100 characters")]
    public string? ErrorType { get; set; }
    
    [Required]
    public DateTime CreatedAt { get; set; }
    
    public DateTime? ResolvedAt { get; set; }
    
    // Navigation property
    public Application? Application { get; set; }
}