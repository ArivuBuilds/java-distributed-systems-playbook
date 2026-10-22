package com.arivubuilds.microservices.cqrs.projection;

import com.arivubuilds.microservices.cqrs.event.PolicyUnderwrittenEvent;
import com.arivubuilds.microservices.cqrs.query.PolicyDashboardSummaryView;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Projection Engine: Listens to domain events and builds 
 * pre-aggregated read views into a fast read-optimized cache/store.
 */
public class PolicyProjectionEngine {

    private final Map<String, PolicyDashboardSummaryView> readViewStore = new ConcurrentHashMap<>();

    public void onPolicyUnderwritten(PolicyUnderwrittenEvent event) {
        String riskTier = event.coverageAmount().compareTo(new BigDecimal("1000000")) > 0
            ? "HIGH_NET_WORTH"
            : "STANDARD";

        PolicyDashboardSummaryView view = new PolicyDashboardSummaryView(
            event.policyId(),
            event.policyHolder(),
            event.planType(),
            event.coverageAmount(),
            event.annualPremium(),
            riskTier,
            Instant.now()
        );

        readViewStore.put(event.policyId(), view);
    }

    public Optional<PolicyDashboardSummaryView> findSummaryByPolicyId(String policyId) {
        return Optional.ofNullable(readViewStore.get(policyId));
    }

    public int totalProjections() {
        return readViewStore.size();
    }
}
