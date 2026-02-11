namespace ErrorMonitoringAPI.Services
{
    public interface IEmailService
    {
        Task<bool> SendContactResponseAsync(string toEmail, string subject, string message, string contactName);
        Task<bool> SendContactConfirmationAsync(string toEmail, string contactName, string subject);
        Task<bool> SendAlertEmailAsync(string toEmail, string alertMessage, string severity);
        Task<bool> SendEmailAsync(string toEmail, string subject, string htmlBody);
    }
}