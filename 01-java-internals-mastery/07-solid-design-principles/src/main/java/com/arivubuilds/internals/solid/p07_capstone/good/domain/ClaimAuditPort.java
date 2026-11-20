package com.arivubuilds.internals.solid.p07_capstone.good.domain;

public interface ClaimAuditPort {
    String persistAudit(ClaimRequest request, String payoutRef);
}
