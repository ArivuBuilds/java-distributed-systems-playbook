package com.arivubuilds.internals.solid.p03_ocp.good;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UpiPaymentStrategy implements PaymentStrategy {

    @Override
    public String getPaymentType() {
        return "UPI";
    }

    @Override
    public void process(BigDecimal amount) {
        System.out.println("Processing instant bank debit of " + amount + " via NPCI UPI Rail.");
    }
}