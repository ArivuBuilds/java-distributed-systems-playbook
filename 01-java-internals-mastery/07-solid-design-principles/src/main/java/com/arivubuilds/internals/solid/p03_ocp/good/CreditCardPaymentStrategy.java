package com.arivubuilds.internals.solid.p03_ocp.good;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    @Override
    public String getPaymentType() {
        return "CREDIT_CARD";
    }

    @Override
    public void process(BigDecimal amount) {
        System.out.println("Processing credit card charge of " + amount + " via Payment Gateway.");
    }
}