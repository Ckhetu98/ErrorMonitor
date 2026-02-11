package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.CompanyDto;
import com.tpms.service.CompanyService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyDto>>> getAll() {
        return ResponseEntity.ok(companyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyDto>> create(@RequestBody CompanyDto dto) {
        return ResponseEntity.status(201).body(companyService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDto>> update(@PathVariable Long id, @RequestBody CompanyDto dto) {
        return ResponseEntity.ok(companyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.delete(id));
    }
}
