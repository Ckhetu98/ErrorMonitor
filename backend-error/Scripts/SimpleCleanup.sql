-- Simple cleanup for LocalDB
USE ErrorMonitoringDB;

-- Drop tables in correct order
IF OBJECT_ID('dbo.Alerts', 'U') IS NOT NULL DROP TABLE Alerts;
IF OBJECT_ID('dbo.ErrorLogs', 'U') IS NOT NULL DROP TABLE ErrorLogs;
IF OBJECT_ID('dbo.AuditLogs', 'U') IS NOT NULL DROP TABLE AuditLogs;
IF OBJECT_ID('dbo.ContactQueries', 'U') IS NOT NULL DROP TABLE ContactQueries;
IF OBJECT_ID('dbo.Applications', 'U') IS NOT NULL DROP TABLE Applications;
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE Users;
IF OBJECT_ID('dbo.__EFMigrationsHistory', 'U') IS NOT NULL DROP TABLE __EFMigrationsHistory;