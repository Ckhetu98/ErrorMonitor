package com.errormonitoring.controller;

import com.errormonitoring.dto.ContactDTOs;
import com.errormonitoring.model.ContactQuery;
import com.errormonitoring.service.EmailService;
import com.errormonitoring.repository.ContactQueryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    
    @Autowired
    private ContactQueryRepository contactQueryRepository;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/submit")
    public ResponseEntity<?> submitContactForm(@Valid @RequestBody ContactDTOs.ContactRequest request) {
        try {
            // Save contact query to database
            ContactQuery contactQuery = new ContactQuery();
            contactQuery.setName(request.getFullName());
            contactQuery.setFullName(request.getFullName());
            contactQuery.setEmail(request.getEmail());
            contactQuery.setPhone(request.getPhone());
            contactQuery.setSubject(request.getSubject());
            contactQuery.setMessage(request.getMessage());
            contactQuery.setStatus("PENDING");
            contactQuery.setCreatedAt(LocalDateTime.now());
            
            contactQuery = contactQueryRepository.save(contactQuery);
            
            // Send confirmation email to user
            try {
                String confirmationHtml = String.format(
                    "<h3>Thank you for contacting us!</h3>" +
                    "<p>Dear %s,</p>" +
                    "<p>We have received your message and will get back to you within 24 hours.</p>" +
                    "<p><strong>Your Message:</strong></p>" +
                    "<p>%s</p>" +
                    "<br><p>Best regards,<br>Error Monitoring Team</p>",
                    request.getFullName(), request.getMessage()
                );
                
                emailService.sendHtmlEmail(
                    request.getEmail(),
                    "Contact Form Confirmation - Error Monitoring System",
                    confirmationHtml
                );
            } catch (Exception e) {
                // Log email error but don't fail the request
                System.err.println("Failed to send confirmation email: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Contact form submitted successfully",
                "id", contactQuery.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to submit contact form", "error", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getContactQueries(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            List<ContactQuery> queries;
            if (status != null && !status.isEmpty()) {
                queries = contactQueryRepository.findByStatus(status.toUpperCase());
            } else {
                queries = contactQueryRepository.findAllByOrderByCreatedAtDesc();
            }
            
            return ResponseEntity.ok(Map.of(
                "queries", queries,
                "totalCount", queries.size(),
                "currentPage", page,
                "pageSize", pageSize
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch contact queries", "error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getContactStats() {
        try {
            long totalQueries = contactQueryRepository.count();
            long pendingQueries = contactQueryRepository.countByStatus("PENDING");
            long resolvedQueries = contactQueryRepository.countByStatus("RESOLVED");
            
            return ResponseEntity.ok(Map.of(
                "totalQueries", totalQueries,
                "pendingQueries", pendingQueries,
                "resolvedQueries", resolvedQueries
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch contact stats", "error", e.getMessage()));
        }
    }
    
    @PutMapping("/queries/{id}/status")
    public ResponseEntity<?> updateQueryStatus(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        try {
            ContactQuery query = contactQueryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact query not found"));
            
            query.setStatus(request.get("status"));
            query.setUpdatedAt(LocalDateTime.now());
            contactQueryRepository.save(query);
            
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to update status", "error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getContactQuery(@PathVariable Integer id) {
        try {
            ContactQuery query = contactQueryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact query not found"));
            return ResponseEntity.ok(query);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to fetch contact query", "error", e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveQuery(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        try {
            ContactQuery query = contactQueryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact query not found"));
            
            String responseMessage = request.get("responseMessage");
            
            query.setStatus("RESOLVED");
            query.setResponseMessage(responseMessage);
            query.setUpdatedAt(LocalDateTime.now());
            query.setResponseSentAt(LocalDateTime.now());
            contactQueryRepository.save(query);
            
            // Send resolution email to user
            try {
                String resolutionHtml = 
                    "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Query Resolved</title>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                    "    <div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                    "        <h1 style='color: white; margin: 0; font-size: 28px;'>Query Resolved ✅</h1>" +
                    "        <p style='color: #e8f4fd; margin: 10px 0 0 0; font-size: 16px;'>Your support request has been successfully resolved</p>" +
                    "    </div>" +
                    "    <div style='background: white; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;'>" +
                    "        <p style='font-size: 18px; margin-bottom: 20px;'>Dear <strong>" + query.getFullName() + "</strong>,</p>" +
                    "        <p style='margin-bottom: 25px;'>Thank you for contacting Error Monitoring System. Your query regarding <strong style='color: #667eea;'>" + query.getSubject() + "</strong> has been resolved by our support team.</p>" +
                    "        " +
                    "        <div style='background: #f8f9fa; border-left: 4px solid #007bff; padding: 20px; margin: 25px 0; border-radius: 5px;'>" +
                    "            <h3 style='color: #007bff; margin: 0 0 10px 0; font-size: 16px;'>📝 Your Original Message:</h3>" +
                    "            <p style='margin: 0; font-style: italic; color: #555;'>\"" + query.getMessage() + "\"</p>" +
                    "        </div>" +
                    "        " +
                    "        <div style='background: #e8f5e8; border-left: 4px solid #28a745; padding: 20px; margin: 25px 0; border-radius: 5px;'>" +
                    "            <h3 style='color: #28a745; margin: 0 0 10px 0; font-size: 16px;'>💬 Our Response:</h3>" +
                    "            <p style='margin: 0; color: #333; font-weight: 500;'>" + responseMessage + "</p>" +
                    "        </div>" +
                    "        " +
                    "        <div style='background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; margin: 25px 0; border-radius: 5px;'>" +
                    "            <p style='margin: 0; color: #856404;'><strong>💡 Need More Help?</strong> If you have any further questions or need additional assistance, please don't hesitate to contact us again.</p>" +
                    "        </div>" +
                    "        " +
                    "        <div style='text-align: center; margin: 30px 0;'>" +
                    "            <a href='mailto:support@errormonitor.com' style='background: #667eea; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Contact Support</a>" +
                    "        </div>" +
                    "        " +
                    "        <hr style='border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;'>" +
                    "        " +
                    "        <div style='text-align: center; color: #666;'>" +
                    "            <p style='margin: 0 0 10px 0;'><strong>Best regards,</strong></p>" +
                    "            <p style='margin: 0; color: #667eea; font-weight: bold;'>Error Monitoring Support Team</p>" +
                    "            <p style='margin: 10px 0 0 0; font-size: 14px;'>📧 support@errormonitor.com | 📞 +91 8104474200</p>" +
                    "        </div>" +
                    "    </div>" +
                    "    " +
                    "    <div style='text-align: center; margin-top: 20px; color: #999; font-size: 12px;'>" +
                    "        <p>© 2025 Error Monitoring System. All rights reserved.</p>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";
                
                emailService.sendHtmlEmail(
                    query.getEmail(),
                    "✅ Query Resolved - " + query.getSubject() + " - Error Monitoring System",
                    resolutionHtml
                );
            } catch (Exception e) {
                System.err.println("Failed to send resolution email: " + e.getMessage());
            }
            
            return ResponseEntity.ok(Map.of("message", "Query resolved successfully and email sent to user"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to resolve query", "error", e.getMessage()));
        }
    }
}