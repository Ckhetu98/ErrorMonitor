package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.JobDto;
import com.tpms.service.JobService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobDto>>> getAll() {
        return ResponseEntity.ok(jobService.getAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobDto>> create(@RequestBody JobDto jobDto) {
        return ResponseEntity.status(201).body(jobService.create(jobDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDto>> update(@PathVariable Long id, @RequestBody JobDto jobDto) {
        return ResponseEntity.ok(jobService.update(id, jobDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.delete(id));
    }
}
