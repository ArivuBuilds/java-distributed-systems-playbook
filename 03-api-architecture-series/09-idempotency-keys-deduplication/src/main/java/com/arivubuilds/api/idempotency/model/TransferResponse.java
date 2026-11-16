package com.arivubuilds.api.idempotency.model;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        String transactionId,
        String status,
        BigDecimal amount,
        Instant processedAt
) {}