package com.arivubuilds.microservices.bff.dto;

import com.arivubuilds.microservices.bff.dto.DownstreamDtos.CustomerProfile;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.LoyaltyAccount;
import com.arivubuilds.microservices.bff.dto.DownstreamDtos.OrderSummary;

import java.math.BigDecimal;
import java.util.List;

/**
 * Rich, multi-widget contract tailored for desktop web dashboards.
 */
public record WebDashboardResponse(
        CustomerProfile profile,
        List<OrderSummary> recentOrders,
        BigDecimal lifetimeSpend,
        LoyaltyAccount loyalty,
        boolean degradedLoyaltyData
) {}