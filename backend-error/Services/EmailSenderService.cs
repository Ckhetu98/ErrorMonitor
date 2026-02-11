using System.Net;
using System.Net.Mail;

namespace ErrorMonitoringAPI.Services;

public class EmailSenderService(IConfiguration configuration) : IEmailSenderService
{
    private readonly IConfiguration _configuration = configuration;

    public async Task SendEmailAsync(string email, string subject, string message)
    {
        try
        {
            // Get email settings directly from appsettings.json for reliability
            var smtpHost = _configuration["EmailSettings:SmtpHost"] ?? "smtp.gmail.com";
            var smtpPort = int.Parse(_configuration["EmailSettings:SmtpPort"] ?? "587");
            var username = _configuration["EmailSettings:Username"];
            var password = _configuration["EmailSettings:Password"];
            var fromEmail = _configuration["EmailSettings:FromEmail"] ?? username;
            var fromName = _configuration["EmailSettings:FromName"] ?? "Error Monitoring System";
            var enableSsl = bool.Parse(_configuration["EmailSettings:EnableSsl"] ?? "true");

            // Validate required settings
            if (string.IsNullOrEmpty(username) || string.IsNullOrEmpty(password))
            {
                throw new Exception("SMTP credentials not configured in appsettings.json");
            }

            using var smtp = new SmtpClient(smtpHost, smtpPort)
            {
                Credentials = new NetworkCredential(username, password),
                EnableSsl = enableSsl,
                Timeout = 30000, // Increased timeout
                DeliveryMethod = SmtpDeliveryMethod.Network
            };

            using var mail = new MailMessage()
            {
                From = new MailAddress(fromEmail, fromName),
                Subject = subject,
                Body = message,
                IsBodyHtml = true
            };
            
            mail.To.Add(email);

            Console.WriteLine($"📧 Attempting to send email to: {email}");
            Console.WriteLine($"📧 Using SMTP: {smtpHost}:{smtpPort}");
            Console.WriteLine($"📧 From: {fromEmail}");
            
            await smtp.SendMailAsync(mail);
            Console.WriteLine($"✅ Email sent successfully to: {email}");
        }
        catch (SmtpException smtpEx)
        {
            Console.WriteLine($"❌ SMTP Error: {smtpEx.Message}");
            Console.WriteLine($"❌ SMTP Status: {smtpEx.StatusCode}");
            throw new Exception($"Failed to send email: {smtpEx.Message}");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"❌ General email error: {ex.Message}");
            throw new Exception($"Failed to send email: {ex.Message}");
        }
    }

    public async Task SendAlertEmailAsync(string toEmail, string alertMessage, string severity)
    {
        var subject = $"[{severity.ToUpper()}] Error Alert - Error Monitoring System";
        var severityColor = severity.ToLower() switch
        {
            "critical" => "#dc2626",
            "high" => "#ea580c",
            "medium" => "#d97706",
            _ => "#059669"
        };

        var htmlBody = $@"
            <html>
            <body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                    <h2 style='color: {severityColor}; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px;'>
                        🚨 Error Alert - {severity.ToUpper()} Severity
                    </h2>
                    
                    <div style='background-color: #fef2f2; border: 1px solid {severityColor}; border-radius: 8px; padding: 15px; margin: 20px 0;'>
                        <h3 style='margin-top: 0; color: {severityColor};'>Alert Details</h3>
                        <p style='margin-bottom: 0;'>{alertMessage}</p>
                    </div>
                    
                    <p>Please log into the Error Monitoring Dashboard to investigate and resolve this issue.</p>
                    
                    <div style='text-align: center; margin: 30px 0;'>
                        <a href='http://localhost:3000/dashboard' 
                           style='background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block;'>
                            View Dashboard
                        </a>
                    </div>
                    
                    <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb;'>
                        <p style='margin: 0; color: #6b7280; font-size: 14px;'>
                            Error Monitoring System<br>
                            Timestamp: {DateTime.UtcNow:yyyy-MM-dd HH:mm:ss} UTC
                        </p>
                    </div>
                </div>
            </body>
            </html>";

        await SendEmailAsync(toEmail, subject, htmlBody);
    }
}
