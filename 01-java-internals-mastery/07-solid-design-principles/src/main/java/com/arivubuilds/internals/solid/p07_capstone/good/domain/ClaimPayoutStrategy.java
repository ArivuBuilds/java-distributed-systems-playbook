package com.arivubuilds.internals.solid.p07_capstone.good.domain;

public interface ClaimPayoutStrategy {
    String disburse(ClaimRequest request);
}
