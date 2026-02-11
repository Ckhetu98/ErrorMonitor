using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ErrorMonitoringAPI.Migrations
{
    /// <inheritdoc />
    public partial class UpdateErrorLogSchema : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // Add missing columns to match Spring Boot ErrorLog structure
            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'StackTrace')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [StackTrace] nvarchar(max) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'Source')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [Source] nvarchar(200) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'Status')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [Status] nvarchar(50) NULL DEFAULT 'Open'
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'Environment')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [Environment] nvarchar(50) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'UserAgent')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [UserAgent] nvarchar(500) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'IpAddress')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [IpAddress] nvarchar(45) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'HttpMethod')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [HttpMethod] nvarchar(10) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'ApiEndpoint')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [ApiEndpoint] nvarchar(500) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'ErrorType')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [ErrorType] nvarchar(100) NULL
                END
            ");

            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[ErrorLogs]') AND name = 'ResolvedAt')
                BEGIN
                    ALTER TABLE [ErrorLogs] ADD [ResolvedAt] datetime2 NULL
                END
            ");

            // Ensure default application exists with correct column names
            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT 1 FROM Applications WHERE Id = 1)
                BEGIN
                    SET IDENTITY_INSERT Applications ON
                    INSERT INTO Applications (Id, Name, Description, Technology, Version, BaseUrl, HealthCheckUrl, ApiKey, IsActive, IsPaused, CreatedBy, CreatedAt)
                    VALUES (1, 'Default Application', 'Default application for error monitoring', '.NET Core', '1.0.0', 'http://localhost:8080', 'http://localhost:8080/health', 'DEFAULT-API-KEY-2024', 1, 0, 1, GETDATE())
                    SET IDENTITY_INSERT Applications OFF
                END
            ");

            // Insert sample error data with varied dates for trend analysis
            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT 1 FROM ErrorLogs)
                BEGIN
                    INSERT INTO ErrorLogs (ApplicationId, Message, StackTrace, Source, Severity, Status, Environment, ErrorType, CreatedAt)
                    VALUES 
                    (1, 'Database connection timeout', 'System.TimeoutException at DatabaseService.Connect()', 'DatabaseService', 'Critical', 'Open', 'Production', 'TimeoutException', DATEADD(month, -5, GETDATE())),
                    (1, 'Authentication failed for user', 'System.UnauthorizedAccessException at AuthService.Login()', 'AuthService', 'High', 'Resolved', 'Production', 'UnauthorizedAccessException', DATEADD(month, -4, GETDATE())),
                    (1, 'Null reference exception in user service', 'System.NullReferenceException at UserService.GetUser()', 'UserService', 'Medium', 'Open', 'Development', 'NullReferenceException', DATEADD(month, -3, GETDATE())),
                    (1, 'API rate limit exceeded', 'System.InvalidOperationException at ApiController.ProcessRequest()', 'ApiController', 'Low', 'Resolved', 'Production', 'InvalidOperationException', DATEADD(month, -2, GETDATE())),
                    (1, 'File not found error', 'System.FileNotFoundException at FileService.ReadFile()', 'FileService', 'Medium', 'Open', 'Staging', 'FileNotFoundException', DATEADD(month, -1, GETDATE())),
                    (1, 'Memory allocation failed', 'System.OutOfMemoryException at DataProcessor.Process()', 'DataProcessor', 'Critical', 'Open', 'Production', 'OutOfMemoryException', DATEADD(day, -15, GETDATE())),
                    (1, 'Invalid input parameter', 'System.ArgumentException at ValidationService.Validate()', 'ValidationService', 'Low', 'Resolved', 'Development', 'ArgumentException', DATEADD(day, -10, GETDATE())),
                    (1, 'Network connection lost', 'System.NetworkException at NetworkService.SendRequest()', 'NetworkService', 'High', 'Open', 'Production', 'NetworkException', DATEADD(day, -5, GETDATE())),
                    (1, 'Configuration value missing', 'System.ConfigurationException at ConfigService.GetValue()', 'ConfigService', 'Medium', 'Open', 'Staging', 'ConfigurationException', DATEADD(day, -2, GETDATE())),
                    (1, 'Concurrent access violation', 'System.InvalidOperationException at CacheService.Update()', 'CacheService', 'High', 'Open', 'Production', 'InvalidOperationException', GETDATE())
                END
            ");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            // Remove sample data
            migrationBuilder.Sql("DELETE FROM ErrorLogs WHERE ApplicationId = 1");
            migrationBuilder.Sql("DELETE FROM Applications WHERE Id = 1");
        }
    }
}