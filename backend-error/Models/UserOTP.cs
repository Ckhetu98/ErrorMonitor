using System.ComponentModel.DataAnnotations;

namespace ErrorMonitoringAPI.Models;

public class UserOTP
{
    public int Id { get; set; }
    
    [Required]
    [Range(1, int.MaxValue, ErrorMessage = "UserId must be a valid user ID")]
    public int UserId { get; set; }
    
    [Required(ErrorMessage = "OTP is required")]
    [StringLength(6, MinimumLength = 6, ErrorMessage = "OTP must be exactly 6 characters")]
    [RegularExpression("^[0-9]{6}$", ErrorMessage = "OTP must be a 6-digit number")]
    public string OTP { get; set; } = string.Empty;
    
    [Required]
    public DateTime CreatedAt { get; set; }
    
    // Navigation property
    public User? User { get; set; }
}