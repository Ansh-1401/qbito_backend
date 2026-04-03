package com.scan2dine.scan2dine.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.frontend.url}")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "QBito backend is running ✅";
    }
}
