# Quick Start Guide - Spring Boot Error Monitoring System

## 🚀 Getting Started in 5 Minutes

### Step 1: Prerequisites
- Java 17+ installed
- Maven 3.9+ installed
- MySQL 8.0+ running

### Step 2: Database Setup
```sql
CREATE DATABASE error_monitoring;
```

### Step 3: Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### Step 4: Build and Run
```bash
cd backend-spring-boot
mvn clean install
mvn spring-boot:run
```

### Step 5: Test the API
```bash
# Health check
curl http://localhost:8080/health

# Test error logging (public endpoint)
curl -X POST http://localhost:8080/api/error-logs/log \
  -H "Content-Type: application/json" \
  -d '{
    "applicationName": "Test-App",
    "errorMessage": "Test error",
    "severity": "High"
  }'
```

### Step 6: Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "password123",
    "role": "ADMIN"
  }'
```

### Step 7: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }'
```

Copy the `token` from the response and use it in subsequent requests:
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/dashboard/stats
```

## 📖 API Documentation

Once running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs

## 🔑 Default Roles

- **ADMIN**: Full access to all applications and errors
- **DEVELOPER**: Access to own applications and errors  
- **VIEWER**: Read-only access

## 📝 Next Steps

1. Create an application via `/api/applications`
2. Get the API key from the application
3. Use the API key to log errors from your applications
4. View errors in the dashboard at `/api/dashboard/stats`

## 🐛 Troubleshooting

**Port already in use?**
```bash
# Change port in application.properties
server.port=8081
```

**Database connection error?**
- Check MySQL is running
- Verify credentials in `application.properties`
- Ensure database exists

**JWT errors?**
- Check `jwt.secret` in `application.properties`
- Ensure token is included in Authorization header

## 📚 Full Documentation

See [README.md](README.md) for complete documentation.

