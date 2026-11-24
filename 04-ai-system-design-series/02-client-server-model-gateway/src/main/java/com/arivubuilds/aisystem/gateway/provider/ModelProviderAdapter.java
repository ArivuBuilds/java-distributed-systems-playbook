package com.arivubuilds.aisystem.gateway.provider;

import com.arivubuilds.aisystem.gateway.model.GatewayContracts.CompletionRequest;
import com.arivubuilds.aisystem.gateway.model.GatewayContracts.ProviderQuote;
import com.arivubuilds.aisystem.gateway.model.GatewayContracts.RoutingStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

public interface ModelProviderAdapter {
    String providerId();
    String modelName();
    int typicalLatencyMs();
    double costPer1kTokensUsd();
    boolean supports(RoutingStrategy strategy);
    ProviderQuote execute(CompletionRequest request);

    @Component
    class LocalSlmProvider implements ModelProviderAdapter {
        @Override public String providerId() { return "local-slm"; }
        @Override public String modelName() { return "phi-3.5-mini-onnx"; }
        @Override public int typicalLatencyMs() { return 18; }
        @Override public double costPer1kTokensUsd() { return 0.0002; }
        @Override public boolean supports(RoutingStrategy strategy) {
            return strategy == RoutingStrategy.LOW_LATENCY || strategy == RoutingStrategy.COST_OPTIMIZED;
        }
        @Override public ProviderQuote execute(CompletionRequest request) {
            String reply = "[local-slm] Fast edge response for: " + request.prompt();
            return new ProviderQuote(providerId(), modelName(), typicalLatencyMs(), costPer1kTokensUsd(), tokenize(reply));
        }
    }

    @Component
    class BalancedCloudProvider implements ModelProviderAdapter {
        @Override public String providerId() { return "balanced-cloud"; }
        @Override public String modelName() { return "claude-haiku-4-5"; }
        @Override public int typicalLatencyMs() { return 95; }
        @Override public double costPer1kTokensUsd() { return 0.0010; }
        @Override public boolean supports(RoutingStrategy strategy) {
            return strategy == RoutingStrategy.COST_OPTIMIZED;
        }
        @Override public ProviderQuote execute(CompletionRequest request) {
            String reply = "[balanced-cloud] Structured synthesis for: " + request.prompt();
            return new ProviderQuote(providerId(), modelName(), typicalLatencyMs(), costPer1kTokensUsd(), tokenize(reply));
        }
    }

    @Component
    class FrontierReasoningProvider implements ModelProviderAdapter {
        @Override public String providerId() { return "frontier-reasoning"; }
        @Override public String modelName() { return "claude-sonnet-4-6"; }
        @Override public int typicalLatencyMs() { return 240; }
        @Override public double costPer1kTokensUsd() { return 0.0060; }
        @Override public boolean supports(RoutingStrategy strategy) {
            return strategy == RoutingStrategy.HIGH_REASONING;
        }
        @Override public ProviderQuote execute(CompletionRequest request) {
            String reply = "[frontier-reasoning] Deep architectural analysis for: " + request.prompt();
            return new ProviderQuote(providerId(), modelName(), typicalLatencyMs(), costPer1kTokensUsd(), tokenize(reply));
        }
    }

    static List<String> tokenize(String text) {
        return Arrays.stream(text.split("(?<=\\s)"))
                .filter(s -> !s.isEmpty())
                .toList();
    }
}