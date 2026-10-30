package com.arivubuilds.internals.solid.p02_srp;

import com.arivubuilds.internals.solid.p02_srp.good.CustomerNotificationService;
import com.arivubuilds.internals.solid.p02_srp.good.InventoryReservationService;
import com.arivubuilds.internals.solid.p02_srp.good.OrderCoordinator;
import com.arivubuilds.internals.solid.p02_srp.good.PaymentExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCoordinatorTest {

    @Mock
    private InventoryReservationService inventoryService;

    @Mock
    private PaymentExecutionService paymentService;

    @Mock
    private CustomerNotificationService notificationService;

    @InjectMocks
    private OrderCoordinator coordinator;

    @Test
    void shouldCoordinateAllOrderStepsIndependently() {
        String orderId = "ORD-101";
        String sku = "SKU-99";
        int qty = 2;
        BigDecimal amount = new BigDecimal("499.00");
        String email = "dev@arivubuilds.com";

        coordinator.processOrder(orderId, sku, qty, amount, email);

        verify(inventoryService).reserve(sku, qty);
        verify(paymentService).charge(orderId, amount);
        verify(notificationService).sendOrderConfirmation(email, orderId);
    }
}