package com.arivubuilds.microservices.bulkhead.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DownstreamCompartmentClient {

    private final RestClient restClient;
    private final String inventoryUri;
    private final String analyticsUri;

    public DownstreamCompartmentClient(
            @Value("${downstream.inventory-uri}") String inventoryUri,
            @Value("${downstream.analytics-uri}") String analyticsUri) {
        this.restClient = RestClient.create();
        this.inventoryUri = inventoryUri;
        this.analyticsUri = analyticsUri;
    }

    public String reserveInventory(String sku) {
        return restClient.get()
                .uri(inventoryUri + "/inventory/reserve/" + sku)
                .retrieve()
                .body(String.class);
    }

    public String generateHeavyAnalytics(String reportId) {
        return restClient.get()
                .uri(analyticsUri + "/analytics/report/" + reportId)
                .retrieve()
                .body(String.class);
    }
}