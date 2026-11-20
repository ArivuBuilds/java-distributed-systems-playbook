package com.arivubuilds.microservices.dbperservice.catalog;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Polyglot Document Store model owned exclusively by the Catalog bounded context.
 * Flexible attributes map replaces rigid relational EAV tables.
 */
public record CatalogProductDocument(
        String productId,
        String title,
        String category,
        BigDecimal currentListPrice,
        Map<String, String> flexibleSpecs
) {}