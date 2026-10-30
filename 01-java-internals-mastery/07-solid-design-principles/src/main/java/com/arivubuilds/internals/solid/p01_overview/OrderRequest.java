package com.arivubuilds.internals.solid.p01_overview;

import java.math.BigDecimal;

public record OrderRequest(
        String orderId,
        String customerEmail,
        BigDecimal amount,
        String paymentMethod
) {}