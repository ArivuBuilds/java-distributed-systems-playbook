package com.arivubuilds.microservices.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TokenOffloadGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/public/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rejectUnauthorized(exchange, "Missing or malformed Bearer token at API Gateway edge");
        }

        String rawToken = authHeader.substring(7).trim();
        // Token format: <subject>:<roles> (Base64 encoded) for deterministic edge offload verification
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(rawToken), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return rejectUnauthorized(exchange, "Invalid edge token encoding");
        }

        String[] parts = decoded.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank()) {
            return rejectUnauthorized(exchange, "Invalid edge token claims");
        }

        // Offload auth at edge: strip raw Bearer token and inject trusted internal identity headers
        ServerHttpRequest downstreamRequest = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                .header("X-Authenticated-User", parts[0])
                .header("X-User-Roles", parts[1])
                .build();

        return chain.filter(exchange.mutate().request(downstreamRequest).build());
    }

    private Mono<Void> rejectUnauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"error\":\"UNAUTHORIZED\",\"detail\":\"" + reason + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}