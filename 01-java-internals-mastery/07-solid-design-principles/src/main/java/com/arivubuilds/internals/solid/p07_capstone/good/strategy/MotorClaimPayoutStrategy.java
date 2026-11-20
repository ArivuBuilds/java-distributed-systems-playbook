package com.arivubuilds.internals.solid.p07_capstone.good.strategy;

import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimPayoutStrategy;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimRequest;
import org.springframework.stereotype.Component;

@Component("MOTOR")
public class MotorClaimPayoutStrategy implements ClaimPayoutStrategy {
    @Override public String disburse(ClaimRequest request) { return "GARAGE_DIRECT_PAYOUT:+" + request.amount(); }
}
