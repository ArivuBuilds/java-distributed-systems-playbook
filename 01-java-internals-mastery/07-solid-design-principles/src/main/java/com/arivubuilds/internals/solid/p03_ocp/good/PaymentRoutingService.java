package com.arivubuilds.internals.solid.p03_ocp.good;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentRoutingService {

    private final Map<String, PaymentStrategy> strategyRegistry;

    // Spring collects all beans implementing PaymentStrategy into this list
    public PaymentRoutingService(List<PaymentStrategy> strategies) {
        this.strategyRegistry = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        PaymentStrategy::getPaymentType,
                        Function.identity()
                ));
    }

    public void executePayment(String paymentType, BigDecimal amount) {
        PaymentStrategy strategy = Optional.ofNullable(strategyRegistry.get(paymentType))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment type: " + paymentType));

        strategy.process(amount);
    }

    public boolean supports(String paymentType) {
        return strategyRegistry.containsKey(paymentType);
    }
}