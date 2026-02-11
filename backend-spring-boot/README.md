# Error Monitoring System - Spring Boot Backend

## 🚀 Overview
Complete Spring Boot backend for the Error Monitoring System with JWT authentication, role-based access control, and comprehensive API endpoints.

## 🛠️ Technology Stack
- **Spring Boot 3.2.1**
- **Spring Security 6** with JWT
- **Spring Data JPA**
- **MySQL 8.0+**
- **Java 17+**
- **Maven 3.9+**

## 📋 Prerequisites
- Java 17 or higher
- Maven 3.9+
- MySQL 8.0+ running on localhost:3306
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## ⚙️ Setup Instructions

### 1. Database Setup
```sql
CREATE DATABASE error_monitoring_db;
CREATE USER 'error_user'@'localhost' IDENTIFIED BY 'error_password';
GRANT ALL PRIVILEGES ON error_monitoring_db.* TO 'error_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configuration
Update `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/error_monitoring_db
spring.datasource.username=root
spring.datasource.password=your_password

# Email (Gmail)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
```

### 3. Run Application
```bash
# Clone and navigate to project
cd backend-spring-boot

# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run
```

Application will start on: `http://localhost:8080`

## 📡 API Endpoints

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/verify-otp` - OTP verification
- `POST /api/auth/resend-otp` - Resend OTP
- `POST /api/auth/enable-2fa` - Enable 2FA
- `POST /api/auth/disable-2fa` - Disable 2FA

### Application Management
- `GET /api/applications` - List applications
- `POST /api/applications` - Create application
- `PUT /api/applications/{id}` - Update application
- `DELETE /api/applications/{id}` - Delete application

### Error Logging
- `GET /api/error-logs` - List error logs
- `POST /api/error-logs/log` - Log new error
- `PUT /api/error-logs/{id}/resolve` - Resolve error

### Alert Management
- `GET /api/alerts` - List alerts
- `POST /api/alerts` - Create alert
- `PUT /api/alerts/{id}` - Update alert
- `DELETE /api/alerts/{id}` - Delete alert

### Dashboard
- `GET /api/dashboard/stats` - Dashboard statistics
- `GET /api/dashboard/recent-errors` - Recent errors
- `GET /api/dashboard/active-alerts` - Active alerts
- `GET /api/dashboard/application-health` - Application health

### User Management (ADMIN only)
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `PUT /api/users/{id}/toggle-status` - Toggle user status

### Reports
- `GET /api/reports/error-trends` - Error trend reports
- `GET /api/reports/application-performance` - Performance reports
- `GET /api/reports/user-activity` - User activity reports

### Audit Logs (ADMIN only)
- `GET /api/audit/logs` - List audit logs
- `GET /api/audit/user-activity/{userId}` - User activity
- `GET /api/audit/entity-history/{type}/{id}` - Entity history

### Contact
- `POST /api/contact` - Submit contact form
- `GET /api/contact/queries` - List contact queries (ADMIN)

## 🔐 Security Features

### JWT Authentication
- 24-hour token expiration
- Role-based access control (ADMIN, DEVELOPER, VIEWER)
- Secure token generation with HMAC SHA-512

### Password Security
- BCrypt password hashing
- Password strength validation
- Two-factor authentication via email

### API Security
- CORS configuration for frontend integration
- Request validation with Bean Validation
- SQL injection prevention with JPA
- XSS protection with input sanitization

## 🏗️ Project Structure
```
src/main/java/com/errormonitoring/
├── controller/          # REST Controllers
│   ├── AuthController.java
│   ├── ApplicationsController.java
│   ├── AlertsController.java
│   ├── DashboardController.java
│   ├── ErrorLogsController.java
│   ├── UserController.java
│   ├── ReportsController.java
│   ├── AuditController.java
│   └── ContactController.java
├── dto/                 # Data Transfer Objects
├── model/              # JPA Entities
├── repository/         # JPA Repositories
├── service/            # Business Logic Services
├── security/           # Security Configuration
│   ├── SecurityConfig.java
│   ├── JwtUtils.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserPrincipal.java
│   └── CustomUserDetailsService.java
└── ErrorMonitoringApplication.java
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### API Testing with Swagger
Visit: `http://localhost:8080/swagger-ui.html`

### Manual Testing with cURL
```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test123!","role":"DEVELOPER"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123!"}'

# Get applications (with JWT token)
curl -X GET http://localhost:8080/api/applications \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🚀 Deployment

### Development
```bash
mvn spring-boot:run
```

### Production
```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/error-monitoring-system-1.0.0.jar
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/error-monitoring-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📊 Monitoring

### Health Check
- `GET /api/actuator/health`

### Metrics
- `GET /api/actuator/metrics`

### Application Info
- `GET /api/actuator/info`

## 🔧 Configuration

### Environment Variables
```bash
export DB_URL=jdbc:mysql://localhost:3306/error_monitoring_db
export DB_USERNAME=root
export DB_PASSWORD=password
export JWT_SECRET=your-secret-key
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### Profile-based Configuration
- `application.properties` - Default
- `application-dev.properties` - Development
- `application-prod.properties` - Production

## 🐛 Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Check MySQL service
sudo systemctl status mysql

# Test connection
mysql -u root -p -h localhost
```

**JWT Token Errors**
- Ensure JWT secret is at least 32 characters
- Check token expiration time
- Verify token format in Authorization header

**Email Not Sending**
- Enable 2FA on Gmail account
- Generate app-specific password
- Check SMTP settings and firewall

## 📝 API Documentation

Complete API documentation is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📞 Support

For technical support:
- **Email**: choudharykhetesh8@gmail.com
- **Documentation**: See `/documentation` folder
- **Issues**: Create GitHub issue

---

**Version**: 1.0.0  
**Last Updated**: January 2025  
**Developed by**: Error Monitoring Development Team