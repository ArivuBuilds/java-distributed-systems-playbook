package com.arivubuilds.microservices.dbperservice.composition;

import com.arivubuilds.microservices.dbperservice.catalog.CatalogProductDocument;
import com.arivubuilds.microservices.dbperservice.orders.OrderBoundedService;
import com.arivubuilds.microservices.dbperservice.orders.OrderLedgerEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class OrderCatalogComposer {

    private final OrderBoundedService orderService;
    private final RestClient restClient;
    private final String catalogBaseUri;

    public OrderCatalogComposer(OrderBoundedService orderService,
                                @Value("${downstream.catalog-service-uri}") String catalogBaseUri) {
        this.orderService = orderService;
        this.restClient = RestClient.create();
        this.catalogBaseUri = catalogBaseUri;
    }

    public ComposedOrderView getComposedOrderDetails(String orderId) {
        // 1. Query local relational Orders DB
        OrderLedgerEntity order = orderService.getOrder(orderId);

        // 2. Query remote Catalog Document Store over HTTP API boundary (zero SQL JOIN)
        try {
            CatalogProductDocument liveProduct = restClient.get()
                    .uri(catalogBaseUri + "/internal/catalog/products/" + order.getProductId())
                    .retrieve()
                    .body(CatalogProductDocument.class);

            if (liveProduct != null) {
                return new ComposedOrderView(
                        order.getOrderId(),
                        order.getCustomerId(),
                        order.getProductId(),
                        order.getQuantity(),
                        order.getUnitPriceSnapshot(),
                        liveProduct.currentListPrice(),
                        liveProduct.title(),
                        liveProduct.flexibleSpecs(),
                        "API_COMPOSED_ACROSS_BOUNDED_CONTEXTS"
                );
            }
        } catch (Exception ignored) {
            // Fall through to local bounded snapshot
        }

        return new ComposedOrderView(
                order.getOrderId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getUnitPriceSnapshot(),
                order.getUnitPriceSnapshot(),
                order.getCachedProductTitle(),
                Map.of(),
                "LOCAL_ORDERS_SNAPSHOT_FALLBACK"
        );
    }
}