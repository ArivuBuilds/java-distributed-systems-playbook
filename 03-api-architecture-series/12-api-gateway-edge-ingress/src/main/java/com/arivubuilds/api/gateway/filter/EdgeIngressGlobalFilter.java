package com.arivubuilds.api.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class EdgeIngressGlobalFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String existingCorrelationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
        String correlationId = (existingCorrelationId != null && !existingCorrelationId.isBlank())
                ? existingCorrelationId
                : "edge-" + UUID.randomUUID().toString().substring(0, 8);

        // Simulate edge SSL/TLS termination metadata forwarded downstream over internal HTTP
        String forwardedProto = exchange.getRequest().getHeaders().getFirst("X-Forwarded-Proto");
        String effectiveProto = (forwardedProto != null && !forwardedProto.isBlank()) ? forwardedProto : "https";

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CORRELATION_HEADER, correlationId)
                .header("X-Forwarded-Proto", effectiveProto)
                .header("X-Edge-Gateway", "arivubuilds-ingress-v1")
                .build();

        exchange.getResponse().getHeaders().add(CORRELATION_HEADER, correlationId);
        exchange.getResponse().getHeaders().add("X-Edge-TLS-Offloaded", "true");

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}