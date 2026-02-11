namespace ErrorMonitoringAPI.Services
{
    public interface IEmailSenderService
    {
        Task SendEmailAsync(string email, string subject, string message);
        Task SendAlertEmailAsync(string toEmail, string alertMessage, string severity);
    }
}
