using ErrorMonitoringAPI.DTOs;
using Microsoft.EntityFrameworkCore;
using ErrorMonitoringAPI.Data;
using ErrorMonitoringAPI.Models;

namespace ErrorMonitoringAPI.Services;

public class ContactService : IContactService
{
    private readonly ApplicationDbContext _context;
    private readonly IEmailSenderService _emailService;

    public ContactService(ApplicationDbContext context, IEmailSenderService emailService)
    {
        _context = context;
        _emailService = emailService;
    }

    public async Task<int> SubmitContactQueryAsync(
        ContactSubmissionRequest request,
        string ipAddress,
        string userAgent)
    {
        var contactQuery = new ContactQuery
        {
            FullName = request.Name,
            Email = request.Email,
            Subject = request.Subject,
            Message = request.Message,
            Status = "PENDING",
            IpAddress = ipAddress ?? "",
            UserAgent = userAgent ?? "",
            CreatedAt = DateTime.UtcNow
        };

        _context.ContactQueries.Add(contactQuery);
        await _context.SaveChangesAsync();

        await _emailService.SendEmailAsync(
            request.Email,
            "Contact Form Confirmation",
            $"<h3>Thank you for contacting us!</h3><p>We have received your message about: {request.Subject}</p><p>We will get back to you soon.</p>");

        return contactQuery.Id;
    }

    public async Task<List<ContactDto>> GetContactQueriesAsync()
    {
        var contacts = await _context.ContactQueries
            .OrderByDescending(c => c.CreatedAt)
            .ToListAsync();

        return contacts.Select(c => new ContactDto
        {
            Id = c.Id,
            FullName = c.FullName,
            Email = c.Email,
            Subject = c.Subject,
            Message = c.Message,
            Status = c.Status,
            CreatedAt = c.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        }).ToList();
    }

    public async Task<bool> UpdateContactStatusAsync(
        int id,
        string status,
        string responseMessage = null,
        int? assignedTo = null)
    {
        var contact = await _context.ContactQueries.FindAsync(id);
        if (contact == null) return false;

        contact.Status = status;
        contact.ResponseMessage = responseMessage;
        contact.AssignedTo = assignedTo;
        contact.UpdatedAt = DateTime.UtcNow;
        
        if (!string.IsNullOrEmpty(responseMessage))
        {
            contact.ResponseSentAt = DateTime.UtcNow;
        }

        int rowsAffected = await _context.SaveChangesAsync();

        if (!string.IsNullOrEmpty(responseMessage) && rowsAffected > 0)
        {
            await _emailService.SendEmailAsync(
                contact.Email,
                $"Response to: {contact.Subject}",
                $"<h3>Response to your inquiry</h3><p>Dear {contact.FullName},</p><p>{responseMessage}</p><p>Thank you for contacting us.</p>");
        }

        return rowsAffected > 0;
    }

    public async Task<ContactDto> GetContactQueryByIdAsync(int id)
    {
        var contact = await _context.ContactQueries.FindAsync(id);
        if (contact == null) return null;

        return new ContactDto
        {
            Id = contact.Id,
            FullName = contact.FullName,
            Email = contact.Email,
            Subject = contact.Subject,
            Message = contact.Message,
            Status = contact.Status,
            CreatedAt = contact.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        };
    }

    public async Task<object> GetContactStatsAsync()
    {
        var today = DateTime.Today;
        
        var totalQueries = await _context.ContactQueries.CountAsync();
        var pendingQueries = await _context.ContactQueries.CountAsync(c => c.Status == "PENDING");
        var resolvedQueries = await _context.ContactQueries.CountAsync(c => c.Status == "RESOLVED");
        var todayQueries = await _context.ContactQueries.CountAsync(c => c.CreatedAt.Date == today);

        return new
        {
            totalQueries,
            pendingQueries,
            resolvedQueries,
            todayQueries
        };
    }
}
