package com.arivubuilds.microservices.outbox.model;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
    UUID eventId,
    String aggregateType,
    String aggregateId,
    String eventType,
    String payloadJson,
    String traceId,
    Instant createdAt
) {}
