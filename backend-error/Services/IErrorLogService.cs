using ErrorMonitoringAPI.DTOs;
using ErrorMonitoringAPI.Models;

namespace ErrorMonitoringAPI.Services
{
    public interface IErrorLogService
    {
        Task<List<ErrorLogDto>> GetErrorLogsAsync(string severity = null, string status = null, int page = 1, int pageSize = 20, int? userId = null, string userRole = null);
        Task<int> LogErrorAsync(ErrorLogRequest request);
        Task<bool> ResolveErrorAsync(int id);
        Task<bool> DeleteErrorAsync(int id);
        Task<Application> GetOrCreateApplicationAsync(string appName);
        Task CreateAlertAsync(int errorLogId, string severity, string errorMessage, int applicationId, string applicationName);
    }
}