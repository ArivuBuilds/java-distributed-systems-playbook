package com.arivubuilds.api.gateway.model;

import java.math.BigDecimal;

public record TransactionItem(
        String txnId,
        String merchant,
        BigDecimal amount,
        String direction
) {}