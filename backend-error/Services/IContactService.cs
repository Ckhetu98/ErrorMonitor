using ErrorMonitoringAPI.DTOs;

namespace ErrorMonitoringAPI.Services
{
    public interface IContactService
    {
        Task<int> SubmitContactQueryAsync(ContactSubmissionRequest request, string ipAddress, string userAgent);
        Task<List<ContactDto>> GetContactQueriesAsync();
        Task<bool> UpdateContactStatusAsync(int id, string status, string responseMessage = null, int? assignedTo = null);
        Task<ContactDto> GetContactQueryByIdAsync(int id);
        Task<object> GetContactStatsAsync();
    }
}