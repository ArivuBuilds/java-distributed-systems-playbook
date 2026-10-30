package com.arivubuilds.internals.solid.p02_srp.good;

import org.springframework.stereotype.Component;

@Component
public class CustomerNotificationService {

    public void sendOrderConfirmation(String email, String orderId) {
        System.out.println("Confirmation sent to: " + email + " for order: " + orderId);
    }
}