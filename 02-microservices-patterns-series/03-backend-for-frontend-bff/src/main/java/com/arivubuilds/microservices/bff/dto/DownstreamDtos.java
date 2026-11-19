package com.arivubuilds.microservices.bff.dto;

import java.math.BigDecimal;
import java.util.List;

public final class DownstreamDtos {

    private DownstreamDtos() {}

    public record CustomerProfile(
            String customerId,
            String fullName,
            String email,
            String phone,
            String city,
            String kycStatus
    ) {}

    public record OrderSummary(
            String orderId,
            String itemDescription,
            BigDecimal totalAmount,
            String status,
            String warehouseCode
    ) {}

    public record OrderHistoryResponse(
            String customerId,
            List<OrderSummary> orders
    ) {}

    public record LoyaltyAccount(
            String customerId,
            String tier,
            int pointsBalance,
            int pointsExpiringSoon
    ) {}
}