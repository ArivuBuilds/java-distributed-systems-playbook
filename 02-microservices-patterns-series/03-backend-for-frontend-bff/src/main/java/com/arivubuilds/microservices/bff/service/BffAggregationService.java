package com.arivubuilds.microservices.bff.service;

import com.arivubuilds.microservices.bff.client.DownstreamDomainClient;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.CustomerProfile;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.LoyaltyAccount;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.OrderHistoryResponse;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.OrderSummary;
import com.arivubuilds.microservices.bff.dto.MobileHomeResponse;
import com.arivubuilds.microservices.bff.dto.WebDashboardResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BffAggregationService {

    private final DownstreamDomainClient domainClient;
    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public BffAggregationService(DownstreamDomainClient domainClient) {
        this.domainClient = domainClient;
    }

    public MobileHomeResponse buildMobileHome(String customerId) {
        AggregatedDomainData data = fanOutAndAggregate(customerId);
        List<OrderSummary> orders = data.orders().orders();

        BigDecimal latestAmount = orders.isEmpty() ? BigDecimal.ZERO : orders.get(0).totalAmount();
        String firstName = data.profile().fullName().split(" ")[0];

        return new MobileHomeResponse(
                customerId,
                firstName,
                orders.size(),
                latestAmount,
                data.loyalty().tier(),
                data.loyalty().pointsBalance(),
                data.degradedLoyalty()
        );
    }

    public WebDashboardResponse buildWebDashboard(String customerId) {
        AggregatedDomainData data = fanOutAndAggregate(customerId);
        List<OrderSummary> orders = data.orders().orders();

        BigDecimal totalSpend = orders.stream()
                .map(OrderSummary::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new WebDashboardResponse(
                data.profile(),
                orders,
                totalSpend,
                data.loyalty(),
                data.degradedLoyalty()
        );
    }

    private AggregatedDomainData fanOutAndAggregate(String customerId) {
        CompletableFuture<CustomerProfile> profileFuture =
                CompletableFuture.supplyAsync(() -> domainClient.fetchProfile(customerId), virtualExecutor);

        CompletableFuture<OrderHistoryResponse> ordersFuture =
                CompletableFuture.supplyAsync(() -> domainClient.fetchOrders(customerId), virtualExecutor);

        CompletableFuture<LoyaltyResult> loyaltyFuture =
                CompletableFuture.supplyAsync(() -> new LoyaltyResult(domainClient.fetchLoyalty(customerId), false), virtualExecutor)
                        .exceptionally(ex -> new LoyaltyResult(
                                new LoyaltyAccount(customerId, "STANDARD", 0, 0), true));

        CompletableFuture.allOf(profileFuture, ordersFuture, loyaltyFuture).join();

        LoyaltyResult loyaltyResult = loyaltyFuture.join();
        return new AggregatedDomainData(
                profileFuture.join(),
                ordersFuture.join(),
                loyaltyResult.account(),
                loyaltyResult.degraded()
        );
    }

    private record LoyaltyResult(LoyaltyAccount account, boolean degraded) {}

    private record AggregatedDomainData(
            CustomerProfile profile,
            OrderHistoryResponse orders,
            LoyaltyAccount loyalty,
            boolean degradedLoyalty
    ) {}
}