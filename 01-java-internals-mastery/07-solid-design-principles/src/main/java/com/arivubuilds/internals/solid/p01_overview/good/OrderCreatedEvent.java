package com.arivubuilds.internals.solid.p01_overview.good;

import java.math.BigDecimal;

public record OrderCreatedEvent(String orderId, String customerEmail, BigDecimal finalAmount) {}