package com.arivubuilds.microservices.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class SslTerminationOffloadFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = "gw-" + UUID.randomUUID().toString().substring(0, 8);
        }

        final String finalRequestId = requestId;

        // Terminate TLS at the ingress edge and enforce clean X-Forwarded-* headers after Gateway's built-in XForwardedHeadersFilter
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.set("X-Request-Id", finalRequestId);
                    headers.set("X-Forwarded-Proto", "https");
                    headers.set("X-Forwarded-Port", "443");
                    headers.set("X-SSL-Terminated-At-Edge", "true");
                })
                .build();

        exchange.getResponse().getHeaders().set("X-Request-Id", finalRequestId);
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return 10;
    }
}