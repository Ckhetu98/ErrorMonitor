-- Test Data Script for EF Core Database
USE ErrorMonitoringDB;

-- Insert test user (admin)
IF NOT EXISTS (SELECT 1 FROM Users WHERE Username = 'admin')
BEGIN
    INSERT INTO Users (Username, Email, PasswordHash, Role, FirstName, LastName, IsActive, TwoFactorEnabled, CreatedAt)
    VALUES ('admin', 'admin@test.com', 'password', 'ADMIN', 'Admin', 'User', 1, 1, GETDATE());
END

-- Insert test applications
IF NOT EXISTS (SELECT 1 FROM Applications WHERE Name = 'Test App 1')
BEGIN
    INSERT INTO Applications (Name, Description, IsActive, CreatedAt, CreatedBy)
    VALUES ('Test App 1', 'First test application', 1, GETDATE(), 1);
END

IF NOT EXISTS (SELECT 1 FROM Applications WHERE Name = 'Test App 2')
BEGIN
    INSERT INTO Applications (Name, Description, IsActive, CreatedAt, CreatedBy)
    VALUES ('Test App 2', 'Second test application', 1, GETDATE(), 1);
END

-- Insert test error logs
DECLARE @AppId1 INT = (SELECT Id FROM Applications WHERE Name = 'Test App 1');
DECLARE @AppId2 INT = (SELECT Id FROM Applications WHERE Name = 'Test App 2');

IF @AppId1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ErrorLogs WHERE ApplicationId = @AppId1)
BEGIN
    INSERT INTO ErrorLogs (ApplicationId, Message, Severity, Status, CreatedAt)
    VALUES 
    (@AppId1, 'Database connection failed', 'Critical', 'OPEN', GETDATE()),
    (@AppId1, 'Authentication timeout', 'High', 'OPEN', GETDATE()),
    (@AppId1, 'Validation error', 'Medium', 'Resolved', GETDATE());
END

IF @AppId2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM ErrorLogs WHERE ApplicationId = @AppId2)
BEGIN
    INSERT INTO ErrorLogs (ApplicationId, Message, Severity, Status, CreatedAt)
    VALUES 
    (@AppId2, 'Cache miss error', 'Low', 'OPEN', GETDATE()),
    (@AppId2, 'API rate limit exceeded', 'Medium', 'OPEN', GETDATE());
END

-- Insert test alerts
IF NOT EXISTS (SELECT 1 FROM Alerts)
BEGIN
    INSERT INTO Alerts (Name, Description, Condition, Recipients, IsActive, CreatedAt, CreatedBy, ApplicationId)
    VALUES 
    ('Critical Alert', 'Critical error detected', 'Severity = Critical', 'admin@test.com', 1, GETDATE(), 1, @AppId1),
    ('High Alert', 'High severity error', 'Severity = High', 'admin@test.com', 1, GETDATE(), 1, @AppId1);
END

PRINT 'Test data inserted successfully!';