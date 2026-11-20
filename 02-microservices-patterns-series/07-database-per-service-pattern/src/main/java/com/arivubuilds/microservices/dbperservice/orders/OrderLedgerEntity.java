package com.arivubuilds.microservices.dbperservice.orders;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Relational ACID entity owned exclusively by the Orders bounded context.
 * Enforces zero cross-database foreign keys: stores only scalar productId + point-in-time snapshot.
 */
@Entity
@Table(name = "orders_ledger")
public class OrderLedgerEntity {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private String orderId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(name = "cached_product_title", nullable = false)
    private String cachedProductTitle;

    protected OrderLedgerEntity() {}

    public OrderLedgerEntity(String orderId,
                             String customerId,
                             String productId,
                             int quantity,
                             BigDecimal unitPriceSnapshot,
                             String cachedProductTitle) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceSnapshot = unitPriceSnapshot;
        this.cachedProductTitle = cachedProductTitle;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPriceSnapshot() { return unitPriceSnapshot; }
    public String getCachedProductTitle() { return cachedProductTitle; }
    public void setCachedProductTitle(String cachedProductTitle) { this.cachedProductTitle = cachedProductTitle; }
}