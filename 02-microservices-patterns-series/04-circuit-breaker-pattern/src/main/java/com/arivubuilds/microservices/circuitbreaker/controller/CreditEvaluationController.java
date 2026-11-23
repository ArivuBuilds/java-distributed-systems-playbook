package com.arivubuilds.microservices.circuitbreaker.controller;

import com.arivubuilds.microservices.circuitbreaker.dto.CreditAssessmentResponse;
import com.arivubuilds.microservices.circuitbreaker.service.CreditEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/credit")
public class CreditEvaluationController {

    private final CreditEvaluationService creditService;

    public CreditEvaluationController(CreditEvaluationService creditService) {
        this.creditService = creditService;
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CreditAssessmentResponse> assess(@PathVariable String customerId) {
        return ResponseEntity.ok(creditService.evaluateCredit(customerId));
    }

    @GetMapping("/breaker-state")
    public ResponseEntity<Map<String, String>> breakerState() {
        return ResponseEntity.ok(Map.of(
                "breaker", CreditEvaluationService.BREAKER_NAME,
                "state", creditService.getCurrentBreakerState()
        ));
    }
}
