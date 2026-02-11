package com.tpms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;

    @Override
    public void sendSelectionEmail(String toEmail, String candidateName, String jobTitle, String companyName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Congratulations! You've been selected for Final Round - " + jobTitle);
            message.setText(
                "Dear " + candidateName + ",\n\n" +
                "Congratulations! We are pleased to inform you that you have been selected for the final round of interviews for the position of " + jobTitle + " at " + companyName + ".\n\n" +
                "Our team was impressed with your application and qualifications. We look forward to the next steps in the selection process.\n\n" +
                "You will be contacted soon with further details regarding the final interview process.\n\n" +
                "Best regards,\n" +
                "Training & Placement Team\n" +
                companyName
            );
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't fail the status update
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}