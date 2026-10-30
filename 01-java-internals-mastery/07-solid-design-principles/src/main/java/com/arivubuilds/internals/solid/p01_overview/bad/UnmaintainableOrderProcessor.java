package com.arivubuilds.internals.solid.p01_overview.bad;

import com.arivubuilds.internals.solid.p01_overview.OrderRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Common Anti-Pattern: A "modern" Spring service where business rules,
 * persistence, payment gateways, and notifications are tightly coupled.
 */
@Service
public class UnmaintainableOrderProcessor {

    public void checkout(OrderRequest request) {
        // 1. Business & Pricing rule calculation
        BigDecimal finalAmount = request.amount();
        if (request.amount().compareTo(new BigDecimal("1000")) > 0) {
            finalAmount = finalAmount.multiply(new BigDecimal("0.90"));
        }

        // 2. Direct hardcoded payment handling
        if ("CREDIT_CARD".equalsIgnoreCase(request.paymentMethod())) {
            System.out.println("Processing credit card charge: " + finalAmount);
        } else if ("UPI".equalsIgnoreCase(request.paymentMethod())) {
            System.out.println("Processing UPI payment: " + finalAmount);
        }

        // 3. Database persistence simulation
        System.out.println("Saving order " + request.orderId() + " to database.");

        // 4. Notification logic
        System.out.println("Sending confirmation email to: " + request.customerEmail());
    }
}