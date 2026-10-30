package com.arivubuilds.internals.solid.p03_ocp;

import com.arivubuilds.internals.solid.p03_ocp.good.CreditCardPaymentStrategy;
import com.arivubuilds.internals.solid.p03_ocp.good.PaymentRoutingService;
import com.arivubuilds.internals.solid.p03_ocp.good.PaymentStrategy;
import com.arivubuilds.internals.solid.p03_ocp.good.UpiPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRoutingServiceTest {

    private PaymentRoutingService routingService;

    @BeforeEach
    void setUp() {
        List<PaymentStrategy> strategies = List.of(
                new UpiPaymentStrategy(),
                new CreditCardPaymentStrategy()
        );
        routingService = new PaymentRoutingService(strategies);
    }

    @Test
    void shouldRouteUpiSuccessfully() {
        assertTrue(routingService.supports("UPI"));
        routingService.executePayment("UPI", new BigDecimal("1500.00"));
    }

    @Test
    void shouldRouteCreditCardSuccessfully() {
        assertTrue(routingService.supports("CREDIT_CARD"));
        routingService.executePayment("CREDIT_CARD", new BigDecimal("2500.00"));
    }

    @Test
    void shouldThrowExceptionForUnsupportedRailWithoutModifyingCode() {
        assertThrows(IllegalArgumentException.class, () ->
                routingService.executePayment("APPLE_PAY", new BigDecimal("100.00"))
        );
    }
}