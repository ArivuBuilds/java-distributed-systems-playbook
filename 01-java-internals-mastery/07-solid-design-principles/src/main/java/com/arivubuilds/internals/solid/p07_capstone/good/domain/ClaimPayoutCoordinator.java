package com.arivubuilds.internals.solid.p07_capstone.good.domain;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ClaimPayoutCoordinator {
    private final ClaimEligibilityValidator validator;
    private final Map<String, ClaimPayoutStrategy> strategies;
    private final ClaimAuditPort auditPort;
    private final ClaimReceiptNotifier notifier;
    public ClaimPayoutCoordinator(ClaimEligibilityValidator validator, Map<String, ClaimPayoutStrategy> strategies, ClaimAuditPort auditPort, ClaimReceiptNotifier notifier) {
        this.validator = validator; this.strategies = strategies; this.auditPort = auditPort; this.notifier = notifier;
    }
    public String processClaim(ClaimRequest request) {
        validator.validate(request);

        ClaimPayoutStrategy strategy = strategies.get(request.claimType());
        if (strategy == null) throw new IllegalArgumentException("Unregistered claim type: " + request.claimType());
        String payoutRef = strategy.disburse(request);
        return auditPort.persistAudit(request, payoutRef) + " | " + payoutRef + " | " + notifier.notifyPolicyholder(request, payoutRef);
    }
}
