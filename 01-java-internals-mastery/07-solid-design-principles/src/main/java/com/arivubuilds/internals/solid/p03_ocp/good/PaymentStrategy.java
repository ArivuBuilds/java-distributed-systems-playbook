package com.arivubuilds.internals.solid.p03_ocp.good;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String getPaymentType();
    void process(BigDecimal amount);
}