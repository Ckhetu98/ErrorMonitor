using ErrorMonitoringAPI.DTOs;

namespace ErrorMonitoringAPI.Services
{
    public interface IApplicationService
    {
        Task<List<ApplicationDto>> GetAllApplicationsAsync(int? userId = null, string userRole = null);
        Task<ApplicationDto> GetApplicationByIdAsync(int id);
        Task<int> CreateApplicationAsync(ApplicationRequest request, int userId);
        Task<bool> UpdateApplicationAsync(int id, ApplicationRequest request);
        Task<bool> DeleteApplicationAsync(int id);
        Task<object> CheckApplicationHealthAsync(int id);
        Task<bool> IsApplicationOwnerAsync(int applicationId, int userId);
        Task<bool> PauseApplicationAsync(int id);
        Task<bool> ResumeApplicationAsync(int id);
    }
}