package com.errormonitoring.controller;

import com.errormonitoring.dto.AuthDTOs;
import com.errormonitoring.model.User;
import com.errormonitoring.service.AuthService;
import com.errormonitoring.service.TwoFactorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private TwoFactorService twoFactorService;
    
    @Autowired
    private com.errormonitoring.service.GlobalSettingsService globalSettingsService;
    
    @Autowired
    private com.errormonitoring.service.AuditLogService auditLogService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTOs.LoginRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("Attempting login for: " + request.getUsername());
            
            var userOpt = authService.validateUser(request.getUsername(), request.getPassword());
            
            if (userOpt.isEmpty()) {
                System.out.println("User not found or invalid credentials: " + request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
            }
            
            User user = userOpt.get();
            System.out.println("User found: " + user.getUsername() + ", checking 2FA setting");
            
            // Check if 2FA is required (global setting OR individual user setting)
            boolean requires2FA = globalSettingsService.isGlobal2FAEnabled() || user.getTwoFactorEnabled();
            
            if (requires2FA) {
                System.out.println("2FA is required (Global: " + globalSettingsService.isGlobal2FAEnabled() + ", User: " + user.getTwoFactorEnabled() + "), sending OTP");
                // Send OTP when either global 2FA is enabled OR user has individual 2FA enabled
                twoFactorService.generateAndSendOTP(user);
                
                // Log login attempt
                try {
                    System.out.println("Logging audit for user: " + user.getId());
                    auditLogService.logActivity(user.getId(), "LOGIN_ATTEMPT", "User", 
                        user.getId().toString(), null, null, getClientIP(httpRequest), 
                        httpRequest.getHeader("User-Agent"));
                    System.out.println("Audit log created successfully");
                } catch (Exception e) {
                    System.err.println("Failed to log audit: " + e.getMessage());
                    e.printStackTrace();
                }
                
                return ResponseEntity.ok(Map.of(
                    "requiresTwoFactor", true,
                    "userId", user.getId().toString(),
                    "message", "Verification code sent to your registered email"
                ));
            } else {
                System.out.println("2FA not required, direct login");
                // Global 2FA disabled - direct login allowed
                String token = authService.generateJwtToken(user);
                
                // Log successful login
                try {
                    System.out.println("Logging successful login for user: " + user.getId());
                    auditLogService.logActivity(user.getId(), "LOGIN", "User", 
                        user.getId().toString(), null, null, getClientIP(httpRequest), 
                        httpRequest.getHeader("User-Agent"));
                    System.out.println("Login audit log created successfully");
                } catch (Exception e) {
                    System.err.println("Failed to log audit: " + e.getMessage());
                    e.printStackTrace();
                }
                
                AuthDTOs.LoginResponse response = new AuthDTOs.LoginResponse();
                response.setToken(token);
                response.setUser(authService.mapToUserResponse(user));
                response.setExpiresAt(LocalDateTime.now().plusHours(24)
                    .format(DateTimeFormatter.ISO_DATE_TIME));
                
                System.out.println("=== LOGIN RESPONSE DEBUG ===");
                System.out.println("Response user role: " + response.getUser().getRole());
                System.out.println("Response user ID: " + response.getUser().getId());
                System.out.println("User object role: " + user.getRole());
                
                return ResponseEntity.ok(response);
            }
        } catch (RuntimeException e) {
            System.err.println("Runtime error during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An error occurred during login"));
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody AuthDTOs.VerifyOTPRequest request, HttpServletRequest httpRequest) {
        try {
            Integer userId = Integer.parseInt(request.getUserId());
            
            var userOpt = authService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired OTP"));
            }
            
            User user = userOpt.get();
            
            if (!twoFactorService.validateOTP(user, request.getCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid or expired OTP"));
            }
            
            String token = authService.generateJwtToken(user);
            
            // Log successful OTP verification and login
            try {
                System.out.println("Logging OTP verification for user: " + user.getId());
                auditLogService.logActivity(user.getId(), "LOGIN", "User", 
                    user.getId().toString(), null, null, getClientIP(httpRequest), 
                    httpRequest.getHeader("User-Agent"));
                System.out.println("OTP verification audit log created successfully");
            } catch (Exception e) {
                System.err.println("Failed to log audit: " + e.getMessage());
                e.printStackTrace();
            }
            
            AuthDTOs.LoginResponse response = new AuthDTOs.LoginResponse();
            response.setToken(token);
            response.setUser(authService.mapToUserResponse(user));
            response.setExpiresAt(LocalDateTime.now().plusHours(24)
                .format(DateTimeFormatter.ISO_DATE_TIME));
            
            System.out.println("=== OTP VERIFICATION RESPONSE DEBUG ===");
            System.out.println("OTP Response user role: " + response.getUser().getRole());
            System.out.println("OTP Response user ID: " + response.getUser().getId());
            
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid or expired OTP"));
        } catch (Exception e) {
            logger.error("OTP verification error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An error occurred during verification"));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        try {
            System.out.println("=== REGISTRATION DEBUG ===");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Email: " + request.getEmail());
            System.out.println("Role from request: " + request.getRole());
            
            String roleToUse = request.getRole() != null ? request.getRole() : "VIEWER";
            System.out.println("Role to use: " + roleToUse);
            
            User user = authService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                roleToUse
            );
            
            return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "user", authService.mapToUserResponse(user)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Registration failed. Username or email may already exist."));
        } catch (Exception e) {
            logger.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An error occurred during registration"));
        }
    }
    
    @GetMapping("/2fa-status")
    public ResponseEntity<?> get2FAStatus() {
        return ResponseEntity.ok(Map.of(
            "enabled", false,
            "globalEnabled", globalSettingsService.isGlobal2FAEnabled(),
            "globalTwoFactorEnabled", globalSettingsService.isGlobal2FAEnabled()
        ));
    }
    
    @PostMapping("/toggle-global-2fa")
    public ResponseEntity<?> toggleGlobal2FA(@RequestBody Map<String, Boolean> request) {
        try {
            Boolean enabled = request.get("enabled");
            globalSettingsService.setGlobal2FAEnabled(enabled);
            
            return ResponseEntity.ok(Map.of(
                "message", "Global 2FA " + (enabled ? "enabled" : "disabled") + " successfully",
                "globalTwoFactorEnabled", enabled
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "Failed to update global 2FA setting"));
        }
    }
    
    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String email = request.get("email");
            String name = request.get("name");
            
            if (token == null || email == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Google token and email are required"));
            }
            
            if (name == null) {
                name = email.split("@")[0];
            }
            
            System.out.println("Google login attempt for email: " + email);
            
            // Check if user exists by email
            var existingUser = authService.findByEmail(email);
            User user;
            
            if (existingUser.isPresent()) {
                // User exists - use existing user with their current role
                user = existingUser.get();
                System.out.println("Found existing user: " + user.getUsername() + " with role: " + user.getRole());
                user.setLastLoginAt(LocalDateTime.now());
                user = authService.updateUser(user);
            } else {
                // New user - create with DEVELOPER role
                System.out.println("Creating new user for email: " + email + " with DEVELOPER role");
                String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "") + "_" + System.currentTimeMillis();
                user = authService.createGoogleUser(username, email, name, "DEVELOPER");
            }
            
            // Send OTP for authentication
            twoFactorService.generateAndSendOTP(user);
            
            return ResponseEntity.ok(Map.of(
                "requiresTwoFactor", true,
                "userId", user.getId().toString(),
                "message", "Verification code sent to your registered email"
            ));
        } catch (Exception e) {
            logger.error("Google auth error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Google authentication failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/google/callback")
    public ResponseEntity<?> googleCallback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state) {
        try {
            if (code == null) {
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/login?error=no_code")
                    .build();
            }
            
            logger.info("Google callback received code: {}", code.substring(0, Math.min(code.length(), 10)) + "...");
            
            // Exchange code for access token and get real user data
            String clientId = "1085239542052-9j0bcugc78d8a4ij3uic6rnm1rncmkll.apps.googleusercontent.com";
            String clientSecret = "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";
            String redirectUri = "http://localhost:8080/api/auth/google/callback";
            
            logger.info("Using clientId: {}...", clientId != null ? clientId.substring(0, Math.min(clientId.length(), 10)) : "null");
            
            if (clientId == null || clientSecret == null) {
                logger.error("Google OAuth credentials not configured");
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/login?error=config_missing")
                    .build();
            }
            
            // Exchange authorization code for access token
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            
            String tokenUrl = "https://oauth2.googleapis.com/token";
            org.springframework.util.MultiValueMap<String, String> tokenParams = new org.springframework.util.LinkedMultiValueMap<>();
            tokenParams.add("client_id", clientId);
            tokenParams.add("client_secret", clientSecret);
            tokenParams.add("code", code);
            tokenParams.add("grant_type", "authorization_code");
            tokenParams.add("redirect_uri", redirectUri);
            
            org.springframework.http.HttpHeaders tokenHeaders = new org.springframework.http.HttpHeaders();
            tokenHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> tokenRequest = 
                new org.springframework.http.HttpEntity<>(tokenParams, tokenHeaders);
            
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            
            if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
                logger.error("Failed to get access token from Google");
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/login?error=token_failed")
                    .build();
            }
            
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            
            if (accessToken == null) {
                logger.error("No access token in Google response");
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/login?error=token_failed")
                    .build();
            }
            
            // Get user info from Google
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            org.springframework.http.HttpHeaders userHeaders = new org.springframework.http.HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            org.springframework.http.HttpEntity<String> userRequest = new org.springframework.http.HttpEntity<>(userHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, org.springframework.http.HttpMethod.GET, userRequest, Map.class);
            
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                logger.error("Failed to get user info from Google");
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/login?error=userinfo_failed")
                    .build();
            }
            
            Map<String, Object> userData = userResponse.getBody();
            String email = (String) userData.get("email");
            String name = (String) userData.get("name");
            
            if (email == null) {
                logger.error("No email in Google user data");
                return ResponseEntity.status(302)
                    .header("Location", "http://localhost:3000/login?error=userinfo_failed")
                    .build();
            }
            
            logger.info("Google OAuth: Got user {}", email);
            
            // Use the actual Google user data
            User user;
            var existingUser = authService.findByEmail(email);
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.setLastLoginAt(LocalDateTime.now());
                user = authService.updateUser(user);
                logger.info("Found existing user: {} (ID: {}) with role: {}", user.getUsername(), user.getId(), user.getRole());
            } else {
                String username = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "") + "_" + System.currentTimeMillis();
                user = authService.createGoogleUser(username, email, name != null ? name : "Google User", "DEVELOPER");
                logger.info("Created new user: {} (ID: {})", user.getUsername(), user.getId());
            }
            
            // Check if 2FA is required (global setting OR individual user setting)
            boolean requires2FA = globalSettingsService.isGlobal2FAEnabled() || user.getTwoFactorEnabled();
            
            if (requires2FA) {
                // 2FA required - send OTP and redirect to OTP page
                logger.info("2FA required for Google user, sending OTP");
                twoFactorService.generateAndSendOTP(user);
                
                // Redirect to OTP verification page with user ID
                String redirectUrl = "http://localhost:3000/auth/verify-otp?userId=" + user.getId() + "&email=" + email;
                return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .build();
            } else {
                // No 2FA required - direct login
                logger.info("No 2FA required for Google user, direct login");
                String jwtToken = authService.generateJwtToken(user);
                
                // Redirect to frontend success page
                String redirectUrl = "http://localhost:3000/login/success?token=" + jwtToken;
                
                return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .build();
            }
        } catch (Exception e) {
            logger.error("Google callback error: {}", e.getMessage(), e);
            return ResponseEntity.status(302)
                .header("Location", "http://localhost:3000/login?error=google_auth_failed")
                .build();
        }
    }
    
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOTP(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User ID is required"));
            }
            
            var userOpt = authService.getUserById(Integer.parseInt(userId));
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
            }
            
            User user = userOpt.get();
            twoFactorService.generateAndSendOTP(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "New verification code sent to your registered email"
            ));
        } catch (Exception e) {
            logger.error("Resend OTP error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to resend verification code"));
        }
    }
    
    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enable2FA(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "2FA enabled successfully",
                "enabled", true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "Failed to enable 2FA"));
        }
    }
    
    @PostMapping("/disable-2fa")
    public ResponseEntity<?> disable2FA(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "2FA disabled successfully",
                "enabled", false
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("message", "Failed to disable 2FA"));
        }
    }
    
    private String getClientIP(jakarta.servlet.http.HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}