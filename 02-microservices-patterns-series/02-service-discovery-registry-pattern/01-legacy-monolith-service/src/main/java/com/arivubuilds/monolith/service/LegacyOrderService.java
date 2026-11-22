package com.arivubuilds.monolith.service;

import com.arivubuilds.monolith.domain.LegacyCustomer;
import com.arivubuilds.monolith.domain.LegacyInventory;
import com.arivubuilds.monolith.domain.LegacyOrder;
import com.arivubuilds.monolith.repository.LegacyCustomerRepository;
import com.arivubuilds.monolith.repository.LegacyInventoryRepository;
import com.arivubuilds.monolith.repository.LegacyOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LegacyOrderService {

    private final LegacyOrderRepository orderRepository;
    private final LegacyCustomerRepository customerRepository;
    private final LegacyInventoryRepository inventoryRepository;

    public LegacyOrderService(LegacyOrderRepository orderRepository,
                              LegacyCustomerRepository customerRepository,
                              LegacyInventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<LegacyOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public LegacyOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));
    }

    @Transactional
    public LegacyOrder placeOrder(Long customerId, String productCode, Integer quantity) {
        LegacyCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        LegacyInventory item = inventoryRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("Product code not found in inventory: " + productCode));

        if (item.getAvailableStock() < quantity) {
            throw new IllegalStateException("Insufficient inventory for: " + productCode);
        }

        item.setAvailableStock(item.getAvailableStock() - quantity);
        inventoryRepository.save(item);

        BigDecimal total = item.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LegacyOrder order = LegacyOrder.builder()
                .orderNumber(orderNumber)
                .customerId(customer.getId())
                .productCode(productCode)
                .quantity(quantity)
                .totalAmount(total)
                .status("COMPLETED")
                .build();

        return orderRepository.save(order);
    }
}