package com.arivubuilds.aisystem.gateway.service;

import com.arivubuilds.aisystem.gateway.model.GatewayContracts.CompletionRequest;
import com.arivubuilds.aisystem.gateway.model.GatewayContracts.RoutingStrategy;
import com.arivubuilds.aisystem.gateway.provider.ModelProviderAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class DynamicModelRouter {

    private final List<ModelProviderAdapter> providers;

    public DynamicModelRouter(List<ModelProviderAdapter> providers) {
        this.providers = providers;
    }

    public ModelProviderAdapter selectProvider(CompletionRequest request) {
        if (request.preferredProvider() != null && !request.preferredProvider().isBlank()) {
            return providers.stream()
                    .filter(p -> p.providerId().equalsIgnoreCase(request.preferredProvider().trim()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unknown preferredProvider: " + request.preferredProvider()
                    ));
        }

        RoutingStrategy strategy = request.effectiveStrategy();
        Comparator<ModelProviderAdapter> comparator = switch (strategy) {
            case LOW_LATENCY -> Comparator.comparingInt(ModelProviderAdapter::typicalLatencyMs);
            case COST_OPTIMIZED -> Comparator.comparingDouble(ModelProviderAdapter::costPer1kTokensUsd);
            case HIGH_REASONING -> Comparator.comparingDouble(ModelProviderAdapter::costPer1kTokensUsd).reversed();
        };

        return providers.stream()
                .filter(p -> p.supports(strategy))
                .min(comparator)
                .orElseGet(() -> providers.getFirst());
    }
}