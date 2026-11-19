package com.arivubuilds.microservices.bulkhead.controller;

import com.arivubuilds.microservices.bulkhead.dto.WorkloadExecutionResponse;
import com.arivubuilds.microservices.bulkhead.service.BulkheadCompartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/bulkhead")
public class BulkheadIsolationController {

    private final BulkheadCompartmentService compartmentService;

    public BulkheadIsolationController(BulkheadCompartmentService compartmentService) {
        this.compartmentService = compartmentService;
    }

    @GetMapping("/inventory/{sku}")
    public ResponseEntity<WorkloadExecutionResponse> reserveInventory(@PathVariable String sku) {
        WorkloadExecutionResponse response = compartmentService.executeInventoryWithSemaphore(sku);
        HttpStatus status = "ADMITTED".equals(response.status()) ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/analytics/{reportId}")
    public CompletableFuture<ResponseEntity<WorkloadExecutionResponse>> runAnalytics(@PathVariable String reportId) {
        return compartmentService.executeAnalyticsWithThreadPool(reportId)
                .thenApply(response -> {
                    HttpStatus status = "ADMITTED".equals(response.status()) ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
                    return ResponseEntity.status(status).body(response);
                });
    }
}