package com.arivubuilds.api.webhook.model;

import java.math.BigDecimal;
import java.time.Instant;

public record WebhookPayload(
        String eventId,
        String eventType,
        String accountId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {}