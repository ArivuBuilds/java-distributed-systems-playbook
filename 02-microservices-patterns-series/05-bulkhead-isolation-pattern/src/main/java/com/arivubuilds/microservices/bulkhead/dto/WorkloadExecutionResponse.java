package com.arivubuilds.microservices.bulkhead.dto;

public record WorkloadExecutionResponse(
        String compartment,
        String bulkheadType,
        String status,
        String workerThreadName,
        String payload
) {}