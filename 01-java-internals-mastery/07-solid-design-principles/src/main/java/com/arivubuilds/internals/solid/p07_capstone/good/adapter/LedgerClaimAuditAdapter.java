package com.arivubuilds.internals.solid.p07_capstone.good.adapter;

import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimAuditPort;
import com.arivubuilds.internals.solid.p07_capstone.good.domain.ClaimRequest;
import org.springframework.stereotype.Component;

@Component
public class LedgerClaimAuditAdapter implements ClaimAuditPort {
    @Override public String persistAudit(ClaimRequest request, String payoutRef) { return "AUDIT_LEDGER:" + request.claimId(); }
}
