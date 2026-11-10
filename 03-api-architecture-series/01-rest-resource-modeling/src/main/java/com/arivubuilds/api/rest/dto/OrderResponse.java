package com.arivubuilds.api.rest.dto;

import com.arivubuilds.api.rest.model.OrderStatus;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.Instant;

@Relation(itemRelation = "order", collectionRelation = "orders")
public class OrderResponse extends RepresentationModel<OrderResponse> {
    private final String id;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final OrderStatus status;
    private final Instant createdAt;

    public OrderResponse(String id, String customerId, BigDecimal totalAmount, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}