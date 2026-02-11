package com.tpms.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("File controller is working!");
    }
    
    @PostMapping("/upload-test")
    public ResponseEntity<String> uploadTest(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String uploadDir = "uploads/resumes/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            
            String filename = file.getOriginalFilename();
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return ResponseEntity.ok("File uploaded successfully: " + filename + " at " + filePath.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/resume/{filename}")
    public ResponseEntity<Resource> getResume(@PathVariable String filename) {
        try {
            // Try multiple possible paths
            String[] possiblePaths = {
                "uploads/resumes/" + filename,
                "uploads/" + filename,
                filename
            };
            
            File file = null;
            for (String path : possiblePaths) {
                File testFile = new File(path);
                if (testFile.exists()) {
                    file = testFile;
                    break;
                }
            }
            
            if (file == null || !file.exists()) {
                System.out.println("File not found: " + filename);
                System.out.println("Tried paths: " + String.join(", ", possiblePaths));
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            System.out.println("Error serving file: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/resume/download/{filename}")
    public ResponseEntity<Resource> downloadResume(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/resumes/" + filename);
            File file = filePath.toFile();
            
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .contentLength(file.length())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}