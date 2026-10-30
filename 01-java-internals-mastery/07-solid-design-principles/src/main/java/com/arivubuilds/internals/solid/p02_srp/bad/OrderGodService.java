package com.arivubuilds.internals.solid.p02_srp.bad;

import java.math.BigDecimal;

/**
 * SRP Violation: Four separate reasons to change:
 * 1. Inventory rules
 * 2. Payment provider APIs
 * 3. Database persistence
 * 4. Notification delivery
 */
public class OrderGodService {

    public void processOrder(String orderId, String itemCode, int quantity, BigDecimal amount, String email) {
        // Reason 1: Inventory logic
        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid inventory quantity");
        }
        System.out.println("Reserved stock for item: " + itemCode);

        // Reason 2: Payment gateway logic
        System.out.println("Charging payment gateway: " + amount);

        // Reason 3: Database persistence
        System.out.println("Writing order record to DB: " + orderId);

        // Reason 4: Notification logic
        System.out.println("Sending email to: " + email);
    }
}