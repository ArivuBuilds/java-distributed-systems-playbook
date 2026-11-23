package com.arivubuilds.microservices.retry.controller;

import com.arivubuilds.microservices.retry.dto.SettlementDispatchResponse;
import com.arivubuilds.microservices.retry.service.SettlementDispatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementRetryController {

    private final SettlementDispatchService dispatchService;

    public SettlementRetryController(SettlementDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/{transactionId}")
    public ResponseEntity<SettlementDispatchResponse> settle(@PathVariable String transactionId) {
        SettlementDispatchResponse response = dispatchService.dispatchWithDecorrelatedRetry(transactionId);
        if ("SETTLED".equals(response.status())) {
            return ResponseEntity.ok(response);
        }
        if (response.status().startsWith("NON_RETRYABLE")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}