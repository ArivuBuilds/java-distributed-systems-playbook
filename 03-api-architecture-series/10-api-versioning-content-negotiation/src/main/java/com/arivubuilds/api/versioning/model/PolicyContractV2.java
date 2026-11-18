package com.arivubuilds.api.versioning.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyContractV2(
        String policyNumber,
        HolderProfile holder,
        MonetaryValue annualPremium,
        String lifecycleState,
        LocalDate effectiveDate
) {
    public record HolderProfile(String fullName, String tier) {}
    public record MonetaryValue(BigDecimal amount, String currency) {}
}