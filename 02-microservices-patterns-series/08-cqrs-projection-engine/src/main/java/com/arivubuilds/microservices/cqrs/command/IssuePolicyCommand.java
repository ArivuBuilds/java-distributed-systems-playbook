package com.arivubuilds.microservices.cqrs.command;

import java.math.BigDecimal;

public record IssuePolicyCommand(
    String policyId,
    String policyHolder,
    String planType,
    BigDecimal coverageAmount,
    BigDecimal annualPremium
) {
    public IssuePolicyCommand {
        if (policyId == null || policyId.isBlank()) {
            throw new IllegalArgumentException("Policy ID must not be blank");
        }
        if (coverageAmount == null || coverageAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Coverage amount must be positive");
        }
        if (annualPremium == null || annualPremium.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Annual premium must be positive");
        }
    }
}
