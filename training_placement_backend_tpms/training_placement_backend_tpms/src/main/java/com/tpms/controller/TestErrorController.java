package com.tpms.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestErrorController {

    @GetMapping("/error")
    public String testError() {
        throw new RuntimeException("Test error for monitoring system");
    }
    
    @GetMapping("/null-error")
    public String testNullError() {
        String test = null;
        return test.toString(); // This will throw NullPointerException
    }
}