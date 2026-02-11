using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace ErrorMonitoringAPI.Models;

public class Application
{
    public int Id { get; set; }
    
    [Required(ErrorMessage = "Application name is required")]
    [StringLength(100, ErrorMessage = "Application name cannot exceed 100 characters")]
    [Column("Name")]
    public string Name { get; set; } = string.Empty;
    
    [StringLength(500, ErrorMessage = "Description cannot exceed 500 characters")]
    [Column("Description")]
    public string? Description { get; set; }
    
    [StringLength(50, ErrorMessage = "Technology cannot exceed 50 characters")]
    public string? Technology { get; set; }
    
    [StringLength(20, ErrorMessage = "Version cannot exceed 20 characters")]
    public string? Version { get; set; }
    
    [StringLength(200, ErrorMessage = "Base URL cannot exceed 200 characters")]
    public string? BaseUrl { get; set; }
    
    [StringLength(200, ErrorMessage = "Health check URL cannot exceed 200 characters")]
    public string? HealthCheckUrl { get; set; }
    
    [StringLength(100, ErrorMessage = "API key cannot exceed 100 characters")]
    public string? ApiKey { get; set; }
    
    public bool IsActive { get; set; } = true;
    
    public bool IsPaused { get; set; } = false;
    
    [Required]
    public DateTime CreatedAt { get; set; }
    
    public DateTime? UpdatedAt { get; set; }
    
    [Required]
    [Range(1, int.MaxValue, ErrorMessage = "CreatedBy must be a valid user ID")]
    public int CreatedBy { get; set; }
    
    // Navigation properties
    public ICollection<ErrorLog> ErrorLogs { get; set; } = new List<ErrorLog>();
}