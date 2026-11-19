package com.arivubuilds.microservices.bff.controller;

import com.arivubuilds.microservices.bff.dto.MobileHomeResponse;
import com.arivubuilds.microservices.bff.service.BffAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff/mobile")
public class MobileBffController {

    private final BffAggregationService aggregationService;

    public MobileBffController(BffAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/home/{customerId}")
    public ResponseEntity<MobileHomeResponse> getMobileHome(@PathVariable String customerId) {
        return ResponseEntity.ok(aggregationService.buildMobileHome(customerId));
    }
}