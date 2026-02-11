using System.Net;
using System.Net.Mail;

namespace ErrorMonitoringAPI.Services;

public class EmailService(IConfiguration configuration) : IEmailService
{
    private readonly IConfiguration _configuration = configuration;
    private readonly string _smtpHost = configuration["EmailSettings:SmtpHost"] ?? "smtp.gmail.com";
    private readonly int _smtpPort = int.Parse(configuration["EmailSettings:SmtpPort"] ?? "587");
    private readonly string _smtpUsername = configuration["EmailSettings:Username"];
    private readonly string _smtpPassword = configuration["EmailSettings:Password"];
    private readonly string _fromEmail = configuration["EmailSettings:FromEmail"];
    private readonly string _fromName = configuration["EmailSettings:FromName"] ?? "Error Monitoring System";

        public async Task<bool> SendContactResponseAsync(string toEmail, string subject, string message, string contactName)
        {
            try
            {
                var emailSubject = $"Re: {subject} - Response from Error Monitoring Team";
                var emailBody = $@"
                    <html>
                    <body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                        <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                            <h2 style='color: #2563eb; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px;'>
                                Error Monitoring System - Response
                            </h2>
                            
                            <p>Dear {contactName},</p>
                            
                            <p>Thank you for contacting us. We have reviewed your inquiry and here is our response:</p>
                            
                            <div style='background-color: #f8fafc; border-left: 4px solid #2563eb; padding: 15px; margin: 20px 0;'>
                                {message.Replace("\n", "<br>")}
                            </div>
                            
                            <p>If you have any further questions, please don't hesitate to contact us.</p>
                            
                            <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb;'>
                                <p style='margin: 0; color: #6b7280; font-size: 14px;'>
                                    Best regards,<br>
                                    Error Monitoring Support Team<br>
                                    <a href='mailto:{_fromEmail}' style='color: #2563eb;'>{_fromEmail}</a>
                                </p>
                            </div>
                        </div>
                    </body>
                    </html>";

                return await SendEmailAsync(toEmail, emailSubject, emailBody);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error sending contact response email: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> SendContactConfirmationAsync(string toEmail, string contactName, string subject)
        {
            try
            {
                var emailSubject = "Contact Form Received - Error Monitoring System";
                var emailBody = $@"
                    <html>
                    <body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>
                        <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>
                            <h2 style='color: #2563eb; border-bottom: 2px solid #e5e7eb; padding-bottom: 10px;'>
                                Contact Form Confirmation
                            </h2>
                            
                            <p>Dear {contactName},</p>
                            
                            <p>We have received your contact form submission with the subject: <strong>{subject}</strong></p>
                            
                            <div style='background-color: #f0f9ff; border: 1px solid #0ea5e9; border-radius: 8px; padding: 15px; margin: 20px 0;'>
                                <h3 style='margin-top: 0; color: #0ea5e9;'>What happens next?</h3>
                                <ul style='margin-bottom: 0;'>
                                    <li>Our support team will review your inquiry within 24 hours</li>
                                    <li>You will receive a detailed response via email</li>
                                    <li>For urgent matters, please contact us directly</li>
                                </ul>
                            </div>
                            
                            <p>Thank you for reaching out to us!</p>
                            
                            <div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb;'>
                                <p style='margin: 0; color: #6b7280; font-size: 14px;'>
                                    Best regards,<br>
                                    Error Monitoring Support Team<br>
                                    <a href='mailto:{_fromEmail}' style='color: #2563eb;'>{_fromEmail}</a>
                                </p>
                            </div>
                        </div>
                    </body>
                    </html>";

                return await SendEmailAsync(toEmail, emailSubject, emailBody);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error sending contact confirmation email: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> SendAlertEmailAsync(string toEmail, string alertMessage, string severity)
        {
            try
            {
                var emailSubject = $"[{severity.ToUpper()}] Error Alert - Error Monitoring System";
                var severityColor = severity.ToLower() switch
                {
                    "critical" => "#dc2626",
                    "high" => "#ea580c",
                    "medium" => "#d97706",
                    _ => "#059669"
                };

                var emailBody = $@"
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

                return await SendEmailAsync(toEmail, emailSubject, emailBody);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error sending alert email: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> SendEmailAsync(string toEmail, string subject, string htmlBody)
        {
            try
            {
                // Validate SMTP configuration
                if (string.IsNullOrEmpty(_smtpUsername) || string.IsNullOrEmpty(_smtpPassword))
                {
                    throw new Exception("SMTP credentials not configured in appsettings.json");
                }

                using var client = new SmtpClient(_smtpHost, _smtpPort);
                client.EnableSsl = true;
                client.Credentials = new NetworkCredential(_smtpUsername, _smtpPassword);

                using var message = new MailMessage();
                message.From = new MailAddress(_fromEmail, _fromName);
                message.To.Add(toEmail);
                message.Subject = subject;
                message.Body = htmlBody;
                message.IsBodyHtml = true;

                await client.SendMailAsync(message);
                Console.WriteLine($"✅ Email sent successfully to: {toEmail}");
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"❌ Failed to send email: {ex.Message}");
                return false;
            }
        }
    }