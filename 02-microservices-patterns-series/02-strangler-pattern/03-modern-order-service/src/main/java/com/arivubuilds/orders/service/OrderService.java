package com.arivubuilds.orders.service;

import com.arivubuilds.orders.domain.OrderEntity;
import com.arivubuilds.orders.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderEntity> listOrders() {
        return orderRepository.findAll();
    }

    public OrderEntity createOrder(Long customerId, String productCode, Integer quantity, BigDecimal price) {
        String orderNumber = "MOD-ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BigDecimal total = price != null ? price.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.valueOf(quantity * 99L);

        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .productCode(productCode)
                .quantity(quantity)
                .totalAmount(total)
                .status("CONFIRMED_MICROSERVICE")
                .fulfillmentTier("EXPRESS_PRIORITY")
                .build();

        return orderRepository.save(order);
    }
}