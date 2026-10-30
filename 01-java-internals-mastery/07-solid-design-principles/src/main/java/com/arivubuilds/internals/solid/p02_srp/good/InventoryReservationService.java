package com.arivubuilds.internals.solid.p02_srp.good;

import org.springframework.stereotype.Component;

@Component
public class InventoryReservationService {

    public void reserve(String itemCode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
        System.out.println("Inventory reserved: " + itemCode + " (Qty: " + quantity + ")");
    }
}