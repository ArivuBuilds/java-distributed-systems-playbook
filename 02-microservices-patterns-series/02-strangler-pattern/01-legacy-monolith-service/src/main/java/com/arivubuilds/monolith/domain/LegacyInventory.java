package com.arivubuilds.monolith.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "legacy_inventory")
public class LegacyInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer availableStock;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    public LegacyInventory() {}

    public LegacyInventory(Long id, String productCode, String productName, Integer availableStock, BigDecimal unitPrice) {
        this.id = id;
        this.productCode = productCode;
        this.productName = productName;
        this.availableStock = availableStock;
        this.unitPrice = unitPrice;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public static class Builder {
        private Long id;
        private String productCode;
        private String productName;
        private Integer availableStock;
        private BigDecimal unitPrice;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder productCode(String productCode) { this.productCode = productCode; return this; }
        public Builder sku(String sku) { this.productCode = sku; return this; }
        public Builder productName(String productName) { this.productName = productName; return this; }
        public Builder availableStock(Integer availableStock) { this.availableStock = availableStock; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public LegacyInventory build() {
            return new LegacyInventory(id, productCode, productName, availableStock, unitPrice);
        }
    }
}