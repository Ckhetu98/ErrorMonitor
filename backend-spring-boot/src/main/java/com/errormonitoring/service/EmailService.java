package com.errormonitoring.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@errormonitoring.com}")
    private String fromEmail;
    
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
    
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail);
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
            // Fallback to plain text
            sendEmail(to, subject, htmlBody.replaceAll("<[^>]+>", ""));
        }
    }
    
    public void sendErrorAlertEmail(String to, String appName, String errorMessage, String severity) {
        String subject = "[" + severity + "] Error Alert - " + appName;
        String htmlBody = String.format(
            "<html><body>" +
            "<h2 style='color: %s;'>Error Alert</h2>" +
            "<p><strong>Application:</strong> %s</p>" +
            "<p><strong>Severity:</strong> %s</p>" +
            "<p><strong>Error Message:</strong></p>" +
            "<pre style='background: #f4f4f4; padding: 10px;'>%s</pre>" +
            "<p>Please review and take appropriate action.</p>" +
            "<hr><p style='color: #666;'>Error Monitoring System</p>" +
            "</body></html>",
            severity.equals("Critical") ? "#dc3545" : severity.equals("High") ? "#fd7e14" : "#ffc107",
            appName, severity, errorMessage
        );
        sendHtmlEmail(to, subject, htmlBody);
    }
    
    public void sendErrorResolvedEmail(String to, String appName, String errorMessage, String resolvedBy) {
        String subject = "[RESOLVED] Error Fixed - " + appName;
        String htmlBody = String.format(
            "<html><body>" +
            "<h2 style='color: #28a745;'>Error Resolved</h2>" +
            "<p><strong>Application:</strong> %s</p>" +
            "<p><strong>Error Message:</strong></p>" +
            "<pre style='background: #f4f4f4; padding: 10px;'>%s</pre>" +
            "<p><strong>Resolved By:</strong> %s</p>" +
            "<p>The error has been successfully resolved.</p>" +
            "<hr><p style='color: #666;'>Error Monitoring System</p>" +
            "</body></html>",
            appName, errorMessage, resolvedBy
        );
        sendHtmlEmail(to, subject, htmlBody);
    }
}

