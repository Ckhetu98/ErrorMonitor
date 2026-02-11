package com.tpms.service;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.ContactDto;
import com.tpms.entity.Contact;
import com.tpms.enums.InquiryStatus;
import com.tpms.exception.ResourceNotFoundException;
import com.tpms.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<ContactDto> create(ContactDto dto) {
        Contact contact = modelMapper.map(dto, Contact.class);
        contact.setStatus(InquiryStatus.NEW);
        Contact saved = contactRepository.save(contact);
        ContactDto result = modelMapper.map(saved, ContactDto.class);
        return new ApiResponse<>(Instant.now(), "Contact inquiry created successfully", "SUCCESS", result);
    }

    @Override
    public ApiResponse<List<ContactDto>> getAll() {
        List<Contact> contacts = contactRepository.findAllByOrderByCreatedAtDesc();
        List<ContactDto> result = contacts.stream()
                .map(c -> modelMapper.map(c, ContactDto.class))
                .toList();
        return new ApiResponse<>(Instant.now(), "Contacts retrieved successfully", "SUCCESS", result);
    }

    @Override
    public ApiResponse<List<ContactDto>> getByStatus(InquiryStatus status) {
        List<Contact> contacts = contactRepository.findByStatusOrderByCreatedAtDesc(status);
        List<ContactDto> result = contacts.stream()
                .map(c -> modelMapper.map(c, ContactDto.class))
                .toList();
        return new ApiResponse<>(Instant.now(), "Contacts retrieved successfully", "SUCCESS", result);
    }

    @Override
    public ApiResponse<ContactDto> updateStatus(Long id, InquiryStatus status) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        contact.setStatus(status);
        Contact updated = contactRepository.save(contact);
        ContactDto result = modelMapper.map(updated, ContactDto.class);
        return new ApiResponse<>(Instant.now(), "Contact status updated successfully", "SUCCESS", result);
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        contactRepository.delete(contact);
        return new ApiResponse<>(Instant.now(), "Contact deleted successfully", "SUCCESS", null);
    }
}