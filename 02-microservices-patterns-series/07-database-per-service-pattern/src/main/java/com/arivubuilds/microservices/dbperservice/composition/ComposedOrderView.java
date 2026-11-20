package com.arivubuilds.microservices.dbperservice.composition;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Application-layer API Composition result replacing cross-database SQL JOINs.
 */
public record ComposedOrderView(
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal purchasedUnitPrice,
        BigDecimal liveCatalogPrice,
        String liveCatalogTitle,
        Map<String, String> liveCatalogSpecs,
        String compositionSource
) {}