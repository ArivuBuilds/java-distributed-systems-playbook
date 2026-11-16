package com.arivubuilds.api.idempotency.model;

import java.math.BigDecimal;

public record TransferRequest(
        String sourceAccount,
        String targetAccount,
        BigDecimal amount,
        String currency
) {}