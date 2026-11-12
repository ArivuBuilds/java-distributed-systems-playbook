package com.arivubuilds.api.sse.model;

import java.time.Instant;

public record ServerMetricPulse(
        long sequenceId,
        String nodeId,
        double cpuUtilization,
        long memoryUsedMb,
        Instant generatedAt
) {}