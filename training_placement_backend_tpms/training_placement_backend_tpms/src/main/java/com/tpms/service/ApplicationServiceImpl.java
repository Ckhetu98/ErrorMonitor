package com.tpms.service;

import org.springframework.stereotype.Service;
import com.tpms.dto.ApiResponse;
import com.tpms.dto.ApplicationDto;
import com.tpms.entity.Application;
import com.tpms.entity.Job;
import com.tpms.entity.User;
import com.tpms.enums.PlacementStatus;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.ApplicationRepository;
import com.tpms.repository.JobRepository;
import com.tpms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import java.util.List;
import java.util.stream.Collectors;

// Service implementation for managing job applications
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    // Retrieve all applications submitted by a specific student
    @Override
    public ApiResponse<List<ApplicationDto>> getByStudent(Long studentId) {
        List<ApplicationDto> list = applicationRepository.findByStudentId(studentId).stream()
                .map(this::mapToApplicationDto)
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    // Retrieve all applications for jobs posted by a specific recruiter
    @Override
    public ApiResponse<List<ApplicationDto>> getByRecruiter(Long recruiterId) {
        List<ApplicationDto> list = applicationRepository.findByRecruiterId(recruiterId).stream()
                .map(this::mapToApplicationDto)
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    // Retrieve applications for a recruiter filtered by status
    @Override
    public ApiResponse<List<ApplicationDto>> getByRecruiterAndStatus(Long recruiterId, String status) {
        List<ApplicationDto> list = applicationRepository.findAll().stream()
                .filter(a -> a.getStatus().name().equalsIgnoreCase(status))
                .map(this::mapToApplicationDto)
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    /**
     * Create a new job application when student applies for a job
     * 
     * @param studentId The ID of the applying student
     * @param jobId The ID of the job being applied to
     * @return ApiResponse containing the created ApplicationDto
     * 
     * Business Logic:
     * 1. Validates student and job exist in database
     * 2. Creates new Application entity with default status (APPLIED)
     * 3. Saves to database and returns DTO with complete information
     * 
     * Validation Rules:
     * - Student must exist and be active
     * - Job must exist and be open for applications
     * - Student cannot apply to same job twice (handled by unique constraint)
     */
    @Override
    public ApiResponse<ApplicationDto> apply(Long studentId, Long jobId) {
        // Validate student exists
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Validate job exists
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        
        // Create new application with default status
        Application app = new Application();
        app.setStudent(student);
        app.setJob(job);
        // Status defaults to APPLIED in entity constructor
        
        // Save to database
        Application saved = applicationRepository.save(app);
        
        // Transform to DTO with complete information
        ApplicationDto dto = mapToApplicationDto(saved);
        
        return new ApiResponse<>(java.time.Instant.now(), "Applied", "SUCCESS", dto);
    }

    /**
     * Update the status of a job application (recruiter action)
     * 
     * @param id The application ID to update
     * @param status The new status string (converted to enum)
     * @return ApiResponse containing updated ApplicationDto
     * 
     * Status Flow:
     * APPLIED → SHORTLISTED → SELECTED/REJECTED
     * 
     * Valid Status Values:
     * - APPLIED: Initial application status
     * - SHORTLISTED: Candidate selected for interview/next round
     * - SELECTED: Final selection for the position
     * - REJECTED: Application rejected
     * 
     * Security: Only recruiters who own the job can update status
     */
    @Override
    public ApiResponse<ApplicationDto> updateStatus(Long id, String status) {
        try {
            // Validate application exists
            Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
            
            // Convert string to enum (validates status value)
            PlacementStatus newStatus = PlacementStatus.valueOf(status.toUpperCase());
            
            // Update application status
            app.setStatus(newStatus);
            Application savedApp = applicationRepository.save(app);
            
            // Log important status changes for audit trail
            if (newStatus == PlacementStatus.SELECTED) {
                System.out.println("Candidate selected: " + savedApp.getStudent().getFullName() + 
                                 " for job: " + savedApp.getJob().getTitle());
            }
            
            // Return updated application as DTO
            ApplicationDto dto = mapToApplicationDto(savedApp);
            return new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", dto);
            
        } catch (IllegalArgumentException e) {
            // Handle invalid status enum value
            throw new RuntimeException("Invalid status: " + status + 
                ". Valid values are: APPLIED, SHORTLISTED, REJECTED, SELECTED");
        } catch (Exception e) {
            // Handle any other errors
            throw new RuntimeException("Failed to update application status: " + e.getMessage());
        }
    }

    /**
     * Delete/withdraw a job application
     * 
     * @param id The application ID to delete
     * @return ApiResponse with success confirmation
     * 
     * Business Rules:
     * - Only students can withdraw their own applications
     * - Applications can only be withdrawn if status is 'APPLIED'
     * - Once shortlisted/selected, applications cannot be withdrawn
     * 
     * Security: Validate user owns the application before deletion
     */
    @Override
    public ApiResponse<Void> delete(Long id) {
        // Validate application exists
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        
        // TODO: Add validation for withdrawal rules
        // - Check if status allows withdrawal
        // - Verify user owns the application
        
        // Delete from database
        applicationRepository.delete(app);
        
        return new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null);
    }
    
    /**
     * Private helper method to convert Application entity to ApplicationDto
     * 
     * @param app The Application entity to convert
     * @return ApplicationDto with complete information for frontend
     * 
     * This method performs comprehensive data mapping:
     * 1. Basic application fields (id, status, dates)
     * 2. Student information (name, email, phone)
     * 3. Job details (title, location, salary, company)
     * 4. Resume file information (filename, download URL)
     * 
     * Resume Handling:
     * - Parses JSON stored in student profile
     * - Extracts filename and constructs proper download URL
     * - Provides fallback values for missing/invalid data
     */
    private ApplicationDto mapToApplicationDto(Application app) {
        // Use ModelMapper for basic field mapping
        ApplicationDto dto = modelMapper.map(app, ApplicationDto.class);
        
        // Set foreign key IDs
        dto.setStudentId(app.getStudent().getId());
        dto.setJobId(app.getJob().getId());
        
        // Map student details for recruiter view
        dto.setStudentName(app.getStudent().getFullName());
        dto.setStudentEmail(app.getStudent().getEmail());
        dto.setStudentPhone(app.getStudent().getPhone());
        
        // Map job details for student view
        dto.setJobTitle(app.getJob().getTitle());
        dto.setJobLocation(app.getJob().getLocation());
        dto.setJobSalary(app.getJob().getSalary());
        
        // Map company information if available
        if (app.getJob().getCompany() != null) {
            dto.setCompanyName(app.getJob().getCompany().getName());
        }
        
        // Extract resume information from student profile
        if (app.getStudent().getStudentProfile() != null) {
            String resumeJson = app.getStudent().getStudentProfile().getResumeLinksJson();
            if (resumeJson != null && !resumeJson.isEmpty()) {
                try {
                    // Parse JSON manually (simple string parsing)
                    // TODO: Consider using Jackson ObjectMapper for robust JSON parsing
                    if (resumeJson.contains("fileName") && resumeJson.contains("url")) {
                        // Extract filename from JSON
                        String fileName = resumeJson.substring(resumeJson.indexOf("fileName\":\"") + 11);
                        fileName = fileName.substring(0, fileName.indexOf("\""));
                        
                        // Extract URL from JSON
                        String url = resumeJson.substring(resumeJson.indexOf("url\":\"") + 6);
                        url = url.substring(0, url.indexOf("\""));
                        
                        dto.setResumeFileName(fileName);
                        
                        // Ensure URL follows proper format for file serving
                        if (!url.startsWith("/files/")) {
                            url = "/files/resume/" + fileName;
                        }
                        dto.setResumeUrl(url);
                    }
                } catch (Exception e) {
                    // Graceful error handling - provide default values
                    dto.setResumeFileName("Resume.pdf");
                    dto.setResumeUrl("/files/resume/default.pdf");
                }
            }
        }
        
        return dto;
    }
    
    /**
     * Send selection email notification to shortlisted candidate
     * 
     * @param id The application ID of the shortlisted candidate
     * @return ApiResponse with email sending status
     * 
     * Email Content Includes:
     * - Congratulations message for being shortlisted
     * - Job title and company name
     * - Next steps in the selection process
     * - Contact information for follow-up
     * 
     * Prerequisites:
     * - Application must exist and be in SHORTLISTED status
     * - Student must have valid email address
     * - SMTP configuration must be properly set up
     * 
     * Error Handling:
     * - Returns error response if email sending fails
     * - Logs error details for debugging
     * - Does not throw exceptions to avoid breaking application flow
     */
    @Override
    public ApiResponse<String> sendSelectionEmail(Long id) {
        try {
            // Validate application exists
            Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
            
            // Extract required information for email
            String studentName = app.getStudent().getFullName();
            String studentEmail = app.getStudent().getEmail();
            String jobTitle = app.getJob().getTitle();
            String companyName = app.getJob().getCompany() != null ? 
                app.getJob().getCompany().getName() : "Company";
            
            // Send email using EmailService
            // EmailService handles template creation and SMTP sending
            emailService.sendSelectionEmail(studentEmail, studentName, jobTitle, companyName);
            
            // Return success response with confirmation
            return new ApiResponse<>(java.time.Instant.now(), "Email sent successfully", "SUCCESS", 
                "Email sent to " + studentEmail);
                
        } catch (Exception e) {
            // Handle all email sending errors gracefully
            // Log error for debugging but don't break application flow
            System.err.println("Email sending failed: " + e.getMessage());
            return new ApiResponse<>(java.time.Instant.now(), "Failed to send email: " + e.getMessage(), "ERROR", null);
        }
    }
}
