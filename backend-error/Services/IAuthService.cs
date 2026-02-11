using ErrorMonitoringAPI.DTOs;
using ErrorMonitoringAPI.Models;

namespace ErrorMonitoringAPI.Services
{
    public interface IAuthService
    {
        Task<UserDto?> ValidateUserAsync(string username, string password);
        Task<UserDto> GetOrCreateGoogleUserAsync(string email, string name, string picture);
        Task<UserDto?> GetUserByIdAsync(int userId);
        Task<UserDto?> GetUserByUsernameAsync(string username);
        Task<UserDto?> GetUserByEmailAsync(string email);
        Task<UserDto> CreateUserAsync(string username, string email, string password, string role, string firstName = "", string lastName = "");
        string GenerateJwtToken(UserDto user);
        Task LogAuditActionAsync(int userId, string action, string entityType, string entityId, string ipAddress, string userAgent);
        Task<bool> Enable2FAAsync(int userId);
        Task<bool> Disable2FAAsync(int userId);
        Task<bool> ToggleGlobal2FAAsync(bool enabled);
        Task<bool> GetGlobal2FAStatusAsync();
        Task<bool> Is2FARequiredAsync(int userId);
        Task StoreOTPAsync(int userId, string otp);
        Task<bool> VerifyOTPAsync(int userId, string otp);
        Task ClearOTPAsync(int userId);
    }
}