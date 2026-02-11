package com.tpms.service;

public interface EmailService {
    void sendSelectionEmail(String toEmail, String candidateName, String jobTitle, String companyName);
}