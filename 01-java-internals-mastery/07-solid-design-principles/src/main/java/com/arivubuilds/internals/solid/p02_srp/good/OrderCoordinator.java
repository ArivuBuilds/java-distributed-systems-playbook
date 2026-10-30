package com.arivubuilds.internals.solid.p02_srp.good;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderCoordinator {

    private final InventoryReservationService inventoryService;
    private final PaymentExecutionService paymentService;
    private final CustomerNotificationService notificationService;

    public OrderCoordinator(InventoryReservationService inventoryService,
                            PaymentExecutionService paymentService,
                            CustomerNotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    public void processOrder(String orderId, String itemCode, int quantity, BigDecimal amount, String email) {
        inventoryService.reserve(itemCode, quantity);
        paymentService.charge(orderId, amount);
        notificationService.sendOrderConfirmation(email, orderId);
    }
}