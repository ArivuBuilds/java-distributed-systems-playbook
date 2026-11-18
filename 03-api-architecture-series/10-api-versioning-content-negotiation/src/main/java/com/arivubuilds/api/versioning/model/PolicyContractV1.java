package com.arivubuilds.api.versioning.model;

import java.math.BigDecimal;

public record PolicyContractV1(
        String policyNumber,
        String holderName,
        BigDecimal premiumAmount,
        String status
) {}