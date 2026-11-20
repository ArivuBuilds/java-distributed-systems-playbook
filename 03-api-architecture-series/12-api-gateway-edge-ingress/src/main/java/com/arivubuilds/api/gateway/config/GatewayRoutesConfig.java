package com.arivubuilds.api.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayRoutesConfig {

    @Value("${downstream.accounts-url}")
    private String accountsUrl;

    @Value("${downstream.transactions-url}")
    private String transactionsUrl;

    @Bean
    public RouteLocator customEdgeRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("accounts-reverse-proxy", r -> r
                        .path("/proxy/accounts/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Routed-Service", "accounts-core"))
                        .uri(accountsUrl))
                .route("transactions-reverse-proxy", r -> r
                        .path("/proxy/transactions/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Routed-Service", "transactions-ledger"))
                        .uri(transactionsUrl))
                .build();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}