package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.JobDto;
import com.tpms.entity.Company;
import com.tpms.entity.Job;
import com.tpms.entity.User;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.CompanyRepository;
import com.tpms.repository.JobRepository;
import com.tpms.repository.RecruiterProfileRepository;
import com.tpms.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final UserRepository userRepository;
    private final com.tpms.repository.ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<List<JobDto>> getAll() {
        List<JobDto> list = jobRepository.findAll().stream()
                .map(j -> {
                    JobDto dto = modelMapper.map(j, JobDto.class);
                    try {
                        if (j.getCompany() != null) {
                            dto.setCompanyId(j.getCompany().getId());
                            dto.setCompanyName(j.getCompany().getName());
                            try {
                                if (j.getCompany().getOwner() != null) {
                                    dto.setPostedBy(j.getCompany().getOwner().getId());
                                }
                            } catch (Exception ex) {
                                // ignore owner mapping issues
                            }
                        } else {
                            dto.setCompanyName("N/A");
                        }
                        
                        // Count applications for this job
                        int appCount = applicationRepository.countByJobId(j.getId());
                        dto.setApplicationCount(appCount);
                        
                    } catch (Exception e) {
                        // Handle case where company reference is broken
                        dto.setCompanyId(null);
                        dto.setCompanyName("N/A");
                        dto.setApplicationCount(0);
                    }
                    return dto;
                }).collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    @Override
    public ApiResponse<JobDto> create(JobDto jobDto) {
        // Check if recruiter has profile
        if (jobDto.getPostedBy() != null) {
            User recruiter = userRepository.findById(jobDto.getPostedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));
            
            if (!recruiterProfileRepository.existsByUserId(jobDto.getPostedBy())) {
                throw new RuntimeException("Please complete your profile before posting jobs");
            }
            
            // Find recruiter's company
            Company company = companyRepository.findAll().stream()
                    .filter(c -> c.getOwner() != null && c.getOwner().getId().equals(jobDto.getPostedBy()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Company profile not found"));
            
            Job job = modelMapper.map(jobDto, Job.class);
            // ensure additional fields from DTO are set (salary, skillsRequired, deadline)
            job.setSalary(jobDto.getSalary());
            job.setSkillsRequired(jobDto.getSkillsRequired());
            job.setDeadline(jobDto.getDeadline());
            job.setCompany(company);
            Job saved = jobRepository.save(job);
            JobDto dto = modelMapper.map(saved, JobDto.class);
            dto.setCompanyId(saved.getCompany().getId());
            // set postedBy from company owner so frontend can filter recruiter jobs
            if (saved.getCompany() != null && saved.getCompany().getOwner() != null) {
                dto.setPostedBy(saved.getCompany().getOwner().getId());
            }
            return new ApiResponse<>(java.time.Instant.now(), "Created", "SUCCESS", dto);
        }
        
        Job job = modelMapper.map(jobDto, Job.class);
        job.setSalary(jobDto.getSalary());
        job.setSkillsRequired(jobDto.getSkillsRequired());
        job.setDeadline(jobDto.getDeadline());
        if (jobDto.getCompanyId() != null) {
            Company c = companyRepository.findById(jobDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            job.setCompany(c);
        }
        Job saved = jobRepository.save(job);
        JobDto dto = modelMapper.map(saved, JobDto.class);
        if (saved.getCompany() != null) dto.setCompanyId(saved.getCompany().getId());
        if (saved.getCompany() != null && saved.getCompany().getOwner() != null) {
            dto.setPostedBy(saved.getCompany().getOwner().getId());
        }
        return new ApiResponse<>(java.time.Instant.now(), "Created", "SUCCESS", dto);
    }

    @Override
    public ApiResponse<JobDto> update(Long id, JobDto jobDto) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        job.setTitle(jobDto.getTitle());
        job.setDescription(jobDto.getDescription());
        job.setLocation(jobDto.getLocation());
        job.setEmploymentType(jobDto.getEmploymentType());
        // update additional fields
        job.setSalary(jobDto.getSalary());
        job.setSkillsRequired(jobDto.getSkillsRequired());
        job.setDeadline(jobDto.getDeadline());
        jobRepository.save(job);
        JobDto dto = modelMapper.map(job, JobDto.class);
        if (job.getCompany() != null) dto.setCompanyId(job.getCompany().getId());
        if (job.getCompany() != null && job.getCompany().getOwner() != null) {
            dto.setPostedBy(job.getCompany().getOwner().getId());
        }
        return new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", dto);
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Job job = jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        // Delete all applications for this job first to avoid foreign key constraint issues
        applicationRepository.deleteByJobId(id);
        jobRepository.delete(job);
        return new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null);
    }
}
