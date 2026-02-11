package com.tpms.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/test")
public class TestController {
    
    @GetMapping("/error")
    public String testError() {
        throw new RuntimeException("Test error from Training Placement Management System");
    }
}