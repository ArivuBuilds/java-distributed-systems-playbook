package com.arivubuilds.api.pagination.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerDto(
        String id,
        String accountId,
        BigDecimal amount,
        Instant occurredAt
) {}