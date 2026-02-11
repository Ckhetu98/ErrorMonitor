package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.CompanyDto;

import java.util.List;

public interface CompanyService {
    ApiResponse<List<CompanyDto>> getAll();
    ApiResponse<CompanyDto> getById(Long id);
    ApiResponse<CompanyDto> create(CompanyDto dto);
    ApiResponse<CompanyDto> update(Long id, CompanyDto dto);
    ApiResponse<Void> delete(Long id);
}
