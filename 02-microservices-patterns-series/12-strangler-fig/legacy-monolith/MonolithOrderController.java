package com.arivubuilds.strangler.legacy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class MonolithOrderController {

    @GetMapping("/{orderId}")
    public ResponseEntity> getLegacyOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(Map.of(
            "orderId", orderId,
            "engine", "Legacy-Monolith-Spring-Boot-2",
            "status", "ACTIVE_IN_MONOLITH"
        ));
    }
}