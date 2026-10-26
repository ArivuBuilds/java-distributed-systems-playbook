package com.arivubuilds.monolith.controller;

import com.arivubuilds.monolith.domain.LegacyOrder;
import com.arivubuilds.monolith.service.LegacyOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class LegacyOrderController {

    private final LegacyOrderService orderService;

    public LegacyOrderController(LegacyOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<LegacyOrder>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LegacyOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<LegacyOrder> placeOrder(@RequestBody Map<String, Object> payload) {
        Long customerId = Long.valueOf(payload.get("customerId").toString());
        String productCode = payload.get("productCode").toString();
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());

        LegacyOrder order = orderService.placeOrder(customerId, productCode, quantity);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}