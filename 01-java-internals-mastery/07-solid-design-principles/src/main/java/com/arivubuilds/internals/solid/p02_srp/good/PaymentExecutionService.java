package com.arivubuilds.internals.solid.p02_srp.good;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentExecutionService {

    public void charge(String orderId, BigDecimal amount) {
        System.out.println("Payment charged: " + amount + " for order " + orderId);
    }
}