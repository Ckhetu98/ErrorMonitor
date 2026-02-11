package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.ContactDto;
import com.tpms.enums.InquiryStatus;

import java.util.List;

public interface ContactService {
    ApiResponse<ContactDto> create(ContactDto dto);
    ApiResponse<List<ContactDto>> getAll();
    ApiResponse<List<ContactDto>> getByStatus(InquiryStatus status);
    ApiResponse<ContactDto> updateStatus(Long id, InquiryStatus status);
    ApiResponse<Void> delete(Long id);
}