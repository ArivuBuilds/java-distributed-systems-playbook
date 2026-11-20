package com.arivubuilds.api.gateway.model;

import java.time.Instant;
import java.util.List;

public record DashboardBffResponse(
        String customerId,
        AccountSummary account,
        List<TransactionItem> recentTransactions,
        boolean partialDegradation,
        Instant aggregatedAt
) {}