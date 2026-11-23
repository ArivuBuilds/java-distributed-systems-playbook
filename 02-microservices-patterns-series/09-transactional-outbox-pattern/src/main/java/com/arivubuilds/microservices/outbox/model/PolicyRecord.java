package com.arivubuilds.microservices.outbox.model;

import java.math.BigDecimal;
import java.time.Instant;

public record PolicyRecord(
    String policyId,
    String holderName,
    BigDecimal coverageAmount,
    BigDecimal premiumAmount,
    String status,
    Instant createdAt
) {}
