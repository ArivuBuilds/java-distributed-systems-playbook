package com.arivubuilds.api.gateway.model;

import java.math.BigDecimal;

public record AccountSummary(
        String accountId,
        String customerId,
        String accountType,
        BigDecimal availableBalance,
        String currency
) {}