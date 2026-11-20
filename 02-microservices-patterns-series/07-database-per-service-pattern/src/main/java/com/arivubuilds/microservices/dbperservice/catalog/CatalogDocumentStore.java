package com.arivubuilds.microservices.dbperservice.catalog;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulated NoSQL Document Store isolated from the relational Orders database.
 */
@Component
public class CatalogDocumentStore {

    private final Map<String, CatalogProductDocument> documentCollection = new ConcurrentHashMap<>();

    public CatalogDocumentStore() {
        save(new CatalogProductDocument(
                "PROD-900",
                "Keychron Q1 Pro Mechanical Keyboard",
                "PERIPHERALS",
                new BigDecimal("18500.00"),
                Map.of("switches", "Banana", "layout", "75%", "connectivity", "Bluetooth/USB-C")
        ));
    }

    public void save(CatalogProductDocument document) {
        documentCollection.put(document.productId(), document);
    }

    public Optional<CatalogProductDocument> findById(String productId) {
        return Optional.ofNullable(documentCollection.get(productId));
    }
}