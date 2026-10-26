package com.arivubuilds.monolith.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderDtos {

    public record CreateOrderRequest(
            @NotNull Long customerId,
            @NotBlank String sku,
            @NotNull @Min(1) Integer quantity
    ) {}

    public record OrderResponse(
            Long orderId,
            String orderNumber,
            Long customerId,
            String customerName,
            String sku,
            String productName,
            Integer quantity,
            BigDecimal totalAmount,
            String status,
            String source
    ) {}
}