package com.arivubuilds.microservices.retry.dto;

import java.util.List;

public record SettlementDispatchResponse(
        String transactionId,
        String status,
        int totalAttempts,
        List<Long> scheduledBackoffDelaysMs,
        String upstreamReference
) {}