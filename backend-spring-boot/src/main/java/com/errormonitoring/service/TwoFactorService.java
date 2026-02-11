package com.errormonitoring.service;

import com.errormonitoring.model.User;
import com.errormonitoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class TwoFactorService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;

    public void generateAndSendOTP(User user) {
        // Generate 6-digit OTP using SecureRandom
        SecureRandom random = new SecureRandom();
        String otp = String.format("%06d", random.nextInt(1000000));
        
        // Set OTP and expiry (5 minutes)
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Send OTP via email using EmailNotificationService
        emailNotificationService.sendOTPEmail(user.getEmail(), otp);
    }

    public boolean validateOTP(User user, String otp) {
        if (user.getOtpCode() == null || user.getOtpExpiry() == null || otp == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            // OTP expired
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return false;
        }

        // Use constant-time comparison to prevent timing attacks
        byte[] storedOtp = user.getOtpCode().getBytes(StandardCharsets.UTF_8);
        byte[] providedOtp = otp.getBytes(StandardCharsets.UTF_8);
        
        if (MessageDigest.isEqual(storedOtp, providedOtp)) {
            // OTP valid, clear it
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }


}