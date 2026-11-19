package com.arivubuilds.microservices.bff.dto;

import java.math.BigDecimal;

/**
 * Slim, bandwidth-optimized contract tailored for mobile screens over cellular networks.
 */
public record MobileHomeResponse(
        String customerId,
        String displayName,
        int activeOrderCount,
        BigDecimal latestOrderAmount,
        String loyaltyTier,
        int loyaltyPoints,
        boolean degradedLoyaltyData
) {}