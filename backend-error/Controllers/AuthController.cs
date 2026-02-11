using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using ErrorMonitoringAPI.Services;
using ErrorMonitoringAPI.Models;
using ErrorMonitoringAPI.DTOs;
using ErrorMonitoringAPI.Data;
using Microsoft.EntityFrameworkCore;
using System.Security.Cryptography;
using System.Net.Http.Headers;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController(IAuthService authService, IEmailSenderService emailSender, IConfiguration configuration, ApplicationDbContext context) : ControllerBase
{
    private readonly ApplicationDbContext _context = context;

  // login any users
    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        try
        {
            var user = await authService.ValidateUserAsync(request.Username, request.Password);
            if (user == null)
            {
                await LogAuditAsync(null, "LOGIN_FAILED", "User", request.Username, null, null);
                return Unauthorized(new { message = "Invalid credentials" });
            }

            // Check if 2FA is required (individual or global setting)
            var requires2FA = await authService.Is2FARequiredAsync(user.Id);
            if (requires2FA)
            {
                var otp = GenerateOTP();
                await authService.StoreOTPAsync(user.Id, otp);

                try
                {
                    await emailSender.SendEmailAsync(
                        user.Email,
                        "Your Login OTP Code",
                        $"<h3>Your OTP: {otp}</h3><p>Valid for 5 minutes</p>");
                    
                    Console.WriteLine($"OTP email sent successfully to {user.Email}");
                }
                catch (Exception emailEx)
                {
                    Console.WriteLine($"Email sending failed: {emailEx.Message}");
                    Console.WriteLine($"Email stack trace: {emailEx.StackTrace}");
                    return StatusCode(500, new { message = "Failed to send OTP email", error = emailEx.Message });
                }

                return Ok(new
                {
                    requiresTwoFactor = true,
                    userId = user.Id.ToString(),
                    userEmail = user.Email
                });
            }

            var token = authService.GenerateJwtToken(user);
            await LogAuditAsync(user.Id, "LOGIN", "User", user.Id.ToString(), null, null);

            return Ok(new
            {
                token,
                user = new {
                    id = user.Id,
                    username = user.Username,
                    email = user.Email,
                    role = user.Role.ToUpper(),
                    firstName = user.FirstName,
                    lastName = user.LastName,
                    isActive = user.IsActive,
                    twoFactorEnabled = user.TwoFactorEnabled
                },
                expiresAt = DateTime.UtcNow.AddHours(24).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
            });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Login error: {ex.Message}");
            return StatusCode(500, new { message = "Login failed", error = ex.Message });
        }
    }

//register for user
    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
    {
        try
        {
            // Validate confirm password
            if (request.Password != request.ConfirmPassword)
            {
                return BadRequest(new { message = "Passwords do not match" });
            }

            var user = await authService.CreateUserAsync(
                request.Username,
                request.Email,
                request.Password,
                request.Role ?? "VIEWER",
                request.FirstName,
                request.LastName);

            return Ok(new
            {
                message = "Registration successful",
                user
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Registration failed", error = ex.Message });
        }
    }

   

    //verifying the otp after enabling 2fa and login pause karke 
    [HttpPost("verify-otp")]
    public async Task<IActionResult> VerifyOTP([FromBody] VerifyOtpRequest request)
    {
        if (!int.TryParse(request.UserId, out int userId))
            return BadRequest("Invalid user");

        if (!await authService.VerifyOTPAsync(userId, request.Code))
        {
            await LogAuditAsync(userId, "OTP_VERIFICATION_FAILED", "User", userId.ToString(), null, null);
            return Unauthorized("Invalid or expired OTP");
        }

        await authService.ClearOTPAsync(userId);

        var user = await authService.GetUserByIdAsync(userId);
        await LogAuditAsync(userId, "LOGIN", "User", userId.ToString(), null, null);
        var token = authService.GenerateJwtToken(user);

        return Ok(new
        {
            token,
            user = new {
                id = user.Id,
                username = user.Username,
                email = user.Email,
                role = user.Role.ToUpper(),
                firstName = user.FirstName,
                lastName = user.LastName,
                isActive = user.IsActive,
                twoFactorEnabled = user.TwoFactorEnabled
            },
            expiresAt = DateTime.UtcNow.AddHours(24).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        });
    }

    //enable the 2fa only by admin
    [HttpPost("enable-2fa")]
    public async Task<IActionResult> Enable2FA([FromBody] Enable2FARequest request)
    {
        try
        {
            var success = await authService.Enable2FAAsync(request.UserId);
            return success 
                ? Ok(new { message = "2FA enabled successfully" }) 
                : NotFound(new { message = "User not found" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to enable 2FA", error = ex.Message });
        }
    }

    [HttpPost("disable-2fa")]
    public async Task<IActionResult> Disable2FA([FromBody] Enable2FARequest request)
    {
        try
        {
            var success = await authService.Disable2FAAsync(request.UserId);
            return success 
                ? Ok(new { message = "2FA disabled successfully" }) 
                : NotFound(new { message = "User not found" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to disable 2FA", error = ex.Message });
        }
    }

    //global
    [HttpPost("toggle-global-2fa")]
    [Authorize]
    public async Task<IActionResult> ToggleGlobal2FA([FromBody] GlobalToggle2FARequest request)
    {
        try
        {
            // Debug: Log all claims
            Console.WriteLine("=== JWT Claims Debug ===");
            foreach (var claim in HttpContext.User.Claims)
            {
                Console.WriteLine($"Claim Type: {claim.Type}, Value: {claim.Value}");
            }
            
            // Check if user is authenticated and is an admin
            var userRole = HttpContext.User.FindFirst(System.Security.Claims.ClaimTypes.Role)?.Value;
            Console.WriteLine($"Extracted user role: {userRole}");
            
            if (userRole?.ToUpper() != "ADMIN")
            {
                Console.WriteLine($"Access denied. Required: ADMIN, Got: {userRole}");
                return StatusCode(403, new { message = "Only administrators can modify global 2FA settings" });
            }

            Console.WriteLine($"Toggling Global 2FA to: {request.Enabled}");
            await authService.ToggleGlobal2FAAsync(request.Enabled);
            var status = request.Enabled ? "enabled" : "disabled";
            
            var response = new { 
                message = $"Global 2FA {status} successfully",
                globalEnabled = request.Enabled,
                enabled = request.Enabled
            };
            
            Console.WriteLine($"Returning response: {System.Text.Json.JsonSerializer.Serialize(response)}");
            return Ok(response);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Toggle Global 2FA error: {ex.Message}");
            Console.WriteLine($"Stack trace: {ex.StackTrace}");
            return StatusCode(500, new { message = "Failed to toggle global 2FA", error = ex.Message });
        }
    }

    [HttpGet("2fa-status")]
    [Authorize]
    public async Task<IActionResult> Get2FAStatus()
    {
        try
        {
            Console.WriteLine("=== Get 2FA Status Called ===");
            var isEnabled = await authService.GetGlobal2FAStatusAsync();
            Console.WriteLine($"Global 2FA Status from DB: {isEnabled}");
            
            var response = new { 
                enabled = isEnabled,
                globalEnabled = isEnabled,
                globalTwoFactorEnabled = isEnabled 
            };
            
            Console.WriteLine($"Returning 2FA status: {System.Text.Json.JsonSerializer.Serialize(response)}");
            return Ok(response);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Get 2FA Status error: {ex.Message}");
            return StatusCode(500, new { message = "Failed to get 2FA status", error = ex.Message });
        }
    }

    [HttpPost("google")]
    public async Task<IActionResult> GoogleAuth([FromBody] GoogleAuthRequest request)
    {
        try
        {
            if (string.IsNullOrEmpty(request.Token) || string.IsNullOrEmpty(request.Email))
            {
                return BadRequest(new { message = "Google token and email are required" });
            }

            var name = request.Name ?? request.Email.Split('@')[0];

            Console.WriteLine($"=== GOOGLE AUTH DEBUG ===");
            Console.WriteLine($"Google login attempt for email: {request.Email}");
            Console.WriteLine($"Google name: {request.Name}");
            
            // Debug: List all users in database
            var allUsers = await _context.Users.Select(u => new { u.Id, u.Email, u.Username, u.Role }).ToListAsync();
            Console.WriteLine($"All users in database:");
            foreach (var u in allUsers)
            {
                Console.WriteLine($"  - ID: {u.Id}, Email: '{u.Email}', Username: {u.Username}, Role: {u.Role}");
            }

            
            // Generic lookup for ALL users 
            UserDto existingUser = null;

            // Normalize email
            string normalizedEmail = request.Email.Trim().ToLower();

            // Extract username part (before @)
            string emailPrefix = normalizedEmail.Split('@')[0];

            var directUser = await _context.Users
                .FirstOrDefaultAsync(u =>
                    // Exact email match
                    u.Email.ToLower() == normalizedEmail ||

                    // Partial email match
                    u.Email.ToLower().Contains(emailPrefix) ||

                    // Username match
                    u.Username.ToLower().Contains(emailPrefix)
                );

            if (directUser != null)
            {
                Console.WriteLine($"MATCH FOUND: {directUser.Username} (ID: {directUser.Id}, Email: {directUser.Email})");

                existingUser = new UserDto
                {
                    Id = directUser.Id,
                    Username = directUser.Username,
                    Email = directUser.Email,
                    Role = directUser.Role,
                    FirstName = directUser.FirstName ?? "",
                    LastName = directUser.LastName ?? "",
                    IsActive = directUser.IsActive,
                    TwoFactorEnabled = directUser.TwoFactorEnabled
                };
            }
            else
            {
                Console.WriteLine("NO MATCH FOUND: Creating new Google user");
            }
            Console.WriteLine($"Existing user lookup result: {(existingUser != null ? $"Found user ID {existingUser.Id}, Role: {existingUser.Role}, Email: {existingUser.Email}" : "No user found")}");
            
            UserDto user;

            if (existingUser != null)
            {
                // Update existing user's last login
                user = existingUser;
                Console.WriteLine($"Google login: Found existing user ID {existingUser.Id} with email {request.Email}");
                var userEntity = await _context.Users.FirstOrDefaultAsync(u => u.Email == request.Email);
                if (userEntity != null)
                {
                    userEntity.LastLoginAt = DateTime.UtcNow;
                    await _context.SaveChangesAsync();
                }
            }
            else
            {
                // Create new user with actual Google data
                Console.WriteLine($"Google login: Creating new user for email {request.Email}");
                user = await authService.GetOrCreateGoogleUserAsync(request.Email, name, "");
            }

            // MANDATORY OTP for Google OAuth users
            var otp = GenerateOTP();
            await authService.StoreOTPAsync(user.Id, otp);

            await emailSender.SendEmailAsync(
                user.Email,
                "Your Login OTP Code",
                $"<h3>Your OTP: {otp}</h3><p>Valid for 5 minutes</p>");

            return Ok(new
            {
                requiresTwoFactor = true,
                userId = user.Id.ToString(),
                message = "Verification code sent to your registered email"
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = $"Google authentication failed: {ex.Message}" });
        }
    }

    [HttpGet("google/callback")]
    public async Task<IActionResult> GoogleCallback([FromQuery] string code, [FromQuery] string state)
    {
        try
        {
            if (string.IsNullOrEmpty(code))
            {
                return Redirect("http://localhost:3000/login?error=no_code");
            }

            // Exchange code for access token
            var clientId = configuration["GoogleAuth:ClientId"];
            var clientSecret = configuration["GoogleAuth:ClientSecret"];
            var redirectUri = "http://localhost:8080/api/auth/google/callback";
            
            var tokenRequest = new
            {
                client_id = clientId,
                client_secret = clientSecret,
                code = code,
                grant_type = "authorization_code",
                redirect_uri = redirectUri
            };
            
            using var httpClient = new HttpClient();
            var tokenResponse = await httpClient.PostAsJsonAsync("https://oauth2.googleapis.com/token", tokenRequest);
            var tokenData = await tokenResponse.Content.ReadFromJsonAsync<GoogleTokenResponse>();
            
            if (tokenData?.AccessToken == null)
            {
                return Redirect("http://localhost:3000/login?error=token_failed");
            }
            
            // Get user info from Google
            httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", tokenData.AccessToken);
            var userResponse = await httpClient.GetAsync("https://www.googleapis.com/oauth2/v2/userinfo");
            var userData = await userResponse.Content.ReadFromJsonAsync<GoogleUserInfo>();
            
            if (userData?.Email == null)
            {
                return Redirect("http://localhost:3000/login?error=userinfo_failed");
            }
            
            Console.WriteLine($"Google OAuth: Got user {userData.Email}");
            
            // Use the actual Google user data
            var existingUser = await authService.GetUserByEmailAsync(userData.Email);
            UserDto user;
            
            if (existingUser != null)
            {
                // Existing user - keep their current role (could be ADMIN)
                user = existingUser;
                Console.WriteLine($"Found existing user: {user.Username} (ID: {user.Id}) with role: {user.Role}");
                
                // Update last login time
                var userEntity = await _context.Users.FirstOrDefaultAsync(u => u.Id == user.Id);
                if (userEntity != null)
                {
                    userEntity.LastLoginAt = DateTime.UtcNow;
                    await _context.SaveChangesAsync();
                }
            }
            else
            {
                // New user - assign DEVELOPER role by default
                Console.WriteLine($"Creating new user for email {userData.Email} with DEVELOPER role");
                user = await authService.GetOrCreateGoogleUserAsync(userData.Email, userData.Name ?? "Google User", userData.Picture ?? "");
                Console.WriteLine($"Created new user: {user.Username} (ID: {user.Id}) with role: {user.Role}");
            }

            // Check if 2FA is required (global setting OR individual user setting)
            var requires2FA = await authService.Is2FARequiredAsync(user.Id);
            
            Console.WriteLine($"=== 2FA CHECK DEBUG ===");
            Console.WriteLine($"User 2FA enabled: {user.TwoFactorEnabled}");
            Console.WriteLine($"Global 2FA required: {requires2FA}");
            Console.WriteLine($"Final 2FA decision: {requires2FA}");
            
            if (requires2FA)
            {
                // 2FA required - send OTP and redirect to OTP page
                Console.WriteLine($"2FA required for Google user, sending OTP");
                var otp = GenerateOTP();
                await authService.StoreOTPAsync(user.Id, otp);
                
                await emailSender.SendEmailAsync(
                    user.Email,
                    "Your Login OTP Code",
                    $"<h3>Your OTP: {otp}</h3><p>Valid for 5 minutes</p>");
                
                // Redirect to OTP verification page with user ID
                var redirectUrl = $"http://localhost:3000/auth/verify-otp?userId={user.Id}&email={user.Email}";
                return Redirect(redirectUrl);
            }
            else
            {
                // No 2FA required - direct login
                Console.WriteLine($"No 2FA required for Google user, direct login");
                var jwtToken = authService.GenerateJwtToken(user);
                return Redirect($"http://localhost:3000/login/success?token={jwtToken}");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Google callback error: {ex.Message}");
            return Redirect("http://localhost:3000/login?error=google_auth_failed");
        }
    }

    [HttpGet("google/login")]
    public IActionResult GoogleLogin()
    {
        // Direct redirect to Google OAuth (matching Spring Boot approach)
        var clientId = configuration["GoogleAuth:ClientId"];
        var redirectUri = "http://localhost:8080/api/auth/google/callback";
        var scope = "email profile";
        var googleAuthUrl = $"https://accounts.google.com/o/oauth2/v2/auth?client_id={clientId}&redirect_uri={redirectUri}&scope={scope}&response_type=code";
        
        return Redirect(googleAuthUrl);
    }

    //resend the otp value
    [HttpPost("resend-otp")]
    public async Task<IActionResult> ResendOTP([FromBody] ResendOtpRequest request)
    {
        try
        {
            if (!int.TryParse(request.UserId, out int userId))
                return BadRequest("Invalid user");

            var user = await authService.GetUserByIdAsync(userId);
            if (user == null)
                return NotFound("User not found");

            var otp = GenerateOTP();
            await authService.StoreOTPAsync(userId, otp);

            try
            {
                await emailSender.SendEmailAsync(
                    user.Email,
                    "Your New Login OTP Code",
                    $"<h3>Your new OTP: {otp}</h3><p>Valid for 5 minutes</p>");
                
                Console.WriteLine($"Resend OTP email sent successfully to {user.Email}");
            }
            catch (Exception emailEx)
            {
                Console.WriteLine($"Resend email failed: {emailEx.Message}");
                Console.WriteLine($"Resend email stack trace: {emailEx.StackTrace}");
                return StatusCode(500, new { message = "Failed to send OTP email", error = emailEx.Message });
            }

            return Ok(new { message = "New OTP sent successfully" });
        }
        catch (Exception ex)
        {
            return StatusCode(500, new { message = "Failed to resend OTP", error = ex.Message });
        }
    }

    //otp helpers to generate otp for 2fa
    private static string GenerateOTP()
    {
        using var rng = RandomNumberGenerator.Create();
        var bytes = new byte[4];
        rng.GetBytes(bytes);
        var number = Math.Abs(BitConverter.ToInt32(bytes, 0)) % 1_000_000;
        return number.ToString("D6");
    }

    private async Task LogAuditAsync(int? userId, string action, string entityType, string? entityId, string? oldValues, string? newValues)
    {
        try
        {
            if (userId.HasValue)
            {
                await authService.LogAuditActionAsync(
                    userId.Value, 
                    action, 
                    entityType, 
                    entityId ?? "", 
                    HttpContext.Connection.RemoteIpAddress?.ToString() ?? "", 
                    HttpContext.Request.Headers.UserAgent.ToString() ?? "");
            }
        }
        catch
        {
            // Ignore audit logging errors to not break main functionality
        }
    }
}

public class GoogleAuthRequest
{
    public string Token { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Name { get; set; }
}

public class ResendOtpRequest
{
    public string UserId { get; set; } = string.Empty;
}

public class LoginRequest
{
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}



public class VerifyOtpRequest
{
    public string UserId { get; set; } = string.Empty;
    public string Code { get; set; } = string.Empty;
}

public class Enable2FARequest
{
    public int UserId { get; set; }
}

public class GlobalToggle2FARequest
{
    public bool Enabled { get; set; }
}

public class GoogleTokenResponse
{
    [JsonPropertyName("access_token")]
    public string AccessToken { get; set; } = string.Empty;
    
    [JsonPropertyName("token_type")]
    public string TokenType { get; set; } = string.Empty;
}

public class GoogleUserInfo
{
    [JsonPropertyName("email")]
    public string Email { get; set; } = string.Empty;
    
    [JsonPropertyName("name")]
    public string Name { get; set; }
    
    [JsonPropertyName("picture")]
    public string Picture { get; set; }
}