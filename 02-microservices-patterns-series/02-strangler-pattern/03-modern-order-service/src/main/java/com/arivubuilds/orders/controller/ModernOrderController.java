package com.arivubuilds.orders.controller;

import com.arivubuilds.orders.domain.OrderEntity;
import com.arivubuilds.orders.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class ModernOrderController {

    private final OrderService orderService;

    public ModernOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderEntity>> getOrders() {
        return ResponseEntity.ok(orderService.listOrders());
    }

    @PostMapping
    public ResponseEntity<OrderEntity> createOrder(@RequestBody Map<String, Object> payload) {
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        String productCode = payload.get("productCode").toString();
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());
        BigDecimal price = payload.containsKey("price") && payload.get("price") != null
                ? new BigDecimal(payload.get("price").toString())
                : null;

        OrderEntity created = orderService.createOrder(customerId, productCode, quantity, price);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}