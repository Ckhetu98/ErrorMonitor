using Microsoft.AspNetCore.Mvc;
using ErrorMonitoringAPI.Services;
using ErrorMonitoringAPI.DTOs;
using Microsoft.Extensions.Logging;

namespace ErrorMonitoringAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ContactController(IContactService contactService, ILogger<ContactController> logger) : ControllerBase
{
    private readonly IContactService _contactService = contactService;
    private readonly ILogger<ContactController> _logger = logger;

    [HttpPost("submit")]
    public async Task<IActionResult> SubmitContact([FromBody] ContactSubmissionRequest request)
    {
        try
        {
            var ipAddress = Request.HttpContext.Connection.RemoteIpAddress?.ToString();
            var userAgent = Request.Headers["User-Agent"].ToString();
            
            var queryId = await _contactService.SubmitContactQueryAsync(request, ipAddress, userAgent);

            return Ok(new { 
                id = queryId, 
                message = "Contact form submitted successfully. You will receive a confirmation email shortly." 
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error submitting contact form");
            return StatusCode(500, new { message = "Failed to submit contact form" });
        }
    }

    [HttpGet]
    public async Task<IActionResult> GetContactQueries()
    {
        try
        {
            var queries = await _contactService.GetContactQueriesAsync();
            return Ok(queries);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching contact queries");
            return StatusCode(500, new { message = "Failed to fetch contact queries" });
        }
    }

    [HttpGet("stats")]
    public async Task<IActionResult> GetContactStats()
    {
        try
        {
            var stats = await _contactService.GetContactStatsAsync();
            return Ok(stats);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching contact stats");
            return StatusCode(500, new { message = "Failed to fetch contact stats" });
        }
    }

    [HttpGet("{id}")]
    public async Task<IActionResult> GetContactQuery(int id)
    {
        try
        {
            var query = await _contactService.GetContactQueryByIdAsync(id);
            if (query == null)
                return NotFound(new { message = "Contact query not found" });

            return Ok(query);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching contact query with id {Id}", id);
            return StatusCode(500, new { message = "Failed to fetch contact query" });
        }
    }

    [HttpPost("{id}/resolve")]
    public async Task<IActionResult> ResolveContact(int id, [FromBody] ContactResolveRequest request)
    {
        try
        {
            var success = await _contactService.UpdateContactStatusAsync(id, "RESOLVED", request.ResponseMessage, null);
            if (!success)
                return NotFound(new { message = "Contact query not found" });

            return Ok(new { message = "Query resolved successfully" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error resolving contact query with id {Id}", id);
            return StatusCode(500, new { message = "Failed to resolve query" });
        }
    }

    [HttpPut("{id}/respond")]
    public async Task<IActionResult> RespondToContact(int id, [FromBody] ContactResponseRequest request)
    {
        try
        {
            var success = await _contactService.UpdateContactStatusAsync(id, "RESOLVED", request.ResponseMessage, request.AssignedTo);
            if (!success)
                return NotFound(new { message = "Contact query not found" });

            return Ok(new { message = "Response sent successfully" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error sending response for contact query with id {Id}", id);
            return StatusCode(500, new { message = "Failed to send response" });
        }
    }

    [HttpPut("{id}/status")]
    public async Task<IActionResult> UpdateContactStatus(int id, [FromBody] ContactStatusUpdateRequest request)
    {
        try
        {
            var success = await _contactService.UpdateContactStatusAsync(id, request.Status, null, request.AssignedTo);
            if (!success)
                return NotFound(new { message = "Contact query not found" });

            return Ok(new { message = "Status updated successfully" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error updating status for contact query with id {Id}", id);
            return StatusCode(500, new { message = "Failed to update status" });
        }
    }
}

public class ContactResolveRequest
{
    public string ResponseMessage { get; set; } = string.Empty;
}

public class ContactResponseRequest
{
    public string ResponseMessage { get; set; } = string.Empty;
    public int? AssignedTo { get; set; }
}

public class ContactStatusUpdateRequest
{
    public string Status { get; set; } = string.Empty;
    public int? AssignedTo { get; set; }
}