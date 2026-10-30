package com.arivubuilds.internals.solid.p03_ocp.bad;

import java.math.BigDecimal;

/**
 * OCP Violation: Every new payment rail forces modifying and re-testing
 * this core class, inflating regression risks.
 */
public class LegacyPaymentProcessor {

    public void processPayment(String paymentType, BigDecimal amount) {
        if ("CREDIT_CARD".equalsIgnoreCase(paymentType)) {
            System.out.println("Processing credit card charge: " + amount);
        } else if ("UPI".equalsIgnoreCase(paymentType)) {
            System.out.println("Processing UPI instant transfer: " + amount);
        } else if ("CRYPTO".equalsIgnoreCase(paymentType)) {
            System.out.println("Processing blockchain transfer: " + amount);
        } else {
            throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }
    }
}