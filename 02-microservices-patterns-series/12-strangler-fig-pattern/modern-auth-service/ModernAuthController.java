package com.arivubuilds.strangler.modern.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class ModernAuthController {

    @PostMapping("/login")
    public ResponseEntity> login(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(Map.of(
            "token", "jwt-token-strangled-v1",
            "service", "Modern-Auth-Service-Spring-Boot-3",
            "authenticatedAt", Instant.now().toString()
        ));
    }
}