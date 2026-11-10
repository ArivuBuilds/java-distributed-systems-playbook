package com.arivubuilds.api.rest.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("Order with identifier '" + orderId + "' was not found");
    }
}