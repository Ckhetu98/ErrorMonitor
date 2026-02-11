-- Delete all applications and related data
DELETE FROM Alerts WHERE ApplicationId IN (SELECT Id FROM Applications);
DELETE FROM ErrorLogs WHERE ApplicationId IN (SELECT Id FROM Applications);
DELETE FROM Applications;