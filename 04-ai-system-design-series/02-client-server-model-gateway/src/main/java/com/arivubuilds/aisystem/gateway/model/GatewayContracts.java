package com.arivubuilds.aisystem.gateway.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public final class GatewayContracts {
    private GatewayContracts() {}

    public enum RoutingStrategy {
        COST_OPTIMIZED,
        LOW_LATENCY,
        HIGH_REASONING
    }

    public record CompletionRequest(
            @NotBlank String tenantId,
            @NotBlank String prompt,
            RoutingStrategy routingStrategy,
            String preferredProvider,
            @Min(1) Integer maxTokens
    ) {
        public RoutingStrategy effectiveStrategy() {
            return routingStrategy != null ? routingStrategy : RoutingStrategy.COST_OPTIMIZED;
        }

        public int effectiveMaxTokens() {
            return maxTokens != null ? maxTokens : 256;
        }
    }

    public record TokenUsage(
            int promptTokens,
            int completionTokens,
            int totalTokens,
            int remainingTenantBudget
    ) {}

    public record CompletionResponse(
            String requestId,
            String providerId,
            String modelName,
            String content,
            long latencyMs,
            TokenUsage usage,
            Instant timestamp
    ) {}

    public record StreamChunk(
            String requestId,
            String providerId,
            int sequence,
            String delta,
            boolean done
    ) {}

    public record ProviderQuote(
            String providerId,
            String modelName,
            int estimatedLatencyMs,
            double costPer1kTokensUsd,
            List<String> tokens
    ) {}
}