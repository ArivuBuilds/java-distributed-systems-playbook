package com.arivubuilds.internals.solid.p07_capstone.good.domain;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ClaimEligibilityValidator {
    public void validate(ClaimRequest request) {
        if (request == null || request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Claim amount must be positive");
    }
}
