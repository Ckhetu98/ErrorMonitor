using ErrorMonitoringAPI.Models;

namespace ErrorMonitoringAPI.Services
{
    public interface IAlertService
    {
        Task<List<Alert>> GetAllAlertsAsync(int? userId = null, string userRole = null);
        Task<Alert> GetAlertByIdAsync(int id);
        Task<int> CreateAlertAsync(Alert alert);
        Task<bool> UpdateAlertAsync(Alert alert);
        Task<bool> ResolveAlertAsync(int id);
        Task<List<Alert>> GetUnresolvedAlertsAsync(int? userId = null, string userRole = null);
        Task<object> GetAlertStatsAsync(int? userId = null, string userRole = null);
        Task<bool> DeleteAlertAsync(int id);
    }
}