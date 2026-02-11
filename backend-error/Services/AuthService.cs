using ErrorMonitoringAPI.DTOs;
using ErrorMonitoringAPI.Data;
using ErrorMonitoringAPI.Models;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using BCrypt.Net;

namespace ErrorMonitoringAPI.Services;

public class AuthService : IAuthService
{
    private readonly ApplicationDbContext _context;
    private readonly string _jwtKey = "iSpHjPNfLNeHico5fh7W1o5UzINBp48mb7PVycs2Wcy";

    public AuthService(ApplicationDbContext context)
    {
        _context = context;
    }

    // =========================
    // LOGIN (LOCAL USERS ONLY)
    // =========================
    public async Task<UserDto?> ValidateUserAsync(string username, string password)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Username == username);

        if (user == null || !user.IsActive)
            return null;

        // Block Google SSO users from password login
        if (user.AuthProvider == "GOOGLE")
            throw new InvalidOperationException("Use Google login");

        if (string.IsNullOrEmpty(user.PasswordHash))
            return null;

        if (!VerifyPassword(password, user.PasswordHash))
            return null;

        user.LastLoginAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();

        return MapToDto(user);
    }

    // Check if 2FA is required for this user (individual or global setting)
    public async Task<bool> Is2FARequiredAsync(int userId)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user == null) return false;
        
        // Check individual user setting first
        if (user.TwoFactorEnabled) return true;
        
        // Check global setting
        try
        {
            var globalUser = await _context.Users.FirstOrDefaultAsync(u => u.Username == "GLOBAL_SETTINGS");
            return globalUser?.TwoFactorEnabled ?? false;
        }
        catch
        {
            return false;
        }
    }

    //google single sign on
    public async Task<UserDto> GetOrCreateGoogleUserAsync(
        string email,
        string name,
        string picture)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Email == email);

        if (user != null)
        {
            if (!user.IsActive)
                throw new UnauthorizedAccessException("Account disabled");

            // Update last login for existing users
            user.LastLoginAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return MapToDto(user);
        }

        // Create new Google user
        var parts = name?.Split(' ', 2) ?? new[] { "User", "" };
        var baseUsername = email.Split('@')[0];
        
        // Ensure username is valid and unique
        var username = baseUsername;
        var counter = 1;
        while (await _context.Users.AnyAsync(u => u.Username == username))
        {
            username = $"{baseUsername}{counter}";
            counter++;
        }
        
        // Ensure username meets length requirements (3-50 chars)
        if (username.Length < 3)
        {
            username = $"user{DateTime.UtcNow.Ticks % 10000}";
        }
        if (username.Length > 50)
        {
            username = username.Substring(0, 47) + counter.ToString();
        }

        var newUser = new User
        {
            Username = username,
            Email = email,
            PasswordHash = null,           // No password for Google users
            AuthProvider = "GOOGLE",
            Role = "DEVELOPER",            // Valid role from RegEx
            FirstName = parts[0] ?? "User",
            LastName = parts.Length > 1 ? parts[1] ?? "" : "",
            IsActive = true,
            TwoFactorEnabled = false,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            LastLoginAt = DateTime.UtcNow
        };

        try
        {
            _context.Users.Add(newUser);
            await _context.SaveChangesAsync();
            return MapToDto(newUser);
        }
        catch (Exception ex)
        {
            // Log the specific database error
            Console.WriteLine($"Database save error: {ex.Message}");
            if (ex.InnerException != null)
            {
                Console.WriteLine($"Inner exception: {ex.InnerException.Message}");
            }
            throw new Exception($"Failed to create Google user: {ex.Message}", ex);
        }
    }

    //local users
    public async Task<UserDto> CreateUserAsync(
        string username,
        string email,
        string password,
        string role,
        string firstName = "",
        string lastName = "")
    {
        if (await _context.Users.AnyAsync(u => u.Email == email))
            throw new InvalidOperationException("Email already registered");

        if (await _context.Users.AnyAsync(u => u.Username == username))
            throw new InvalidOperationException("Username already taken");

        var user = new User
        {
            Username = username,
            Email = email,
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(password, 12),
            AuthProvider = "LOCAL",
            Role = role,
            FirstName = firstName,
            LastName = lastName,
            IsActive = true,
            TwoFactorEnabled = false,
            CreatedAt = DateTime.UtcNow
        };

        _context.Users.Add(user);
        await _context.SaveChangesAsync();

        return MapToDto(user);
    }

    //get the user from database
    public async Task<UserDto?> GetUserByIdAsync(int userId)
    {
        var user = await _context.Users.FindAsync(userId);
        return user == null ? null : MapToDto(user);
    }

    public async Task<UserDto?> GetUserByUsernameAsync(string username)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Username == username);
        return user == null ? null : MapToDto(user);
    }

    public async Task<UserDto?> GetUserByEmailAsync(string email)
    {
        Console.WriteLine($"Looking up user by email: {email}");
        
        // Try exact match first (case-insensitive)
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Email.ToLower() == email.ToLower());
        
        // If no exact match and email contains 'choudharykhetesh', try partial match
        if (user == null && email.ToLower().Contains("choudharykhetesh"))
        {
            user = await _context.Users
                .FirstOrDefaultAsync(u => u.Email.ToLower().Contains("choudharykhetesh"));
            Console.WriteLine($"Partial match found: {user?.Email}");
        }
        
        Console.WriteLine($"User lookup result: {(user != null ? $"Found user {user.Username} (ID: {user.Id}, Role: {user.Role}, Email: {user.Email})" : "No user found")}");
        return user == null ? null : MapToDto(user);
    }

    //generate jwt tokens for authentication 
    public string GenerateJwtToken(UserDto user)
    {
        var key = Encoding.UTF8.GetBytes(_jwtKey);

        var claims = new[]
        {
            new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
            new Claim(ClaimTypes.Name, user.Username),
            new Claim(ClaimTypes.Email, user.Email),
            new Claim(ClaimTypes.Role, user.Role)
        };

        var token = new JwtSecurityToken(
            issuer: "CodeErrorMonitoring",
            audience: "CodeErrorMonitoring-Users",
            claims: claims,
            expires: DateTime.UtcNow.AddHours(24),
            signingCredentials: new SigningCredentials(
                new SymmetricSecurityKey(key),
                SecurityAlgorithms.HmacSha256)
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    //enabling  2factor authentication
    public async Task<bool> Enable2FAAsync(int userId)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user == null) return false;

        user.TwoFactorEnabled = true;
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> Disable2FAAsync(int userId)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user == null) return false;

        user.TwoFactorEnabled = false;
        return await _context.SaveChangesAsync() > 0;
    }

    public async Task<bool> ToggleGlobal2FAAsync(bool enabled)
    {
        try
        {
            var globalUser = await _context.Users.FirstOrDefaultAsync(u => u.Username == "GLOBAL_SETTINGS");
            
            if (globalUser == null)
            {
                globalUser = new User
                {
                    Username = "GLOBAL_SETTINGS",
                    Email = "global@system.internal",
                    PasswordHash = "N/A",
                    AuthProvider = "SYSTEM",
                    Role = "SYSTEM",
                    FirstName = "Global",
                    LastName = "Settings",
                    IsActive = true,
                    TwoFactorEnabled = enabled,
                    CreatedAt = DateTime.UtcNow,
                    UpdatedAt = DateTime.UtcNow
                };
                _context.Users.Add(globalUser);
            }
            else
            {
                globalUser.TwoFactorEnabled = enabled;
                globalUser.UpdatedAt = DateTime.UtcNow;
            }
            
            await _context.SaveChangesAsync();
            return true;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error in ToggleGlobal2FAAsync: {ex.Message}");
            throw;
        }
    }

    public async Task<bool> GetGlobal2FAStatusAsync()
    {
        try
        {
            var globalUser = await _context.Users.FirstOrDefaultAsync(u => u.Username == "GLOBAL_SETTINGS");
            return globalUser?.TwoFactorEnabled ?? false;
        }
        catch
        {
            return false;
        }
    }

    //otp store
    public async Task StoreOTPAsync(int userId, string otp)
    {
        var existing = await _context.UserOTPs
            .FirstOrDefaultAsync(o => o.UserId == userId);

        if (existing != null)
        {
            existing.OTP = otp;
            existing.CreatedAt = DateTime.UtcNow;
        }
        else
        {
            _context.UserOTPs.Add(new UserOTP
            {
                UserId = userId,
                OTP = otp,
                CreatedAt = DateTime.UtcNow
            });
        }

        await _context.SaveChangesAsync();
    }

    public async Task<bool> VerifyOTPAsync(int userId, string otp)
    {
        var record = await _context.UserOTPs
            .FirstOrDefaultAsync(o => o.UserId == userId);

        if (record == null) return false;

        return record.OTP == otp &&
               DateTime.UtcNow <= record.CreatedAt.AddMinutes(5);
    }

    public async Task ClearOTPAsync(int userId)
    {
        var record = await _context.UserOTPs
            .FirstOrDefaultAsync(o => o.UserId == userId);

        if (record != null)
        {
            _context.UserOTPs.Remove(record);
            await _context.SaveChangesAsync();
        }
    }

   //log audit for every task
    public async Task LogAuditActionAsync(
        int userId,
        string action,
        string entityType,
        string entityId,
        string ipAddress,
        string userAgent)
    {
        _context.AuditLogs.Add(new AuditLog
        {
            UserId = userId,
            Action = action,
            EntityType = entityType,
            EntityId = entityId,
            IpAddress = ipAddress ?? "",
            UserAgent = userAgent ?? "",
            CreatedAt = DateTime.UtcNow
        });

        await _context.SaveChangesAsync();
    }

  //mapping DTO
    private static UserDto MapToDto(User user)
    {
        return new UserDto
        {
            Id = user.Id,
            Username = user.Username,
            Email = user.Email,
            Role = user.Role,
            FirstName = user.FirstName ?? "",
            LastName = user.LastName ?? "",
            IsActive = user.IsActive,
            TwoFactorEnabled = user.TwoFactorEnabled
        };
    }

   //password verify karna hai
    private static bool VerifyPassword(string password, string storedHash)
    {
        try
        {
            // Try BCrypt first (new format)
            if (BCrypt.Net.BCrypt.Verify(password, storedHash))
                return true;
        }
        catch
        {
            // BCrypt failed, try legacy format
        }

        // Fallback to plain text (for existing users)
        return storedHash == password;
    }
}
