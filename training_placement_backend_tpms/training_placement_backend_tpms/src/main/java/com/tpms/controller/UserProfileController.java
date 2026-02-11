package com.tpms.controller;

import com.tpms.dto.ApiResponse;
import com.tpms.dto.StudentProfileDto;
import com.tpms.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserProfileController {
    private final StudentProfileService studentProfileService;

    @GetMapping("/student/profile/{id}")
    public ResponseEntity<ApiResponse<StudentProfileDto>> getStudentProfile(@PathVariable Long id) {
        return ResponseEntity.ok(studentProfileService.getByUserId(id));
    }

    @PutMapping("/student/profile/{id}")
    public ResponseEntity<ApiResponse<StudentProfileDto>> updateStudentProfile(@PathVariable Long id, @RequestBody StudentProfileDto dto) {
        return ResponseEntity.ok(studentProfileService.update(id, dto));
    }
    
    @PostMapping("/student/profile/{id}/resume")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadResume(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(studentProfileService.uploadResume(id, file));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(java.time.Instant.now(), "Upload failed", "ERROR", null));
        }
    }
    
    @GetMapping("/student/profile/{id}/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {
        try {
            Map<String, Object> resumeData = studentProfileService.getResume(id);
            byte[] fileContent = (byte[]) resumeData.get("content");
            String fileName = (String) resumeData.get("fileName");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
