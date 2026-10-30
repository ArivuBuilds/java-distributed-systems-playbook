package com.arivubuilds.internals.solid.p01_overview.good;

import com.arivubuilds.internals.solid.p01_overview.OrderRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class CleanOrderService {

    private final ApplicationEventPublisher eventPublisher;

    public CleanOrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void createOrder(OrderRequest request) {
        BigDecimal finalAmount = request.amount();
        System.out.println("Persisting order " + request.orderId() + " to database.");

        // Decouple side-effects (notifications, analytics, audit) via domain events
        eventPublisher.publishEvent(new OrderCreatedEvent(request.orderId(), request.customerEmail(), finalAmount));
    }
}