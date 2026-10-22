package com.arivubuilds.microservices.cqrs.query;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read Model: Completely denormalized projection tailored specifically 
 * for UI executive dashboards (zero table joins required).
 */
public record PolicyDashboardSummaryView(
    String policyId,
    String policyHolder,
    String planType,
    BigDecimal coverageAmount,
    BigDecimal annualPremium,
    String riskTier,
    Instant lastSynchronizedAt
) {}
