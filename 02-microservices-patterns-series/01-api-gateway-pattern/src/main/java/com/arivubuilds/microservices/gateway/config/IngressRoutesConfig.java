package com.arivubuilds.microservices.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngressRoutesConfig {

    @Value("${services.orders-uri}")
    private String ordersUri;

    @Value("${services.inventory-uri}")
    private String inventoryUri;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("orders-ingress-route", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(ordersUri))
                .route("inventory-ingress-route", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(inventoryUri))
                .build();
    }
}