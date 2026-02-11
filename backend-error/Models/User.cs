using System.ComponentModel.DataAnnotations;

namespace ErrorMonitoringAPI.Models;

public class User
{
    public int Id { get; set; }

    [Required(ErrorMessage = "Username is required")]
    [StringLength(50, MinimumLength = 3)]
    public string Username { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email is required")]
    [EmailAddress]
    [StringLength(100)]
    public string Email { get; set; } = string.Empty;

    // ✅ MUST be nullable for Google SSO users
    public string? PasswordHash { get; set; }

    // ✅ Needed to separate LOCAL vs GOOGLE users
    [Required]
    [StringLength(20)]
    public string AuthProvider { get; set; } = "LOCAL";

    // ✅ Matches roles used in AuthService
    [Required]
    [RegularExpression("^(ADMIN|DEVELOPER|VIEWER)$",
        ErrorMessage = "Role must be ADMIN, DEVELOPER, or VIEWER")]
    public string Role { get; set; } = "VIEWER";

    [StringLength(50)]
    public string? FirstName { get; set; }

    [StringLength(50)]
    public string? LastName { get; set; }

    public bool IsActive { get; set; } = true;
    public bool TwoFactorEnabled { get; set; } = false;

    [Required]
    public DateTime CreatedAt { get; set; }

    public DateTime? UpdatedAt { get; set; }
    public DateTime? LastLoginAt { get; set; }
}
