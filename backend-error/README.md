# Monolithic Backend Setup Guide - Error Monitoring System

## OVERVIEW
This is a simplified monolithic version of the Error Monitoring System backend.
All services are combined into a single ASP.NET Core application running on port 5000.

## STEP 1: Prerequisites
- Visual Studio 2022 or VS Code
- .NET 8 SDK
- SQL Server Express
- SQL Server Management Studio (SSMS)

## STEP 2: Database Setup
1. Start SQL Server Express service
2. Open SSMS and connect to .\SQLEXPRESS
3. Create database: "ErrorMonitoring"
4. Run scripts:
   - d:\cdac\project\final\database\complete_database.sql
   - d:\cdac\project\final\database\sample_data.sql

## STEP 3: Run Backend (Visual Studio)
1. Open Visual Studio 2022
2. File → Open → Project/Solution
3. Navigate to: d:\cdac\project\final\backend-error
4. Open: ErrorMonitoringAPI.csproj
5. Press F5 to run

## STEP 4: Run Backend (VS Code)
1. Open VS Code
2. Open folder: d:\cdac\project\final\backend-error
3. Open terminal (Ctrl + `)
4. Run commands:
   ```
   dotnet restore
   dotnet build
   dotnet run
   ```

## STEP 5: Verify Backend
- Backend runs on: http://localhost:5000
- Swagger UI: http://localhost:5000/swagger
- Health check: http://localhost:5000/health

## STEP 6: Run Frontend
1. Open new terminal
2. Navigate to: d:\cdac\project\final\frontend
3. Run commands:
   ```
   npm install
   npm run dev
   ```
4. Frontend runs on: http://localhost:3000

## API ENDPOINTS
All endpoints are available at http://localhost:5000/api/

### Authentication
- POST /api/auth/login
- POST /api/auth/google-login

### Dashboard
- GET /api/dashboard/stats
- GET /api/dashboard/recent-errors
- GET /api/dashboard/active-alerts

### Alerts
- POST /api/alerts
- GET /api/alerts/audit

### Contact
- POST /api/contact/submit
- GET /api/contact

## FEATURES INCLUDED
✅ Google SSO integration
✅ JWT authentication
✅ Dashboard with real data
✅ Alert management
✅ Contact form handling
✅ Audit logging
✅ SignalR hubs for real-time
✅ Swagger documentation
✅ CORS configured for frontend

## TESTING
1. Login with: admin/password
2. Test Google login (demo mode)
3. View dashboard with real data
4. Create alerts
5. Submit contact forms

## PRODUCTION DEPLOYMENT
1. Update connection string in appsettings.json
2. Configure HTTPS
3. Deploy to IIS/Azure/AWS
4. Update frontend API_BASE_URL

This monolithic version is easier to deploy and maintain while providing all the same functionality as the microservices version.