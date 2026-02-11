package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.CompanyDto;
import com.tpms.entity.Company;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final com.tpms.repository.JobRepository jobRepository;
    private final com.tpms.repository.ApplicationRepository applicationRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<List<CompanyDto>> getAll() {
        List<Company> companies = companyRepository.findAll();
        List<CompanyDto> list = companies.stream()
                .map(c -> {
                    CompanyDto dto = modelMapper.map(c, CompanyDto.class);
                    // Force load the owner if it exists
                    if (c.getOwner() != null) {
                        dto.setOwnerId(c.getOwner().getId());
                        dto.setOwnerName(c.getOwner().getFullName());
                        dto.setOwnerEmail(c.getOwner().getEmail());
                        dto.setOwnerPhone(c.getOwner().getPhone());
                    }
                    
                    // Count jobs for this company
                    int jobCount = jobRepository.findByCompanyId(c.getId()).size();
                    dto.setJobCount(jobCount);
                    
                    // Count students hired by this company
                    int studentsHired = (int) applicationRepository.findAll().stream()
                        .filter(app -> app.getJob().getCompany() != null && 
                                      app.getJob().getCompany().getId().equals(c.getId()) &&
                                      app.getStatus() == com.tpms.enums.PlacementStatus.SELECTED)
                        .count();
                    dto.setStudentsHired(studentsHired);
                    
                    return dto;
                })
                .collect(Collectors.toList());
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", list);
    }

    @Override
    public ApiResponse<CompanyDto> getById(Long id) {
        Company c = companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        CompanyDto dto = modelMapper.map(c, CompanyDto.class);
        if (c.getOwner() != null) {
            dto.setOwnerId(c.getOwner().getId());
            dto.setOwnerName(c.getOwner().getFullName());
            dto.setOwnerEmail(c.getOwner().getEmail());
            dto.setOwnerPhone(c.getOwner().getPhone());
        }
        return new ApiResponse<>(java.time.Instant.now(), "OK", "SUCCESS", dto);
    }

    @Override
    public ApiResponse<CompanyDto> create(CompanyDto dto) {
        Company c = modelMapper.map(dto, Company.class);
        Company saved = companyRepository.save(c);
        return new ApiResponse<>(java.time.Instant.now(), "Created", "SUCCESS", modelMapper.map(saved, CompanyDto.class));
    }

    @Override
    public ApiResponse<CompanyDto> update(Long id, CompanyDto dto) {
        Company c = companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        c.setName(dto.getName()); c.setWebsite(dto.getWebsite()); c.setAddress(dto.getAddress()); c.setDescription(dto.getDescription());
        companyRepository.save(c);
        return new ApiResponse<>(java.time.Instant.now(), "Updated", "SUCCESS", modelMapper.map(c, CompanyDto.class));
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Company c = companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        companyRepository.delete(c);
        return new ApiResponse<>(java.time.Instant.now(), "Deleted", "SUCCESS", null);
    }
}
