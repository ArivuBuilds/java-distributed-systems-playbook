package com.arivubuilds.internals.solid.p07_capstone.good.strategy;

import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimPayoutStrategy;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimRequest;
import org.springframework.stereotype.Component;

@Component("HEALTH")
public class HealthClaimPayoutStrategy implements ClaimPayoutStrategy {
    @Override public String disburse(ClaimRequest request) { return "TPD_HEALTH_PAYOUT:" + request.amount(); }
}
