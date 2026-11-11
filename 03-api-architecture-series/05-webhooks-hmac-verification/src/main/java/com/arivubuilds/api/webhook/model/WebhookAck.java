package com.arivubuilds.api.webhook.model;

import java.time.Instant;

public record WebhookAck(
        String status,
        String message,
        Instant processedAt
) {}