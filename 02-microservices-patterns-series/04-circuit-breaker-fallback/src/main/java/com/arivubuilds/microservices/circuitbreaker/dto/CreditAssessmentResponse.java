package com.arivubuilds.microservices.circuitbreaker.dto;

public record CreditAssessmentResponse(
        String customerId,
        int creditScore,
        String riskTier,
        String source,
        String breakerState
) {}