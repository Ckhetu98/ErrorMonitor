using ErrorMonitoringAPI.DTOs;
using Microsoft.EntityFrameworkCore;
using ErrorMonitoringAPI.Data;
using ErrorMonitoringAPI.Models;
using Microsoft.Extensions.Logging;

namespace ErrorMonitoringAPI.Services;

public class ApplicationService : IApplicationService
{
    private readonly ApplicationDbContext _context;
    private readonly ILogger<ApplicationService> _logger;

    public ApplicationService(ApplicationDbContext context, ILogger<ApplicationService> logger)
    {
        _context = context;
        _logger = logger;
    }

        public async Task<List<ApplicationDto>> GetAllApplicationsAsync(int? userId = null, string userRole = null)
        {
            var query = _context.Applications.AsQueryable();
            
            // Role-based filtering
            if (userRole?.ToUpper() == "DEVELOPER" && userId.HasValue)
            {
                query = query.Where(a => a.CreatedBy == userId.Value);
            }
            // ADMIN can see all applications (no filtering)
            
            var applications = await query
                .OrderByDescending(a => a.CreatedAt)
                .ToListAsync();

            // Get all error counts in a single query to avoid N+1 problem
            var applicationIds = applications.Select(a => a.Id).ToList();
            var errorCounts = await _context.ErrorLogs
                .Where(e => applicationIds.Contains(e.ApplicationId))
                .GroupBy(e => e.ApplicationId)
                .Select(g => new {
                    ApplicationId = g.Key,
                    TotalErrors = g.Count(),
                    OpenErrors = g.Count(e => e.Status == "OPEN"),
                    CriticalErrors = g.Count(e => e.Severity == "Critical")
                })
                .ToListAsync();

            return applications.Select(a => {
                var errorStats = errorCounts.FirstOrDefault(ec => ec.ApplicationId == a.Id);
                return new ApplicationDto
                {
                    Id = a.Id,
                    name = a.Name ?? "Unnamed Application",
                    apiKey = a.ApiKey ?? "Create new app to get API key",
                    description = a.Description ?? "No description available",
                    technology = a.Technology ?? "Spring Boot",
                    version = a.Version ?? "1.0.0",
                    baseUrl = a.BaseUrl ?? "http://localhost:8080",
                    status = a.IsActive ? "ACTIVE" : "INACTIVE",
                    isActive = a.IsActive,
                    isPaused = a.IsPaused,
                    createdAt = a.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                    errorCount = errorStats?.TotalErrors ?? 0,
                    openErrors = errorStats?.OpenErrors ?? 0,
                    criticalErrors = errorStats?.CriticalErrors ?? 0,
                    healthStatus = a.IsActive ? "Healthy" : "Unhealthy",
                    healthCheckUrl = a.HealthCheckUrl ?? ""
                };
            }).ToList();
        }

        public async Task<ApplicationDto> GetApplicationByIdAsync(int id)
        {
            var app = await _context.Applications.FindAsync(id);
            if (app == null) return null;

            var errorCounts = await _context.ErrorLogs
                .Where(e => e.ApplicationId == id)
                .GroupBy(e => 1)
                .Select(g => new {
                    TotalErrors = g.Count(),
                    OpenErrors = g.Count(e => e.Status == "OPEN"),
                    CriticalErrors = g.Count(e => e.Severity == "Critical")
                })
                .FirstOrDefaultAsync();

            return new ApplicationDto
            {
                Id = app.Id,
                name = app.Name ?? "Unnamed Application",
                apiKey = app.ApiKey ?? "Create new app to get API key",
                description = app.Description ?? "No description available",
                technology = app.Technology ?? "Spring Boot",
                version = app.Version ?? "1.0.0",
                baseUrl = app.BaseUrl ?? "http://localhost:8080",
                status = app.IsActive ? "ACTIVE" : "INACTIVE",
                isActive = app.IsActive,
                createdAt = app.CreatedAt.ToString("yyyy-MM-ddTHH:mm:ss.fffZ"),
                errorCount = errorCounts?.TotalErrors ?? 0,
                openErrors = errorCounts?.OpenErrors ?? 0,
                criticalErrors = errorCounts?.CriticalErrors ?? 0,
                healthStatus = app.IsActive ? "Healthy" : "Unhealthy",
                healthCheckUrl = app.HealthCheckUrl ?? ""
            };
        }

        public async Task<int> CreateApplicationAsync(ApplicationRequest request, int userId)
        {
            // Generate unique API key
            var apiKey = await GenerateUniqueApiKey(request.name);
            
            var app = new Application
            {
                Name = request.name,
                Description = request.description ?? "",
                Technology = request.technology ?? "Spring Boot",
                Version = request.version ?? "1.0.0",
                BaseUrl = request.baseUrl ?? "",
                HealthCheckUrl = request.healthCheckUrl ?? "",
                ApiKey = apiKey,
                IsActive = true, // Always create applications as active
                CreatedAt = DateTime.UtcNow,
                CreatedBy = userId
            };

            _context.Applications.Add(app);
            await _context.SaveChangesAsync();
            
            _logger.LogInformation($"Generated API key for application '{request.name}': {apiKey}");
            
            return app.Id;
        }
        
        private async Task<string> GenerateUniqueApiKey(string appName)
        {
            string apiKey;
            bool isUnique;
            
            do
            {
                // Clean app name for key generation - handle empty names
                var cleanName = string.IsNullOrWhiteSpace(appName) ? "APP" : appName.Replace(" ", "-").Replace("_", "-").ToUpper();
                var year = DateTime.UtcNow.Year;
                var randomNumber = new Random().Next(100, 999);
                
                apiKey = $"{cleanName}-{year}-{randomNumber}";
                
                // Check if key already exists
                isUnique = !await _context.Applications.AnyAsync(a => a.ApiKey == apiKey);
                
            } while (!isUnique);
            
            return apiKey;
        }

        public async Task<bool> UpdateApplicationAsync(int id, ApplicationRequest request)
        {
            try
            {
                _logger.LogInformation($"Attempting to update application with ID: {id}");
                
                var app = await _context.Applications.FirstOrDefaultAsync(a => a.Id == id);
                if (app == null) 
                {
                    _logger.LogWarning($"Application with ID {id} not found");
                    return false;
                }

                // Validate required fields
                if (string.IsNullOrWhiteSpace(request.name))
                {
                    _logger.LogWarning("name is null or empty");
                    return false;
                }

                _logger.LogInformation($"Updating application {id}: Name='{request.name}', Active={request.isActive}");
                
                // Update properties
                app.Name = request.name.Trim();
                app.Description = request.description?.Trim() ?? "";
                app.Technology = request.technology?.Trim() ?? "Spring Boot";
                app.Version = request.version?.Trim() ?? "1.0.0";
                app.BaseUrl = request.baseUrl?.Trim() ?? "";
                app.HealthCheckUrl = request.healthCheckUrl?.Trim() ?? "";
                app.IsActive = request.isActive;
                app.UpdatedAt = DateTime.UtcNow;

                // Mark entity as modified
                _context.Entry(app).State = EntityState.Modified;
                
                var result = await _context.SaveChangesAsync();
                _logger.LogInformation($"SaveChanges affected {result} rows");
                
                return result > 0;
            }
            catch (DbUpdateException dbEx)
            {
                _logger.LogError(dbEx, $"Database error updating application {id}: {dbEx.Message}");
                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, $"Error updating application {id}: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> DeleteApplicationAsync(int id)
        {
            using var transaction = await _context.Database.BeginTransactionAsync();
            try
            {
                // Delete related error logs first
                var errorLogs = await _context.ErrorLogs
                    .Where(e => e.ApplicationId == id)
                    .ToListAsync();
                _context.ErrorLogs.RemoveRange(errorLogs);

                // Delete related alerts
                var alerts = await _context.Alerts
                    .Where(a => a.ApplicationId == id)
                    .ToListAsync();
                _context.Alerts.RemoveRange(alerts);

                // Delete the application
                var app = await _context.Applications.FindAsync(id);
                if (app == null) return false;

                _context.Applications.Remove(app);
                await _context.SaveChangesAsync();
                await transaction.CommitAsync();
                return true;
            }
            catch
            {
                await transaction.RollbackAsync();
                return false;
            }
        }

        public async Task<object> CheckApplicationHealthAsync(int id)
        {
            var app = await _context.Applications.FindAsync(id);
            if (app == null) return null;

            return new
            {
                applicationName = app.Name,
                status = app.IsActive ? "Healthy" : "Unhealthy",
                lastChecked = DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
            };
        }

        public async Task<bool> IsApplicationOwnerAsync(int applicationId, int userId)
        {
            var app = await _context.Applications.FindAsync(applicationId);
            return app?.CreatedBy == userId;
        }

        public async Task<bool> PauseApplicationAsync(int id)
        {
            var app = await _context.Applications.FindAsync(id);
            if (app == null) return false;

            app.IsPaused = true;
            app.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> ResumeApplicationAsync(int id)
        {
            var app = await _context.Applications.FindAsync(id);
            if (app == null) return false;

            app.IsPaused = false;
            app.IsActive = true;
            app.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
            return true;
        }


    }
