package com.arivubuilds.microservices.bff.client;

import com.arivubuilds.microservices.bff.dto.DownstreamDtos.CustomerProfile;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.LoyaltyAccount;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.OrderHistoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DownstreamDomainClient {

    private final RestClient restClient;
    private final String profileUri;
    private final String ordersUri;
    private final String loyaltyUri;

    public DownstreamDomainClient(
            @Value("${downstream.profile-uri}") String profileUri,
            @Value("${downstream.orders-uri}") String ordersUri,
            @Value("${downstream.loyalty-uri}") String loyaltyUri) {
        this.restClient = RestClient.create();
        this.profileUri = profileUri;
        this.ordersUri = ordersUri;
        this.loyaltyUri = loyaltyUri;
    }

    public CustomerProfile fetchProfile(String customerId) {
        return restClient.get()
                .uri(profileUri + "/profiles/" + customerId)
                .retrieve()
                .body(CustomerProfile.class);
    }

    public OrderHistoryResponse fetchOrders(String customerId) {
        return restClient.get()
                .uri(ordersUri + "/orders/customer/" + customerId)
                .retrieve()
                .body(OrderHistoryResponse.class);
    }

    public LoyaltyAccount fetchLoyalty(String customerId) {
        return restClient.get()
                .uri(loyaltyUri + "/loyalty/" + customerId)
                .retrieve()
                .body(LoyaltyAccount.class);
    }
}