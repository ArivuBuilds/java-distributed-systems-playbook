package com.arivubuilds.microservices.cqrs.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PolicyUnderwrittenEvent(
    String policyId,
    String policyHolder,
    String planType,
    BigDecimal coverageAmount,
    BigDecimal annualPremium,
    Instant eventTimestamp
) {}
