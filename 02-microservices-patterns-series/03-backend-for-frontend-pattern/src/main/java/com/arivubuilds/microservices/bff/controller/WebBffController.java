package com.arivubuilds.microservices.bff.controller;

import com.arivubuilds.microservices.bff.dto.WebDashboardResponse;
import com.arivubuilds.microservices.bff.service.BffAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff/web")
public class WebBffController {

    private final BffAggregationService aggregationService;

    public WebBffController(BffAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/dashboard/{customerId}")
    public ResponseEntity<WebDashboardResponse> getWebDashboard(@PathVariable String customerId) {
        return ResponseEntity.ok(aggregationService.buildWebDashboard(customerId));
    }
}