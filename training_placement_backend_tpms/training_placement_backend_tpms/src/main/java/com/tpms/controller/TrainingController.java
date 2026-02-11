package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.TrainingDto;
import com.tpms.service.TrainingService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {
    private final TrainingService trainingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainingDto>>> getAll() {
        return ResponseEntity.ok(trainingService.getAll());
    }

    @GetMapping("/student/{id}")
    public ResponseEntity<ApiResponse<List<TrainingDto>>> getByStudent(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getByStudent(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TrainingDto>> create(@RequestBody TrainingDto dto) {
        return ResponseEntity.status(201).body(trainingService.create(dto));
    }

    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<Void>> enroll(@RequestParam Long studentId, @RequestParam Long trainingId) {
        return ResponseEntity.status(201).body(trainingService.enroll(studentId, trainingId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainingDto>> update(@PathVariable Long id, @RequestBody TrainingDto dto) {
        return ResponseEntity.ok(trainingService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.delete(id));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<ApiResponse<List<Object>>> getEnrolledStudents(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getEnrolledStudents(id));
    }
}
