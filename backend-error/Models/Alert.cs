using System.ComponentModel.DataAnnotations;

namespace ErrorMonitoringAPI.Models;

public class Alert
{
    public int Id { get; set; }
    
    public int? ApplicationId { get; set; } // Allow null to match Spring Boot
    
    [Required(ErrorMessage = "Alert name is required")]
    [StringLength(100, ErrorMessage = "Alert name cannot exceed 100 characters")]
    public string Name { get; set; } = string.Empty;
    
    [StringLength(500, ErrorMessage = "Description cannot exceed 500 characters")]
    public string? Description { get; set; }
    
    [Required(ErrorMessage = "Condition is required")]
    [StringLength(1000, ErrorMessage = "Condition cannot exceed 1000 characters")]
    public string Condition { get; set; } = string.Empty;
    
    [Required(ErrorMessage = "Recipients are required")]
    [StringLength(1000, ErrorMessage = "Recipients cannot exceed 1000 characters")]
    public string Recipients { get; set; } = string.Empty;
    
    // Additional fields to match Spring Boot Alert model
    public string? ErrorLogId { get; set; }
    
    [StringLength(50)]
    public string AlertType { get; set; } = "EMAIL";
    
    [StringLength(20)]
    public string AlertLevel { get; set; } = "HIGH";
    
    [StringLength(2000)]
    public string? AlertMessage { get; set; }
    
    public bool IsActive { get; set; } = true;
    
    public bool IsResolved { get; set; } = false;
    
    [Required]
    public DateTime CreatedAt { get; set; }
    
    public DateTime? ResolvedAt { get; set; }
    
    [Required]
    [Range(1, int.MaxValue, ErrorMessage = "CreatedBy must be a valid user ID")]
    public int CreatedBy { get; set; }
    
    // Navigation property
    public Application? Application { get; set; }
}