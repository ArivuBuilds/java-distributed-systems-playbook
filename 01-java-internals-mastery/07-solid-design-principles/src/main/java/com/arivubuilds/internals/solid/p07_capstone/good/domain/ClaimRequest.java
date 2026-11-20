package com.arivubuilds.internals.solid.p07_capstone.good.domain;

import java.math.BigDecimal;

public record ClaimRequest(String claimId, BigDecimal amount, String claimType, String customerPhone) {}
