-- Database Cleanup Script for EF Core Migration
-- Run this script to clean existing database before applying EF Core migrations

USE [ErrorMonitoringDB]
GO

-- Drop foreign key constraints first
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_ErrorLogs_Applications')
    ALTER TABLE [ErrorLogs] DROP CONSTRAINT [FK_ErrorLogs_Applications]
GO

IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Alerts_ErrorLogs')
    ALTER TABLE [Alerts] DROP CONSTRAINT [FK_Alerts_ErrorLogs]
GO

IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_AuditLogs_Users')
    ALTER TABLE [AuditLogs] DROP CONSTRAINT [FK_AuditLogs_Users]
GO

-- Drop indexes
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Users_Username')
    DROP INDEX [IX_Users_Username] ON [Users]
GO

IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Users_Email')
    DROP INDEX [IX_Users_Email] ON [Users]
GO

-- Drop tables in correct order (child tables first)
IF OBJECT_ID('dbo.Alerts', 'U') IS NOT NULL
    DROP TABLE [Alerts]
GO

IF OBJECT_ID('dbo.ErrorLogs', 'U') IS NOT NULL
    DROP TABLE [ErrorLogs]
GO

IF OBJECT_ID('dbo.AuditLogs', 'U') IS NOT NULL
    DROP TABLE [AuditLogs]
GO

IF OBJECT_ID('dbo.ContactQueries', 'U') IS NOT NULL
    DROP TABLE [ContactQueries]
GO

IF OBJECT_ID('dbo.Applications', 'U') IS NOT NULL
    DROP TABLE [Applications]
GO

IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL
    DROP TABLE [Users]
GO

-- Drop EF Core migration history table if exists
IF OBJECT_ID('dbo.__EFMigrationsHistory', 'U') IS NOT NULL
    DROP TABLE [__EFMigrationsHistory]
GO

PRINT 'Database cleanup completed successfully!'
PRINT 'You can now run: dotnet ef migrations add InitialCreate'
PRINT 'Then run: dotnet ef database update'