package com.errormonitoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendErrorNotification(String userEmail, String errorMessage, String applicationName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("🚨 Error Alert - " + applicationName);
            message.setText(
                "Dear User,\n\n" +
                "An error has been detected in your application: " + applicationName + "\n\n" +
                "Error Details:\n" + errorMessage + "\n\n" +
                "Please investigate and resolve this issue as soon as possible.\n\n" +
                "Best regards,\n" +
                "Error Monitoring System"
            );
            message.setFrom("noreply@errormonitoring.com");
            
            mailSender.send(message);
            System.out.println("Error notification sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send error notification: " + e.getMessage());
        }
    }
    
    public void sendErrorResolvedNotification(String userEmail, String errorMessage, String applicationName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("✅ Error Resolved - " + applicationName);
            message.setText(
                "Dear User,\n\n" +
                "Good news! The following error in your application " + applicationName + " has been resolved:\n\n" +
                "Error Details:\n" + errorMessage + "\n\n" +
                "The issue has been successfully fixed and your application is now running smoothly.\n\n" +
                "Best regards,\n" +
                "Error Monitoring System"
            );
            message.setFrom("noreply@errormonitoring.com");
            
            mailSender.send(message);
            System.out.println("Error resolved notification sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send error resolved notification: " + e.getMessage());
        }
    }
    
    public void sendOTPEmail(String userEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("🔐 Your OTP Code - Error Monitoring System");
            message.setText(
                "Dear User,\n\n" +
                "Your One-Time Password (OTP) for login verification is:\n\n" +
                "OTP: " + otpCode + "\n\n" +
                "This code will expire in 5 minutes.\n" +
                "Please do not share this code with anyone.\n\n" +
                "If you did not request this code, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Error Monitoring System"
            );
            message.setFrom("noreply@errormonitoring.com");
            
            mailSender.send(message);
            System.out.println("OTP sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }
    
    public void sendEmail(String userEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@errormonitoring.com");
            
            mailSender.send(message);
            System.out.println("Email sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}