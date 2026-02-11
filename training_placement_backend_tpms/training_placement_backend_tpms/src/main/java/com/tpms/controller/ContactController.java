package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.ContactDto;
import com.tpms.enums.InquiryStatus;
import com.tpms.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactDto>> create(@RequestBody ContactDto dto) {
        return ResponseEntity.status(201).body(contactService.create(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactDto>>> getAll() {
        return ResponseEntity.ok(contactService.getAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ContactDto>>> getByStatus(@PathVariable InquiryStatus status) {
        return ResponseEntity.ok(contactService.getByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContactDto>> updateStatus(@PathVariable Long id, @RequestParam InquiryStatus status) {
        return ResponseEntity.ok(contactService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.delete(id));
    }
}