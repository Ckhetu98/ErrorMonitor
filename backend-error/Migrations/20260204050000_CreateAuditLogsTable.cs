using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ErrorMonitoringAPI.Migrations
{
    /// <inheritdoc />
    public partial class CreateAuditLogsTable : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            // Create AuditLogs table if it doesn't exist
            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AuditLogs')
                BEGIN
                    CREATE TABLE [AuditLogs] (
                        [Id] int IDENTITY(1,1) NOT NULL,
                        [UserId] int NOT NULL,
                        [Action] nvarchar(100) NOT NULL,
                        [EntityType] nvarchar(100) NOT NULL,
                        [EntityId] nvarchar(100) NOT NULL,
                        [OldValues] nvarchar(max) NULL,
                        [NewValues] nvarchar(max) NULL,
                        [IpAddress] nvarchar(45) NULL,
                        [UserAgent] nvarchar(500) NULL,
                        [CreatedAt] datetime2 NOT NULL,
                        CONSTRAINT [PK_AuditLogs] PRIMARY KEY ([Id])
                    )
                END
            ");

            // Insert sample audit data
            migrationBuilder.Sql(@"
                IF NOT EXISTS (SELECT 1 FROM AuditLogs)
                BEGIN
                    INSERT INTO AuditLogs (UserId, Action, EntityType, EntityId, NewValues, CreatedAt)
                    VALUES 
                    (1, 'CREATE', 'ErrorLog', '1', 'Created new error log', DATEADD(day, -5, GETDATE())),
                    (1, 'UPDATE', 'Application', '1', 'Updated application settings', DATEADD(day, -3, GETDATE())),
                    (1, 'DELETE', 'ErrorLog', '2', 'Deleted resolved error', DATEADD(day, -1, GETDATE()))
                END
            ");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.Sql("DROP TABLE IF EXISTS [AuditLogs]");
        }
    }
}