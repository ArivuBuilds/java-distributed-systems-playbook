package com.arivubuilds.orders.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "modern_orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status;

    private String fulfillmentTier;

    private LocalDateTime createdAt;

    public OrderEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public OrderEntity(Long id, String orderNumber, Long customerId, String productCode,
                       Integer quantity, BigDecimal totalAmount, String status,
                       String fulfillmentTier, LocalDateTime createdAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.productCode = productCode;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = status;
        this.fulfillmentTier = fulfillmentTier;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFulfillmentTier() { return fulfillmentTier; }
    public void setFulfillmentTier(String fulfillmentTier) { this.fulfillmentTier = fulfillmentTier; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private Long id;
        private String orderNumber;
        private Long customerId;
        private String productCode;
        private Integer quantity;
        private BigDecimal totalAmount;
        private String status;
        private String fulfillmentTier;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderNumber(String orderNumber) { this.orderNumber = orderNumber; return this; }
        public Builder customerId(Long customerId) { this.customerId = customerId; return this; }
        public Builder productCode(String productCode) { this.productCode = productCode; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder fulfillmentTier(String fulfillmentTier) { this.fulfillmentTier = fulfillmentTier; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public OrderEntity build() {
            return new OrderEntity(id, orderNumber, customerId, productCode, quantity, totalAmount, status, fulfillmentTier, createdAt);
        }
    }
}